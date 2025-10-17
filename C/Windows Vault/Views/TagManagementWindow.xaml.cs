using System.Windows;
using System.Windows.Input;
using WindowsVault.ViewModels;
using WindowsVault.Helpers;

namespace WindowsVault.Views
{
    public partial class TagManagementWindow : Window
    {
        private TagManagementViewModel? _viewModel;

        public TagManagementWindow(TagManagementViewModel viewModel)
        {
            InitializeComponent();
            _viewModel = viewModel;
            DataContext = _viewModel;
            
            Loaded += TagManagementWindow_Loaded;
        }

        private async void TagManagementWindow_Loaded(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                await _viewModel.InitializeAsync();
            }
        }

        private async void CreateTagButton_Click(object sender, RoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Create Tag button clicked!");
            
            // Manually call the CreateTagAsync method since command binding isn't working
            if (_viewModel != null)
            {
                System.Diagnostics.Debug.WriteLine($"Calling CreateTagAsync with Tag Name: '{_viewModel.NewTagName}', Color: '{_viewModel.NewTagColor}'");
                
                // Call the method through reflection to access the private method
                var method = _viewModel.GetType().GetMethod("CreateTagAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    var task = method.Invoke(_viewModel, null) as System.Threading.Tasks.Task;
                    if (task != null)
                    {
                        await task;
                        await ModernDialog.ShowSuccessAsync($"Tag '{_viewModel.NewTagName}' created successfully!\nCheck the sidebar.");
                    }
                }
                else
                {
                    await ModernDialog.ShowErrorAsync("Could not find CreateTagAsync method!");
                }
            }
        }

        private async void UpdateTagButton_Click(object sender, RoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Update Tag button clicked!");
            
            // Manually call the UpdateTagAsync method
            if (_viewModel != null)
            {
                System.Diagnostics.Debug.WriteLine($"Calling UpdateTagAsync with Tag Name: '{_viewModel.SelectedTagName}', Color: '{_viewModel.SelectedTagColor}'");
                
                var method = _viewModel.GetType().GetMethod("UpdateTagAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    var task = method.Invoke(_viewModel, null) as System.Threading.Tasks.Task;
                    if (task != null)
                    {
                        await task;
                        await ModernDialog.ShowSuccessAsync("Tag updated successfully!");
                    }
                }
                else
                {
                    await ModernDialog.ShowErrorAsync("Could not find UpdateTagAsync method!");
                }
            }
        }

        private async void DeleteTagButton_Click(object sender, RoutedEventArgs e)
        {
            System.Diagnostics.Debug.WriteLine("Delete Tag button clicked!");
            
            // Manually call the DeleteTagAsync method
            if (_viewModel != null)
            {
                if (_viewModel.SelectedTag == null)
                {
                    await ModernDialog.ShowErrorAsync("No tag selected!");
                    return;
                }

                System.Diagnostics.Debug.WriteLine($"Calling DeleteTagAsync for Tag: '{_viewModel.SelectedTag.Name}'");
                
                var method = _viewModel.GetType().GetMethod("DeleteTagAsync", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);
                if (method != null)
                {
                    var task = method.Invoke(_viewModel, null) as System.Threading.Tasks.Task;
                    if (task != null)
                    {
                        await task;
                    }
                }
                else
                {
                    await ModernDialog.ShowErrorAsync("Could not find DeleteTagAsync method!");
                }
            }
        }

        private void NewTagColorPicker_Click(object sender, MouseButtonEventArgs e)
        {
            if (_viewModel != null)
            {
                var colorPicker = new ColorPickerDialog(_viewModel.NewTagColor);
                colorPicker.Owner = this;
                
                if (colorPicker.ShowDialog() == true)
                {
                    _viewModel.NewTagColor = colorPicker.SelectedColor;
                    NewTagColorTextBox.Text = colorPicker.SelectedColor;
                }
            }
        }

        private void SelectedTagColorPicker_Click(object sender, MouseButtonEventArgs e)
        {
            if (_viewModel != null && !string.IsNullOrEmpty(_viewModel.SelectedTagColor))
            {
                var colorPicker = new ColorPickerDialog(_viewModel.SelectedTagColor);
                colorPicker.Owner = this;
                
                if (colorPicker.ShowDialog() == true)
                {
                    _viewModel.SelectedTagColor = colorPicker.SelectedColor;
                    SelectedTagColorBox.Text = colorPicker.SelectedColor;
                }
            }
        }
    }
}