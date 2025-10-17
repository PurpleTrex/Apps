using System.Windows;
using WindowsVault.ViewModels;

namespace WindowsVault.Views
{
    public partial class BulkTagAssignmentDialog : Window
    {
        public BulkTagAssignmentDialog()
        {
            InitializeComponent();
        }

        private BulkTagAssignmentViewModel ViewModel => (BulkTagAssignmentViewModel)DataContext;

        private async void ApplyButton_Click(object sender, RoutedEventArgs e)
        {
            if (ViewModel != null)
            {
                await ViewModel.ApplyTagsCommand.ExecuteAsync(null);
            }
            DialogResult = true;
            Close();
        }

        private void SkipButton_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
            Close();
        }
    }
}