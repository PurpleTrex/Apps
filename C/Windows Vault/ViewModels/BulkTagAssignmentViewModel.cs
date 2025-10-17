using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using WindowsVault.Models;
using WindowsVault.Services;

namespace WindowsVault.ViewModels
{
    public partial class BulkTagAssignmentViewModel : ObservableObject
    {
        private readonly ITagService _tagService;
        private readonly IMediaFileService _mediaFileService;
        private readonly ISettingsService _settingsService;

        [ObservableProperty]
        private ObservableCollection<BulkMediaFileItem> _mediaFiles = new();

        [ObservableProperty]
        private ObservableCollection<TagSelectionItem> _filteredTags = new();

        [ObservableProperty]
        private ObservableCollection<Tag> _recentTags = new();

        [ObservableProperty]
        private string _tagSearchQuery = string.Empty;

        [ObservableProperty]
        private bool _rememberSettings;

        [ObservableProperty]
        private int _selectedCount;

        [ObservableProperty]
        private string _summary = "No tags selected";

        [ObservableProperty]
        private bool _canApplyTags;

        public int MediaFileCount => MediaFiles.Count;

        private ObservableCollection<TagSelectionItem> _allTags = new();

        public BulkTagAssignmentViewModel(
            ITagService tagService,
            IMediaFileService mediaFileService,
            ISettingsService settingsService)
        {
            _tagService = tagService;
            _mediaFileService = mediaFileService;
            _settingsService = settingsService;
        }

        public async Task InitializeAsync(List<MediaFile> mediaFiles)
        {
            // Load media files
            MediaFiles.Clear();
            foreach (var file in mediaFiles)
            {
                MediaFiles.Add(new BulkMediaFileItem
                {
                    MediaFile = file,
                    IsSelected = true,
                    FileName = System.IO.Path.GetFileName(file.FileName),
                    FileInfo = $"{file.MediaType} • {FormatFileSize(file.FileSizeBytes)}",
                    ThumbnailPath = file.ThumbnailPath,
                    CurrentTags = new ObservableCollection<Tag>(file.Tags)
                });
            }

            // Load all tags
            var tags = await _tagService.GetAllTagsAsync();
            _allTags.Clear();
            foreach (var tag in tags)
            {
                _allTags.Add(new TagSelectionItem
                {
                    Tag = tag,
                    Name = tag.Name,
                    Color = tag.Color,
                    UsageCount = tag.UsageCount,
                    IsSelected = false
                });
            }
            FilteredTags = new ObservableCollection<TagSelectionItem>(_allTags);

            // Load recent tags (last 10 used tags)
            var recentTagIds = await _settingsService.GetRecentTagIdsAsync();
            RecentTags.Clear();
            foreach (var tagId in recentTagIds.Take(10))
            {
                var tag = tags.FirstOrDefault(t => t.Id == tagId);
                if (tag != null)
                {
                    RecentTags.Add(tag);
                }
            }

            UpdateSelectedCount();
            UpdateSummary();
        }

        partial void OnTagSearchQueryChanged(string value)
        {
            if (string.IsNullOrWhiteSpace(value))
            {
                FilteredTags = new ObservableCollection<TagSelectionItem>(_allTags);
            }
            else
            {
                var filtered = _allTags.Where(t =>
                    t.Name.Contains(value, StringComparison.OrdinalIgnoreCase));
                FilteredTags = new ObservableCollection<TagSelectionItem>(filtered);
            }
        }

        [RelayCommand]
        private void SelectAll()
        {
            foreach (var file in MediaFiles)
            {
                file.IsSelected = true;
            }
            UpdateSelectedCount();
        }

        [RelayCommand]
        private void SelectNone()
        {
            foreach (var file in MediaFiles)
            {
                file.IsSelected = false;
            }
            UpdateSelectedCount();
        }

        [RelayCommand]
        private void InvertSelection()
        {
            foreach (var file in MediaFiles)
            {
                file.IsSelected = !file.IsSelected;
            }
            UpdateSelectedCount();
        }

        [RelayCommand]
        private async Task AutoTagByDate()
        {
            // Create tags based on year/month
            var dateTags = new List<string>();
            foreach (var file in MediaFiles.Where(f => f.IsSelected))
            {
                var year = file.MediaFile.DateAdded.Year.ToString();
                var month = file.MediaFile.DateAdded.ToString("MMMM");
                dateTags.Add(year);
                dateTags.Add($"{month} {year}");
            }

            foreach (var tagName in dateTags.Distinct())
            {
                var existingTag = _allTags.FirstOrDefault(t => t.Name.Equals(tagName, StringComparison.OrdinalIgnoreCase));
                if (existingTag == null)
                {
                    var newTag = await _tagService.CreateTagAsync(tagName, "#4A90E2", $"Auto-created date tag");
                    var tagItem = new TagSelectionItem
                    {
                        Tag = newTag,
                        Name = newTag.Name,
                        Color = newTag.Color,
                        IsSelected = true
                    };
                    _allTags.Add(tagItem);
                    FilteredTags.Add(tagItem);
                }
                else
                {
                    existingTag.IsSelected = true;
                }
            }
            UpdateSummary();
        }

