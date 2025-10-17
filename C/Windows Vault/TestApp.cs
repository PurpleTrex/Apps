using System.Windows;

namespace WindowsVault
{
    public partial class TestApp : Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);
            
            try
            {
                var window = new Window
                {
                    Title = "Windows Vault - Test",
                    Width = 800,
                    Height = 600,
                    WindowStartupLocation = WindowStartupLocation.CenterScreen
                };
                
                window.Content = new System.Windows.Controls.TextBlock
                {
                    Text = "Windows Vault is working!",
                    HorizontalAlignment = HorizontalAlignment.Center,
                    VerticalAlignment = VerticalAlignment.Center,
                    FontSize = 24
                };
                
                window.Show();
            }
            catch (System.Exception ex)
            {
                MessageBox.Show($"Error: {ex.Message}\n\nStack Trace:\n{ex.StackTrace}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }
}