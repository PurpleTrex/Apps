using Microsoft.Extensions.DependencyInjection;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Threading.Tasks;
using WindowsVault.ViewModels;
using WindowsVault.Models;
using System;

namespace WindowsVault.Views
{
    public partial class MainWindow : Window
    {
        private MainViewModel? _viewModel;
        private bool _sortAscending = true;
        private readonly IServiceProvider _serviceProvider;

        public MainWindow(MainViewModel viewModel, IServiceProvider serviceProvider)
        {
            InitializeComponent();
            _viewModel = viewModel;
            _serviceProvider = serviceProvider;
            DataContext = _viewModel;
            
            Loaded += MainWindow_Loaded;
        }

        private async void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                await _viewModel.InitializeAsync();
            }
            else
            {
                // ViewModel not injected - show error
                MessageBox.Show("Application initialization error: ViewModel not properly injected.", 
                    "Windows Vault", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        private void SortComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (SortComboBox.SelectedItem is ComboBoxItem item && _viewModel != null)
            {
                var sortBy = item.Content?.ToString();
                if (!string.IsNullOrEmpty(sortBy))
                {
                    _viewModel.SortMediaFiles(sortBy, _sortAscending);
                }
            }
        }

        private void SortOrderButton_Click(object sender, RoutedEventArgs e)
        {
            _sortAscending = !_sortAscending;
            SortOrderButton.Content = _sortAscending ? "↑" : "↓";
            
            if (SortComboBox.SelectedItem is ComboBoxItem item && _viewModel != null)
            {
                var sortBy = item.Content?.ToString();
                if (!string.IsNullOrEmpty(sortBy))
                {
                    _viewModel.SortMediaFiles(sortBy, _sortAscending);
                }
            }
        }

        private async void MediaItem_DoubleClick(object sender, MouseButtonEventArgs e)
        {
            if (sender is FrameworkElement element && element.DataContext is MediaFile mediaFile)
            {
                // Open the media detail window for tag assignment
                var mediaDetailWindow = _serviceProvider.GetRequiredService<MediaDetailWindow>();
                await mediaDetailWindow.InitializeAsync(mediaFile);
                mediaDetailWindow.Owner = this;
                mediaDetailWindow.ShowDialog();
                
                // Refresh the view after closing
                if (_viewModel != null)
                {
                    var refreshMethod = _viewModel.GetType().GetMethod("LoadMediaFilesAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                    if (refreshMethod != null)
                    {
                        await (refreshMethod.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                    }
                }
            }
        }

        private async void ManageTagsButton_Click(object sender, RoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Manage Tags button clicked!");
            
            if (_viewModel != null)
            {
                // Manually call the ManageTags method
                var method = _viewModel.GetType().GetMethod("ManageTags", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    var task = method.Invoke(_viewModel, null) as System.Threading.Tasks.Task;
                    if (task != null)
                    {
                        await task;
                        System.Diagnostics.Debug.WriteLine("ManageTags completed, tags should be refreshed.");
                    }
                }
                else
                {
                    MessageBox.Show("Could not find ManageTags method!", "Error", MessageBoxButton.OK);
                }
            }
        }

        private async void AddMediaButton_Click(object sender, RoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Add Media button clicked!");
            
            if (_viewModel != null)
            {
                // Manually call the AddMediaAsync method
                var method = _viewModel.GetType().GetMethod("AddMediaAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    var task = method.Invoke(_viewModel, null) as System.Threading.Tasks.Task;
                    if (task != null)
                    {
                        await task;
                        System.Diagnostics.Debug.WriteLine("AddMediaAsync completed.");
                    }
                }
                else
                {
                    MessageBox.Show("Could not find AddMediaAsync method!", "Error", MessageBoxButton.OK);
                }
            }
        }

        private async void AllMediaButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ShowAllMediaAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void ImagesButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ShowImagesAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void VideosButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ShowVideosAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void AudioButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ShowAudioAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void FavoritesButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ShowFavoritesAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void AddTagButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("AddTagAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void SettingsButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ShowSettings", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private void SelectModeButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ToggleSelectionMode", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                method?.Invoke(_viewModel, null);
            }
        }

        private void SelectAllButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("SelectAll", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                method?.Invoke(_viewModel, null);
            }
        }

        private void ClearSelectionButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ClearSelection", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                method?.Invoke(_viewModel, null);
            }
        }

        private async void DeleteSelectedButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("DeleteSelectedAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void AssignTagsButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("AssignTagsToSelectedAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }
    }
}