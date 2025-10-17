using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using Microsoft.Win32;
using Microsoft.Extensions.DependencyInjection;
using WindowsVault.Models;
using WindowsVault.Services;
using WindowsVault.Views;

namespace WindowsVault.ViewModels
{
    public partial class MainViewModel : ObservableObject
    {
        private readonly IMediaFileService _mediaFileService;
        private readonly ITagService _tagService;
        private readonly ISettingsService _settingsService;
        private readonly IServiceProvider _serviceProvider;

        [ObservableProperty]
        private ObservableCollection<MediaFile> _mediaFiles = new();

        [ObservableProperty]
        private ObservableCollection<MediaFile> _filteredMediaFiles = new();

        [ObservableProperty]
        private ObservableCollection<Tag> _tags = new();

        [ObservableProperty]
        private ObservableCollection<Tag> _selectedTags = new();

        [ObservableProperty]
        private MediaFile? _selectedMediaFile;

        [ObservableProperty]
        private ObservableCollection<MediaFile> _selectedMediaFiles = new();

        [ObservableProperty]
        private bool _isSelectionMode = false;

        partial void OnIsSelectionModeChanged(bool value)
        {
            // Update all media files with selection mode state
            foreach (var file in FilteredMediaFiles)
            {
                file.IsSelectionMode = value;
                if (!value)
                {
                    file.IsSelected = false;
                }
            }
        }

        [ObservableProperty]
        private string _searchQuery = string.Empty;

        [ObservableProperty]
        private string _statusText = "Ready";

        [ObservableProperty]
        private bool _isLoading;

        public MainViewModel(
            IMediaFileService mediaFileService,
            ITagService tagService,
            ISettingsService settingsService,
            IServiceProvider serviceProvider)
        {
            _mediaFileService = mediaFileService;
            _tagService = tagService;
            _settingsService = settingsService;
            _serviceProvider = serviceProvider;
        }

        public async Task InitializeAsync()
        {
            IsLoading = true;
            StatusText = "Loading media files...";

            try
            {
                await LoadMediaFilesAsync();
                await LoadTagsAsync();
                ApplyFilters();
                StatusText = $"Loaded {MediaFiles.Count} media files";
            }
            catch (Exception ex)
            {
                StatusText = $"Error loading data: {ex.Message}";
            }
            finally
            {
                IsLoading = false;
            }
        }

        [RelayCommand]
        private async Task AddMediaAsync()
        {
            try
            {
                StatusText = "Opening file dialog...";
                
                var openFileDialog = new OpenFileDialog
                {
                    Title = "Select Media Files",
                    Multiselect = true,
                    Filter = "All Supported|*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.tiff;*.webp;*.mp4;*.avi;*.mkv;*.mov;*.wmv;*.flv;*.webm;*.mp3;*.wav;*.flac;*.aac;*.ogg|" +
                            "Images|*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.tiff;*.webp|" +
                            "Videos|*.mp4;*.avi;*.mkv;*.mov;*.wmv;*.flv;*.webm|" +
                            "Audio|*.mp3;*.wav;*.flac;*.aac;*.ogg|" +
                            "All Files|*.*"
                };

                var dialogResult = openFileDialog.ShowDialog();
                System.Diagnostics.Debug.WriteLine($"File dialog result: {dialogResult}");

                if (dialogResult == true)
                {
                    IsLoading = true;
                    var addedCount = 0;
                    var skippedCount = 0;
                    var addedMediaFiles = new List<MediaFile>();

                    foreach (var fileName in openFileDialog.FileNames)
                    {
                        try
                        {
                            StatusText = $"Adding {Path.GetFileName(fileName)}...";
                            var mediaFile = await _mediaFileService.AddMediaFileAsync(fileName);
                            MediaFiles.Add(mediaFile);
                            addedMediaFiles.Add(mediaFile);
                            addedCount++;
                        }
                        catch (InvalidOperationException)
                        {
                            skippedCount++; // Duplicate file
                        }
                        catch (Exception ex)
                        {
                            StatusText = $"Error adding {Path.GetFileName(fileName)}: {ex.Message}";
                            System.Diagnostics.Debug.WriteLine($"AddMedia Error: {ex}");
                        }
                    }

                    ApplyFilters();
                    StatusText = $"Added {addedCount} files, skipped {skippedCount} duplicates";

                    // Prompt for tag assignment if files were added
                    if (addedMediaFiles.Count > 0)
                    {
                        await PromptForTagAssignmentAsync(addedMediaFiles);
                    }
                }
                else
                {
                    StatusText = "File selection cancelled";
                }
            }
            catch (Exception ex)
            {
                StatusText = $"Error opening file dialog: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"AddMediaAsync Error: {ex}");
            }
            finally
            {
                IsLoading = false;
            }
        }

