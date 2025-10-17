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