        [RelayCommand]
        private async Task AutoTagByFolder()
        {
            // Create tags based on folder structure
            var folderTags = new HashSet<string>();
            foreach (var file in MediaFiles.Where(f => f.IsSelected))
            {
                var directory = System.IO.Path.GetDirectoryName(file.MediaFile.OriginalPath);
                if (!string.IsNullOrEmpty(directory))
                {
                    var folderName = System.IO.Path.GetFileName(directory);
                    if (!string.IsNullOrEmpty(folderName))
                    {
                        folderTags.Add(folderName);
                    }
                }
            }

            foreach (var tagName in folderTags)
            {
                var existingTag = _allTags.FirstOrDefault(t => t.Name.Equals(tagName, StringComparison.OrdinalIgnoreCase));
                if (existingTag == null)
                {
                    var newTag = await _tagService.CreateTagAsync(tagName, "#E74C3C", $"Auto-created folder tag");
                    var tagItem = new TagSelectionItem
                    {
                        Tag = newTag,
                        Name = newTag.Name,
                        Color = newTag.Color,
                        IsSelected = true
                    };
                    _allTags.Add(tagItem);
                    FilteredTags.Add(tagItem);
                }
                else
                {
                    existingTag.IsSelected = true;
                }
            }
            UpdateSummary();
        }

        [RelayCommand]
        private async Task AutoTagByType()
        {
            // Create tags based on media type
            var typeTags = new Dictionary<MediaType, string>
            {
                { MediaType.Image, "Photos" },
                { MediaType.Video, "Videos" },
                { MediaType.Audio, "Audio" }
            };

            var usedTypes = MediaFiles
                .Where(f => f.IsSelected)
                .Select(f => f.MediaFile.MediaType)
                .Distinct();

            foreach (var type in usedTypes)
            {
                if (typeTags.TryGetValue(type, out var tagName))
                {
                    var existingTag = _allTags.FirstOrDefault(t => t.Name.Equals(tagName, StringComparison.OrdinalIgnoreCase));
                    if (existingTag == null)
                    {
                        var color = type switch
                        {
                            MediaType.Image => "#27AE60",
                            MediaType.Video => "#8E44AD",
                            MediaType.Audio => "#F39C12",
                            _ => "#95A5A6"
                        };

                        var newTag = await _tagService.CreateTagAsync(tagName, color, $"Media type: {type}");
                        var tagItem = new TagSelectionItem
                        {
                            Tag = newTag,
                            Name = newTag.Name,
                            Color = newTag.Color,
                            IsSelected = true
                        };
                        _allTags.Add(tagItem);
                        FilteredTags.Add(tagItem);
                    }
                    else
                    {
                        existingTag.IsSelected = true;
                    }
                }
            }
            UpdateSummary();
        }

        [RelayCommand]
        private void QuickAddTag(Tag tag)
        {
            var tagItem = _allTags.FirstOrDefault(t => t.Tag.Id == tag.Id);
            if (tagItem != null)
            {
                tagItem.IsSelected = true;
                UpdateSummary();
            }
        }

        [RelayCommand]
        private async Task CreateNewTag()
        {
            // This would open the tag creation dialog
            // For now, we'll create a simple tag
            var newTagName = $"New Tag {DateTime.Now:HHmmss}";
            var newTag = await _tagService.CreateTagAsync(newTagName, "#3498DB");

            var tagItem = new TagSelectionItem
            {
                Tag = newTag,
                Name = newTag.Name,
                Color = newTag.Color,
                IsSelected = true
            };
            _allTags.Add(tagItem);
            FilteredTags.Insert(0, tagItem);
            UpdateSummary();
        }

        [RelayCommand]
        private async Task ApplyTags()
        {
            var selectedTags = _allTags.Where(t => t.IsSelected).Select(t => t.Tag).ToList();
            var selectedFiles = MediaFiles.Where(f => f.IsSelected).Select(f => f.MediaFile).ToList();

            foreach (var file in selectedFiles)
            {
                foreach (var tag in selectedTags)
                {
                    await _mediaFileService.AddTagToMediaFileAsync(file.Id, tag.Id);
                }
            }

            // Save recent tags
            if (RememberSettings && selectedTags.Any())
            {
                await _settingsService.SaveRecentTagIdsAsync(selectedTags.Select(t => t.Id).ToList());
            }
        }

        [RelayCommand]
        private void Skip()
        {
            // Close dialog without applying tags
        }

        private void UpdateSelectedCount()
        {
            SelectedCount = MediaFiles.Count(f => f.IsSelected);
        }

        private void UpdateSummary()
        {
            var selectedTagCount = _allTags.Count(t => t.IsSelected);
            var selectedFileCount = MediaFiles.Count(f => f.IsSelected);

            if (selectedTagCount == 0)
            {
                Summary = "No tags selected";
                CanApplyTags = false;
            }
            else if (selectedFileCount == 0)
            {
                Summary = "No files selected";
                CanApplyTags = false;
            }
            else
            {
                Summary = $"Apply {selectedTagCount} tag(s) to {selectedFileCount} file(s)";
                CanApplyTags = true;
            }
        }

        private string FormatFileSize(long bytes)
        {
            string[] sizes = { "B", "KB", "MB", "GB" };
            int order = 0;
            double size = bytes;
            while (size >= 1024 && order < sizes.Length - 1)
            {
                order++;
                size /= 1024;
            }
            return $"{size:0.##} {sizes[order]}";
        }
    }

    public class BulkMediaFileItem : ObservableObject
    {
        public MediaFile MediaFile { get; set; } = null!;

        private bool _isSelected;
        public bool IsSelected
        {
            get => _isSelected;
            set => SetProperty(ref _isSelected, value);
        }

        public string FileName { get; set; } = string.Empty;
        public string FileInfo { get; set; } = string.Empty;
        public string? ThumbnailPath { get; set; }
        public ObservableCollection<Tag> CurrentTags { get; set; } = new();
    }

    public class TagSelectionItem : ObservableObject
    {
        public Tag Tag { get; set; } = null!;
        public string Name { get; set; } = string.Empty;
        public string Color { get; set; } = "#000000";
        public int UsageCount { get; set; }

        private bool _isSelected;
        public bool IsSelected
        {
            get => _isSelected;
            set => SetProperty(ref _isSelected, value);
        }
    }
}