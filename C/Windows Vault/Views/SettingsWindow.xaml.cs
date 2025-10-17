using System;
using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using WindowsVault.ViewModels;
using WindowsVault.Helpers;
using Microsoft.Win32;

namespace WindowsVault.Views
{
    public partial class SettingsWindow : Window
    {
        private SettingsViewModel? _viewModel;

        public SettingsWindow(SettingsViewModel viewModel)
        {
            InitializeComponent();
            _viewModel = viewModel;
            DataContext = _viewModel;
            Loaded += SettingsWindow_Loaded;
        }

        private async void SettingsWindow_Loaded(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                await _viewModel.LoadSettingsAsync();
                await _viewModel.LoadStatsAsync();
            }
        }

        private void BrowseVaultPath_Click(object sender, RoutedEventArgs e)
        {
            var dialog = new OpenFileDialog
            {
                Title = "Select Vault Storage Location",
                ValidateNames = false,
                CheckFileExists = false,
                CheckPathExists = true,
                FileName = "Select Folder"
            };

            if (dialog.ShowDialog() == true)
            {
                if (_viewModel != null)
                {
                    var folderPath = System.IO.Path.GetDirectoryName(dialog.FileName);
                    if (!string.IsNullOrEmpty(folderPath))
                    {
                        _viewModel.VaultPath = folderPath;
                    }
                }
            }
        }

        private async void SaveChanges_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                try
                {
                    await _viewModel.SaveSettingsAsync();
                    await ModernDialog.ShowSuccessAsync("Settings saved successfully!");
                    DialogResult = true;
                    Close();
                }
                catch (Exception ex)
                {
                    await ModernDialog.ShowErrorAsync($"Error saving settings: {ex.Message}");
                }
            }
        }

        private void Cancel_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
            Close();
        }

        private async void BackupNow_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                try
                {
                    _viewModel.StatusMessage = "Creating backup...";
                    await _viewModel.CreateBackupAsync();
                    await ModernDialog.ShowSuccessAsync("Backup created successfully!");
                    _viewModel.StatusMessage = "Backup completed";
                }
                catch (Exception ex)
                {
                    await ModernDialog.ShowErrorAsync($"Error creating backup: {ex.Message}");
                    _viewModel.StatusMessage = "Backup failed";
                }
            }
        }

        private void OpenBackupFolder_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var backupPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Backups");
                if (!Directory.Exists(backupPath))
                {
                    Directory.CreateDirectory(backupPath);
                }

                Process.Start(new ProcessStartInfo
                {
                    FileName = backupPath,
                    UseShellExecute = true
                });
            }
            catch (Exception ex)
            {
                ModernDialog.ShowErrorAsync($"Error opening backup folder: {ex.Message}");
            }
        }

        private async void OptimizeDatabase_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var confirm = await ModernDialog.ShowConfirmAsync(
                    "This will optimize the database and may take a few moments. Continue?",
                    "Optimize Database");

                if (confirm)
                {
                    try
                    {
                        _viewModel.StatusMessage = "Optimizing database...";
                        await _viewModel.OptimizeDatabaseAsync();
                        await ModernDialog.ShowSuccessAsync("Database optimized successfully!");
                        _viewModel.StatusMessage = "Optimization complete";
                    }
                    catch (Exception ex)
                    {
                        await ModernDialog.ShowErrorAsync($"Error optimizing database: {ex.Message}");
                        _viewModel.StatusMessage = "Optimization failed";
                    }
                }
            }
        }

        private async void CleanOrphanedFiles_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                var confirm = await ModernDialog.ShowConfirmAsync(
                    "This will remove files in the vault that are no longer in the database. Continue?",
                    "Clean Orphaned Files");

                if (confirm)
                {
                    try
                    {
                        _viewModel.StatusMessage = "Cleaning orphaned files...";
                        var count = await _viewModel.CleanOrphanedFilesAsync();
                        await ModernDialog.ShowSuccessAsync($"Cleaned {count} orphaned files!");
                        _viewModel.StatusMessage = $"Removed {count} files";
                    }
                    catch (Exception ex)
                    {
                        await ModernDialog.ShowErrorAsync($"Error cleaning orphaned files: {ex.Message}");
                        _viewModel.StatusMessage = "Cleaning failed";
                    }
                }
            }
        }

        private async void ExportLibrary_Click(object sender, RoutedEventArgs e)
        {
            var saveFileDialog = new SaveFileDialog
            {
                Title = "Export Library",
                Filter = "Vault Export (*.vaultx)|*.vaultx|All Files (*.*)|*.*",
                DefaultExt = ".vaultx",
                FileName = $"WindowsVault_Export_{DateTime.Now:yyyyMMdd_HHmmss}.vaultx"
            };

            if (saveFileDialog.ShowDialog() == true)
            {
                if (_viewModel != null)
                {
                    try
                    {
                        _viewModel.StatusMessage = "Exporting library...";
                        await _viewModel.ExportLibraryAsync(saveFileDialog.FileName);
                        await ModernDialog.ShowSuccessAsync("Library exported successfully!");
                        _viewModel.StatusMessage = "Export complete";
                    }
                    catch (Exception ex)
                    {
                        await ModernDialog.ShowErrorAsync($"Error exporting library: {ex.Message}");
                        _viewModel.StatusMessage = "Export failed";
                    }
                }
            }
        }

        private async void ImportLibrary_Click(object sender, RoutedEventArgs e)
        {
            var openFileDialog = new OpenFileDialog
            {
                Title = "Import Library",
                Filter = "Vault Export (*.vaultx)|*.vaultx|All Files (*.*)|*.*"
            };

            if (openFileDialog.ShowDialog() == true)
            {
                var confirm = await ModernDialog.ShowConfirmAsync(
                    "Importing will merge the imported data with your current library. Continue?",
                    "Import Library");

                if (confirm && _viewModel != null)
                {
                    try
                    {
                        _viewModel.StatusMessage = "Importing library...";
                        await _viewModel.ImportLibraryAsync(openFileDialog.FileName);
                        await ModernDialog.ShowSuccessAsync("Library imported successfully!");
                        _viewModel.StatusMessage = "Import complete";
                    }
                    catch (Exception ex)
                    {
                        await ModernDialog.ShowErrorAsync($"Error importing library: {ex.Message}");
                        _viewModel.StatusMessage = "Import failed";
                    }
                }
            }
        }

        private async void ViewLicenses_Click(object sender, RoutedEventArgs e)
        {
            var licenses = @"Windows Vault v1.0.0

Third-Party Licenses:

ModernWPF UI Library
- https://github.com/Kinnara/ModernWpf
- MIT License

CommunityToolkit.Mvvm
- https://github.com/CommunityToolkit/dotnet
- MIT License

Entity Framework Core
- https://github.com/dotnet/efcore
- MIT License

SQLite
- https://www.sqlite.org/
- Public Domain

For full license texts, visit the respective project repositories.";

            var licenseWindow = new Window
            {
                Title = "Third-Party Licenses",
                Width = 600,
                Height = 500,
                WindowStartupLocation = WindowStartupLocation.CenterOwner,
                Owner = this,
                Content = new ScrollViewer
                {
                    Content = new TextBlock
                    {
                        Text = licenses,
                        Margin = new Thickness(20),
                        TextWrapping = TextWrapping.Wrap,
                        FontFamily = new System.Windows.Media.FontFamily("Consolas")
                    }
                }
            };

            licenseWindow.ShowDialog();
        }
    }
}
