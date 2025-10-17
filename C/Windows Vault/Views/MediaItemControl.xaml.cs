using System;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Extensions.DependencyInjection;
using WindowsVault.Models;
using WindowsVault.Helpers;

namespace WindowsVault.Views
{
    public partial class MediaItemControl : UserControl
    {
        public static readonly DependencyProperty ThumbnailSizeProperty =
            DependencyProperty.Register("ThumbnailSize", typeof(double), typeof(MediaItemControl), 
                new PropertyMetadata(200.0));

        public double ThumbnailSize
        {
            get { return (double)GetValue(ThumbnailSizeProperty); }
            set { SetValue(ThumbnailSizeProperty, value); }
        }

        public MediaItemControl()
        {
            InitializeComponent();
        }

        private void MediaItem_Click(object sender, System.Windows.Input.MouseButtonEventArgs e)
        {
            if (DataContext is MediaFile mediaFile)
            {
                // Get MainWindow's ViewModel using reflection
                var mainWindow = Window.GetWindow(this) as MainWindow;
                if (mainWindow != null)
                {
                    var viewModelField = mainWindow.GetType().GetField("_viewModel", 
                        System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                    var viewModel = viewModelField?.GetValue(mainWindow);
                    
                    if (viewModel != null)
                    {
                        // Get SelectedMediaFiles collection
                        var selectedFilesProperty = viewModel.GetType().GetProperty("SelectedMediaFiles");
                        var selectedFiles = selectedFilesProperty?.GetValue(viewModel) as System.Collections.ObjectModel.ObservableCollection<MediaFile>;
                        
                        if (selectedFiles != null)
                        {
                            bool isCtrlPressed = System.Windows.Input.Keyboard.IsKeyDown(System.Windows.Input.Key.LeftCtrl) || 
                                               System.Windows.Input.Keyboard.IsKeyDown(System.Windows.Input.Key.RightCtrl);
                            
                            if (isCtrlPressed)
                            {
                                // Toggle selection
                                if (mediaFile.IsSelected)
                                {
                                    mediaFile.IsSelected = false;
                                    selectedFiles.Remove(mediaFile);
                                }
                                else
                                {
                                    mediaFile.IsSelected = true;
                                    selectedFiles.Add(mediaFile);
                                }
                            }
                            else
                            {
                                // Single select - clear others
                                foreach (var file in selectedFiles.ToArray())
                                {
                                    file.IsSelected = false;
                                }
                                selectedFiles.Clear();
                                
                                mediaFile.IsSelected = true;
                                selectedFiles.Add(mediaFile);
                            }
                        }
                    }
                }
            }
        }

        private void OpenFile_Click(object sender, RoutedEventArgs e)
        {
            if (DataContext is MediaFile mediaFile)
            {
                try
                {
                    Process.Start(new ProcessStartInfo
                    {
                        FileName = mediaFile.VaultPath,
                        UseShellExecute = true
                    });
                }
                catch (Exception ex)
                {
                    ModernDialog.ShowErrorAsync($"Error opening file: {ex.Message}");
                }
            }
        }

        private async void ManageTags_Click(object sender, RoutedEventArgs e)
        {
            var mainWindow = Window.GetWindow(this) as MainWindow;
            if (mainWindow != null)
            {
                var viewModelField = mainWindow.GetType().GetField("_viewModel", 
                    System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                var viewModel = viewModelField?.GetValue(mainWindow);
                
                if (viewModel != null)
                {
                    // Get SelectedMediaFiles collection
                    var selectedFilesProperty = viewModel.GetType().GetProperty("SelectedMediaFiles");
                    var selectedFiles = selectedFilesProperty?.GetValue(viewModel) as System.Collections.ObjectModel.ObservableCollection<MediaFile>;
                    
                    // Get service provider
                    var serviceProviderField = mainWindow.GetType().GetField("_serviceProvider", 
                        System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                    var serviceProvider = serviceProviderField?.GetValue(mainWindow) as IServiceProvider;
                    
                    if (selectedFiles != null && selectedFiles.Count > 0 && serviceProvider != null)
                    {
                        // Manage tags for multiple files - call AssignTagsToSelectedAsync
                        var assignMethod = viewModel.GetType().GetMethod("AssignTagsToSelectedAsync", 
                            System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                        if (assignMethod != null)
                        {
                            await (Task)assignMethod.Invoke(viewModel, null);
                        }
                    }
                    else if (DataContext is MediaFile mediaFile && serviceProvider != null)
                    {
                        // Single file - open detail window
                        var mediaDetailWindow = serviceProvider.GetRequiredService<MediaDetailWindow>();
                        await mediaDetailWindow.InitializeAsync(mediaFile);
                        mediaDetailWindow.Owner = mainWindow;
                        mediaDetailWindow.ShowDialog();
                    }
                }
            }
        }

        private async void ToggleFavorite_Click(object sender, RoutedEventArgs e)
        {
            if (DataContext is MediaFile mediaFile)
            {
                var mainWindow = Window.GetWindow(this) as MainWindow;
                if (mainWindow?.DataContext is ViewModels.MainViewModel viewModel)
                {
                    mediaFile.IsFavorite = !mediaFile.IsFavorite;
                    
                    // Call SetFavoriteAsync via reflection
                    var mediaFileServiceField = viewModel.GetType().GetField("_mediaFileService",
                        System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                    
                    if (mediaFileServiceField != null)
                    {
                        var mediaFileService = mediaFileServiceField.GetValue(viewModel) as Services.IMediaFileService;
                        if (mediaFileService != null)
                        {
                            await mediaFileService.SetFavoriteAsync(mediaFile.Id, mediaFile.IsFavorite);
                            await ModernDialog.ShowSuccessAsync(mediaFile.IsFavorite ? 
                                "Added to favorites!" : "Removed from favorites!");
                        }
                    }
                }
            }
        }

        private async void Delete_Click(object sender, RoutedEventArgs e)
        {
            var mainWindow = Window.GetWindow(this) as MainWindow;
            if (mainWindow != null)
            {
                var viewModelField = mainWindow.GetType().GetField("_viewModel", 
                    System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                var viewModel = viewModelField?.GetValue(mainWindow);
                
                if (viewModel != null)
                {
                    // Get SelectedMediaFiles collection
                    var selectedFilesProperty = viewModel.GetType().GetProperty("SelectedMediaFiles");
                    var selectedFiles = selectedFilesProperty?.GetValue(viewModel) as System.Collections.ObjectModel.ObservableCollection<MediaFile>;
                    
                    if (selectedFiles != null && selectedFiles.Count > 0)
                    {
                        // Delete multiple selected files
                        var count = selectedFiles.Count;
                        var result = await ModernDialog.ShowDeleteConfirmAsync($"{count} file(s)", "media files");
                        
                        if (result == ModernWpf.Controls.ContentDialogResult.Primary)
                        {
                            var deleteMethod = viewModel.GetType().GetMethod("DeleteSelectedAsync", 
                                System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                            await (Task)deleteMethod.Invoke(viewModel, null);
                        }
                    }
                    else if (DataContext is MediaFile mediaFile)
                    {
                        // Delete single file
                        var result = await ModernDialog.ShowDeleteConfirmAsync(mediaFile.FileName, "media file");
                        
                        if (result == ModernWpf.Controls.ContentDialogResult.Primary)
                        {
                            if (mainWindow?.DataContext is ViewModels.MainViewModel vm)
                            {
                                var mediaFileServiceField = vm.GetType().GetField("_mediaFileService",
                                    System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                                
                                if (mediaFileServiceField != null)
                                {
                                    var mediaFileService = mediaFileServiceField.GetValue(vm) as Services.IMediaFileService;
                                    if (mediaFileService != null)
                                    {
                                        await mediaFileService.DeleteMediaFileAsync(mediaFile.Id);
                                        
                                        // Refresh the view
                                        var refreshMethod = vm.GetType().GetMethod("LoadMediaFilesAsync", 
                                            System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                                        if (refreshMethod != null)
                                        {
                                            await (refreshMethod.Invoke(vm, null) as System.Threading.Tasks.Task);
                                        }
                                        
                                        await ModernDialog.ShowSuccessAsync("Media file deleted successfully!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private void SelectionCheckBox_Changed(object sender, RoutedEventArgs e)
        {
            if (DataContext is MediaFile mediaFile)
            {
                var mainWindow = Window.GetWindow(this) as MainWindow;
                if (mainWindow?.DataContext is ViewModels.MainViewModel viewModel)
                {
                    var method = viewModel.GetType().GetMethod("ToggleMediaSelection", 
                        System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                    method?.Invoke(viewModel, new[] { mediaFile });
                }
            }
        }
    }
}