using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using System.Windows;
using WindowsVault.Models;
using WindowsVault.Services;
using WindowsVault.Helpers;
using System;
using System.Linq;

namespace WindowsVault.ViewModels
{
    public partial class TagManagementViewModel : ObservableObject
    {
        private readonly ITagService _tagService;

        [ObservableProperty]
        private ObservableCollection<Tag> _tags = new();

        [ObservableProperty]
        private Tag? _selectedTag;

        [ObservableProperty]
        private bool _isLoading;

        [ObservableProperty]
        private string _newTagName = string.Empty;

        [ObservableProperty]
        private string _newTagColor = "#0078D4";

        [ObservableProperty]
        private string _selectedTagName = string.Empty;

        [ObservableProperty]
        private string _selectedTagColor = "#0078D4";

        [ObservableProperty]
        private string _statusMessage = string.Empty;

        public TagManagementViewModel(ITagService tagService)
        {
            _tagService = tagService;
        }

        public async Task InitializeAsync()
        {
            await LoadTagsAsync();
        }

        public async Task LoadTagsAsync()
        {
            IsLoading = true;
            try
            {
                var tags = await _tagService.GetAllTagsAsync();
                Tags.Clear();
                
                foreach (var tag in tags)
                {
                    Tags.Add(tag);
                }
            }
            catch (Exception ex)
            {
                StatusMessage = $"Error loading tags: {ex.Message}";
            }
            finally
            {
                IsLoading = false;
            }
        }

        [RelayCommand]
        private async Task CreateTagAsync()
        {
            try
            {
                StatusMessage = "Creating tag...";
                
                if (string.IsNullOrWhiteSpace(NewTagName))
                {
                    StatusMessage = "Tag name cannot be empty.";
                    return;
                }

                if (Tags.Any(t => t.Name.Equals(NewTagName, StringComparison.OrdinalIgnoreCase)))
                {
                    StatusMessage = "A tag with this name already exists.";
                    return;
                }

                IsLoading = true;
                
                System.Diagnostics.Debug.WriteLine($"Creating tag: {NewTagName.Trim()}");
                var newTag = await _tagService.CreateTagAsync(NewTagName.Trim(), NewTagColor);
                System.Diagnostics.Debug.WriteLine($"Tag created with ID: {newTag.Id}");
                
                await LoadTagsAsync();
                System.Diagnostics.Debug.WriteLine($"Tags reloaded: {Tags.Count} tags in TagManagement window");
                
                NewTagName = string.Empty;
                NewTagColor = "#0078D4";
                StatusMessage = $"Tag '{newTag.Name}' created successfully!";
            }
            catch (Exception ex)
            {
                StatusMessage = $"Error creating tag: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"CreateTag Error: {ex}");
            }
            finally
            {
                IsLoading = false;
            }
        }

        [RelayCommand]
        private async Task UpdateTagAsync()
        {
            if (SelectedTag == null)
            {
                StatusMessage = "No tag selected.";
                return;
            }

            if (string.IsNullOrWhiteSpace(SelectedTagName))
            {
                StatusMessage = "Tag name cannot be empty.";
                return;
            }

            try
            {
                SelectedTag.Name = SelectedTagName;
                SelectedTag.Color = SelectedTagColor;
                await _tagService.UpdateTagAsync(SelectedTag);
                await LoadTagsAsync();
                
                StatusMessage = "Tag updated successfully.";
            }
            catch (Exception ex)
            {
                StatusMessage = $"Error updating tag: {ex.Message}";
            }
        }

        [RelayCommand]
        private async Task DeleteTagAsync()
        {
            if (SelectedTag == null)
            {
                StatusMessage = "No tag selected.";
                return;
            }

            var result = await ModernDialog.ShowDeleteConfirmAsync(SelectedTag.Name, "tag");

            if (result == ModernWpf.Controls.ContentDialogResult.Primary)
            {
                try
                {
                    await _tagService.DeleteTagAsync(SelectedTag.Id);
                    await LoadTagsAsync();
                    SelectedTag = null;
                    StatusMessage = "Tag deleted successfully.";
                }
                catch (Exception ex)
                {
                    StatusMessage = $"Error deleting tag: {ex.Message}";
                    await ModernDialog.ShowErrorAsync($"Error deleting tag: {ex.Message}");
                }
            }
        }

        partial void OnSelectedTagChanged(Tag? value)
        {
            if (value != null)
            {
                SelectedTagName = value.Name;
                SelectedTagColor = value.Color;
            }
            else
            {
                SelectedTagName = string.Empty;
                SelectedTagColor = "#0078D4";
            }
        }
    }
}