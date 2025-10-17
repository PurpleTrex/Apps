using CommunityToolkit.Mvvm.ComponentModel;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using WindowsVault.Models;
using WindowsVault.Services;

namespace WindowsVault.ViewModels
{
    public partial class MediaDetailViewModel : ObservableObject
    {
        private readonly IMediaFileService _mediaFileService;
        private readonly ITagService _tagService;

        [ObservableProperty]
        private MediaFile? _mediaFile;

        [ObservableProperty]
        private ObservableCollection<Tag> _availableTags = new();

        [ObservableProperty]
        private ObservableCollection<Tag> _assignedTags = new();

        [ObservableProperty]
        private Tag? _selectedAvailableTag;

        [ObservableProperty]
        private Tag? _selectedAssignedTag;

        public MediaDetailViewModel(IMediaFileService mediaFileService, ITagService tagService)
        {
            _mediaFileService = mediaFileService;
            _tagService = tagService;
        }

        public async Task InitializeAsync(MediaFile mediaFile)
        {
            MediaFile = mediaFile;
            await LoadTagsAsync();
        }

        private async Task LoadTagsAsync()
        {
            if (MediaFile == null) return;

            // Get all tags
            var allTags = await _tagService.GetAllTagsAsync();
            
            // Get currently assigned tags
            var mediaFileWithTags = await _mediaFileService.GetMediaFileByIdAsync(MediaFile.Id);
            var assignedTagIds = mediaFileWithTags?.Tags.Select(t => t.Id).ToHashSet() ?? new HashSet<int>();

            // Separate into assigned and available
            AssignedTags.Clear();
            AvailableTags.Clear();

            foreach (var tag in allTags)
            {
                if (assignedTagIds.Contains(tag.Id))
                {
                    AssignedTags.Add(tag);
                }
                else
                {
                    AvailableTags.Add(tag);
                }
            }
        }

        public async Task AssignTagAsync()
        {
            if (SelectedAvailableTag == null || MediaFile == null) return;

            var success = await _mediaFileService.AddTagToMediaFileAsync(MediaFile.Id, SelectedAvailableTag.Id);
            
            if (success)
            {
                // Clear selection state
                SelectedAvailableTag.IsSelected = false;
                
                // Move tag from available to assigned
                var tagToMove = SelectedAvailableTag;
                AvailableTags.Remove(tagToMove);
                AssignedTags.Add(tagToMove);
                SelectedAvailableTag = null;
                
                // Force UI refresh
                OnPropertyChanged(nameof(AvailableTags));
                OnPropertyChanged(nameof(AssignedTags));
            }
        }

        public async Task RemoveTagAsync()
        {
            if (SelectedAssignedTag == null || MediaFile == null) return;

            var success = await _mediaFileService.RemoveTagFromMediaFileAsync(MediaFile.Id, SelectedAssignedTag.Id);
            
            if (success)
            {
                // Clear selection state
                SelectedAssignedTag.IsSelected = false;
                
                // Move tag from assigned to available
                var tagToMove = SelectedAssignedTag;
                AssignedTags.Remove(tagToMove);
                AvailableTags.Add(tagToMove);
                SelectedAssignedTag = null;
                
                // Force UI refresh
                OnPropertyChanged(nameof(AvailableTags));
                OnPropertyChanged(nameof(AssignedTags));
            }
        }

        public async Task ToggleFavoriteAsync()
        {
            if (MediaFile == null) return;

            MediaFile.IsFavorite = !MediaFile.IsFavorite;
            await _mediaFileService.SetFavoriteAsync(MediaFile.Id, MediaFile.IsFavorite);
            OnPropertyChanged(nameof(MediaFile));
        }
    }
}
