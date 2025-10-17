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
                UpdateRatingStars();
            }
        }

        private void UpdateRatingStars()
        {
            if (_viewModel?.MediaFile == null) return;

            var rating = (int)_viewModel.MediaFile.Rating;

            // Update star colors based on rating
            Star1.Foreground = rating >= 1 ? System.Windows.Media.Brushes.Gold : System.Windows.Media.Brushes.Gray;
            Star2.Foreground = rating >= 2 ? System.Windows.Media.Brushes.Gold : System.Windows.Media.Brushes.Gray;
            Star3.Foreground = rating >= 3 ? System.Windows.Media.Brushes.Gold : System.Windows.Media.Brushes.Gray;
            Star4.Foreground = rating >= 4 ? System.Windows.Media.Brushes.Gold : System.Windows.Media.Brushes.Gray;
            Star5.Foreground = rating >= 5 ? System.Windows.Media.Brushes.Gold : System.Windows.Media.Brushes.Gray;
        }

        private async void SetRating_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel?.MediaFile == null) return;

            if (sender is System.Windows.Controls.Button button && button.Tag is string ratingStr)
            {
                if (int.TryParse(ratingStr, out int rating) && rating >= 1 && rating <= 5)
                {
                    _viewModel.MediaFile.Rating = rating;

                    // Update the UI
                    UpdateRatingStars();
                    RatingText.Text = $"Current Rating: {rating:F1} / 5.0";

                    // Save to database
                    if (_viewModel != null)
                    {
                        var method = _viewModel.GetType().GetMethod("SaveMediaFileAsync",
                            System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);

                        if (method != null)
                        {
                            await (method.Invoke(_viewModel, null) as Task);
                        }
                    }

                    await ModernDialog.ShowSuccessAsync($"Rating set to {rating} stars!");
                }
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
