using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using WindowsVault.ViewModels;
using WindowsVault.Models;
using WindowsVault.Helpers;

namespace WindowsVault.Views
{
    public partial class MediaDetailWindow : Window
    {
        private MediaDetailViewModel? _viewModel;

        public MediaDetailWindow(MediaDetailViewModel viewModel)
        {
            InitializeComponent();
            _viewModel = viewModel;
            DataContext = _viewModel;
        }

        public async Task InitializeAsync(MediaFile mediaFile)
        {
            if (_viewModel != null)
            {
                await _viewModel.InitializeAsync(mediaFile);
            }
        }

        private async void AssignTagButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("AssignTagAsync", System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void RemoveTagButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("RemoveTagAsync", System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private async void ToggleFavoriteButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var method = _viewModel.GetType().GetMethod("ToggleFavoriteAsync", System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    await (method.Invoke(_viewModel, null) as System.Threading.Tasks.Task);
                }
            }
        }

        private void AvailableTag_Click(object sender, MouseButtonEventArgs e)
        {
            if (sender is FrameworkElement element && element.DataContext is Tag tag && _viewModel != null)
            {
                // Clear previous selection
                if (_viewModel.SelectedAvailableTag != null)
                {
                    _viewModel.SelectedAvailableTag.IsSelected = false;
                }
                
                // Set new selection
                tag.IsSelected = true;
                _viewModel.SelectedAvailableTag = tag;
                
                // Force UI refresh
                element.GetBindingExpression(FrameworkElement.DataContextProperty)?.UpdateTarget();
            }
        }

        private void AssignedTag_Click(object sender, MouseButtonEventArgs e)
        {
            if (sender is FrameworkElement element && element.DataContext is Tag tag && _viewModel != null)
            {
                // Clear previous selection
                if (_viewModel.SelectedAssignedTag != null)
                {
                    _viewModel.SelectedAssignedTag.IsSelected = false;
                }
                
                // Set new selection
                tag.IsSelected = true;
                _viewModel.SelectedAssignedTag = tag;
                
                // Force UI refresh
                element.GetBindingExpression(FrameworkElement.DataContextProperty)?.UpdateTarget();
            }
        }
    }
}
