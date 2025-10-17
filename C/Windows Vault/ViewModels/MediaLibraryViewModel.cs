using CommunityToolkit.Mvvm.ComponentModel;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using WindowsVault.Models;
using WindowsVault.Services;

namespace WindowsVault.ViewModels
{
    public partial class MediaLibraryViewModel : ObservableObject
    {
        private readonly IMediaFileService _mediaFileService;
        private readonly ITagService _tagService;

        [ObservableProperty]
        private ObservableCollection<MediaFile> _mediaFiles = new();

        [ObservableProperty]
        private MediaFile? _selectedMediaFile;

        [ObservableProperty]
        private bool _isLoading;

        public MediaLibraryViewModel(IMediaFileService mediaFileService, ITagService tagService)
        {
            _mediaFileService = mediaFileService;
            _tagService = tagService;
        }

        public async Task LoadMediaFilesAsync()
        {
            IsLoading = true;
            try
            {
                var files = await _mediaFileService.GetAllMediaFilesAsync();
                MediaFiles.Clear();
                foreach (var file in files)
                    MediaFiles.Add(file);
            }
            finally
            {
                IsLoading = false;
            }
        }
    }
}