        [RelayCommand]
        private async Task ImportFolderAsync()
        {
            var folderDialog = new OpenFileDialog
            {
                Title = "Select Folder to Import",
                CheckFileExists = false,
                CheckPathExists = true,
                ValidateNames = false,
                FileName = "Select Folder"
            };

            if (folderDialog.ShowDialog() == true)
            {
                var folderPath = System.IO.Path.GetDirectoryName(folderDialog.FileName);
                if (string.IsNullOrEmpty(folderPath)) return;
                IsLoading = true;
                StatusText = "Importing folder...";

                try
                {
                    var progress = new Progress<string>(message => StatusText = message);
                    await _mediaFileService.ImportDirectoryAsync(folderPath, progress);
                    await LoadMediaFilesAsync();
                    ApplyFilters();
                    StatusText = "Folder import completed";
                }
                catch (Exception ex)
                {
                    StatusText = $"Error importing folder: {ex.Message}";
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        [RelayCommand]
        private async Task ShowAllMediaAsync()
        {
            await LoadMediaFilesAsync();
            ApplyFilters();
        }

        [RelayCommand]
        private async Task ShowImagesAsync()
        {
            var imageFiles = await _mediaFileService.GetMediaFilesByTypeAsync(MediaType.Image);
            MediaFiles.Clear();
            foreach (var file in imageFiles)
                MediaFiles.Add(file);
            ApplyFilters();
        }

        [RelayCommand]
        private async Task ShowVideosAsync()
        {
            var videoFiles = await _mediaFileService.GetMediaFilesByTypeAsync(MediaType.Video);
            MediaFiles.Clear();
            foreach (var file in videoFiles)
                MediaFiles.Add(file);
            ApplyFilters();
        }

        [RelayCommand]
        private async Task ShowAudioAsync()
        {
            var audioFiles = await _mediaFileService.GetMediaFilesByTypeAsync(MediaType.Audio);
            MediaFiles.Clear();
            foreach (var file in audioFiles)
                MediaFiles.Add(file);
            ApplyFilters();
        }

        [RelayCommand]
        private async Task ShowFavoritesAsync()
        {
            var favoriteFiles = await _mediaFileService.GetFavoriteMediaFilesAsync();
            MediaFiles.Clear();
            foreach (var file in favoriteFiles)
                MediaFiles.Add(file);
            ApplyFilters();
        }

        [RelayCommand]
        private void ToggleTagFilter(Tag tag)
        {
            if (SelectedTags.Contains(tag))
            {
                SelectedTags.Remove(tag);
            }
            else
            {
                SelectedTags.Add(tag);
            }
            ApplyFilters();
        }

        [RelayCommand]
        private async Task AddTagAsync()
        {
            // Open tag management window instead of using InputBox
            await ManageTags();
        }

        [RelayCommand]
        private async Task ManageTags()
        {
            try
            {
                StatusText = "Opening tag management...";
                var tagManagementWindow = _serviceProvider.GetRequiredService<TagManagementWindow>();
                tagManagementWindow.Owner = Application.Current.MainWindow;
                
                var result = tagManagementWindow.ShowDialog();
                
                // Refresh tags after managing them
                StatusText = "Refreshing tags...";
                System.Diagnostics.Debug.WriteLine("Refreshing tags after tag management closed...");
                await LoadTagsAsync();
                System.Diagnostics.Debug.WriteLine($"Tags loaded: {Tags.Count} tags");
                StatusText = $"Ready - {Tags.Count} tags loaded";
            }
            catch (Exception ex)
            {
                StatusText = $"Error opening tag management: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"ManageTags Error: {ex}");
            }
        }

        [RelayCommand]
        private async Task ShowSettings()
        {
            try
            {
                StatusText = "Opening settings...";
                var settingsWindow = _serviceProvider.GetRequiredService<SettingsWindow>();
                settingsWindow.Owner = Application.Current.MainWindow;
                settingsWindow.ShowDialog();
                StatusText = "Ready";
            }
            catch (Exception ex)
            {
                StatusText = $"Error opening settings: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"ShowSettings Error: {ex}");
            }
        }

        [RelayCommand]
        private async Task OpenMediaFile(MediaFile mediaFile)
        {
            try
            {
                var processStartInfo = new ProcessStartInfo
                {
                    FileName = mediaFile.VaultPath,
                    UseShellExecute = true
                };
                Process.Start(processStartInfo);
            }
            catch (Exception ex)
            {
                StatusText = $"Error opening file: {ex.Message}";
            }
        }

        public void SortMediaFiles(string sortBy, bool ascending)
        {
            var sorted = sortBy switch
            {
                "Name" => ascending ? 
                    MediaFiles.OrderBy(m => m.FileName) : 
                    MediaFiles.OrderByDescending(m => m.FileName),
                "Size" => ascending ? 
                    MediaFiles.OrderBy(m => m.FileSizeBytes) : 
                    MediaFiles.OrderByDescending(m => m.FileSizeBytes),
                "Rating" => ascending ? 
                    MediaFiles.OrderBy(m => m.Rating) : 
                    MediaFiles.OrderByDescending(m => m.Rating),
                "Date Modified" => ascending ? 
                    MediaFiles.OrderBy(m => m.DateModified) : 
                    MediaFiles.OrderByDescending(m => m.DateModified),
                _ => ascending ? 
                    MediaFiles.OrderBy(m => m.DateAdded) : 
                    MediaFiles.OrderByDescending(m => m.DateAdded)
            };

            MediaFiles.Clear();
            foreach (var item in sorted)
                MediaFiles.Add(item);
            
            ApplyFilters();
        }

        partial void OnSearchQueryChanged(string value)
        {
            ApplyFilters();
        }

        private async Task LoadMediaFilesAsync()
        {
            var mediaFiles = await _mediaFileService.GetAllMediaFilesAsync();
            MediaFiles.Clear();
            foreach (var mediaFile in mediaFiles)
            {
                mediaFile.IsSelectionMode = IsSelectionMode;
                MediaFiles.Add(mediaFile);
            }
        }

        private async Task LoadTagsAsync()
        {
            System.Diagnostics.Debug.WriteLine("MainViewModel: Loading tags...");
            var tags = await _tagService.GetAllTagsAsync();
            System.Diagnostics.Debug.WriteLine($"MainViewModel: Retrieved {tags.Count()} tags from service");
            Tags.Clear();
            foreach (var tag in tags)
            {
                System.Diagnostics.Debug.WriteLine($"MainViewModel: Adding tag '{tag.Name}' to collection");
                Tags.Add(tag);
            }
            System.Diagnostics.Debug.WriteLine($"MainViewModel: Tags collection now has {Tags.Count} items");
        }

        private void ApplyFilters()
        {
            var filtered = MediaFiles.AsEnumerable();

            // Apply search filter
            if (!string.IsNullOrWhiteSpace(SearchQuery))
            {
                var query = SearchQuery.ToLower();
                filtered = filtered.Where(m => 
                    m.FileName.ToLower().Contains(query) ||
                    (m.Description != null && m.Description.ToLower().Contains(query)) ||
                    m.Tags.Any(t => t.Name.ToLower().Contains(query)));
            }

            // Apply tag filters
            if (SelectedTags.Any())
            {
                var selectedTagIds = SelectedTags.Select(t => t.Id).ToHashSet();
                filtered = filtered.Where(m => 
                    m.Tags.Any(t => selectedTagIds.Contains(t.Id)));
            }

            FilteredMediaFiles.Clear();
            foreach (var item in filtered)
            {
                item.IsSelectionMode = IsSelectionMode;
                FilteredMediaFiles.Add(item);
            }
        }

        private async Task PromptForTagAssignmentAsync(List<MediaFile> mediaFiles)
        {
            try
            {
                // Get all available tags
                var allTags = await _tagService.GetAllTagsAsync();
                
                if (!allTags.Any())
                {
                    StatusText = "No tags available. Create tags first in Tag Management.";
                    return;
                }

                // Show tag selection dialog
                var tagDialog = new Views.TagSelectionDialog(allTags);
                var result = tagDialog.ShowDialog();

                if (result == true && tagDialog.TagsAssigned)
                {
                    var selectedTags = tagDialog.GetSelectedTags();
                    
                    if (selectedTags.Any())
                    {
                        // Assign selected tags to all imported media files
                        foreach (var mediaFile in mediaFiles)
                        {
                            foreach (var tag in selectedTags)
                            {
                                await _mediaFileService.AddTagToMediaFileAsync(mediaFile.Id, tag.Id);
                            }
                        }

                        // Refresh the view
                        await LoadMediaFilesAsync();
                        StatusText = $"Tags assigned to {mediaFiles.Count} file(s)";
                    }
                }
            }
            catch (Exception ex)
            {
                StatusText = $"Error assigning tags: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"PromptForTagAssignment Error: {ex}");
            }
        }

        [RelayCommand]
        private void ToggleSelectionMode()
        {
            IsSelectionMode = !IsSelectionMode;
            if (!IsSelectionMode)
            {
                SelectedMediaFiles.Clear();
            }
            StatusText = IsSelectionMode ? "Selection Mode: Click items to select" : "Ready";
        }

        [RelayCommand]
        private void ToggleMediaSelection(MediaFile mediaFile)
        {
            if (SelectedMediaFiles.Contains(mediaFile))
            {
                SelectedMediaFiles.Remove(mediaFile);
            }
            else
            {
                SelectedMediaFiles.Add(mediaFile);
            }
            StatusText = $"{SelectedMediaFiles.Count} item(s) selected";
        }

        [RelayCommand]
        private void SelectAll()
        {
            SelectedMediaFiles.Clear();
            foreach (var file in FilteredMediaFiles)
            {
                SelectedMediaFiles.Add(file);
            }
            StatusText = $"All {SelectedMediaFiles.Count} items selected";
        }

        [RelayCommand]
        private void ClearSelection()
        {
            SelectedMediaFiles.Clear();
            StatusText = "Selection cleared";
        }

        [RelayCommand]
        private async Task DeleteSelectedAsync()
        {
            if (SelectedMediaFiles.Count == 0)
            {
                await Helpers.ModernDialog.ShowErrorAsync("No items selected for deletion.");
                return;
            }

            var result = await Helpers.ModernDialog.ShowAsync(
                "Delete Selected Media",
                $"Are you sure you want to delete {SelectedMediaFiles.Count} item(s)? This action cannot be undone.",
                "Delete",
                "Cancel");

            if (result == ModernWpf.Controls.ContentDialogResult.Primary)
            {
                IsLoading = true;
                StatusText = $"Deleting {SelectedMediaFiles.Count} items...";
                
                var idsToDelete = SelectedMediaFiles.Select(m => m.Id).ToList();
                
                try
                {
                    var deletedCount = await _mediaFileService.DeleteMediaFilesAsync(idsToDelete);
                    
                    SelectedMediaFiles.Clear();
                    IsSelectionMode = false;
                    await LoadMediaFilesAsync();
                    IsLoading = false;
                    StatusText = $"Deleted {deletedCount} items successfully";
                    
                    await Helpers.ModernDialog.ShowSuccessAsync($"Successfully deleted {deletedCount} item(s)!");
                }
                catch (Exception ex)
                {
                    IsLoading = false;
                    StatusText = $"Error deleting items: {ex.Message}";
                    await Helpers.ModernDialog.ShowErrorAsync($"Error deleting items: {ex.Message}");
                    System.Diagnostics.Debug.WriteLine($"Error deleting: {ex}");
                }
            }
        }

        [RelayCommand]
        private async Task AssignTagsToSelectedAsync()
        {
            if (SelectedMediaFiles.Count == 0)
            {
                await Helpers.ModernDialog.ShowErrorAsync("No items selected.");
                return;
            }

            await PromptForTagAssignmentAsync(SelectedMediaFiles.ToList());
            SelectedMediaFiles.Clear();
            IsSelectionMode = false;
        }
    }
}