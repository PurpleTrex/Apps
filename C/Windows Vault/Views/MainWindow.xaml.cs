using Microsoft.Extensions.DependencyInjection;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Threading.Tasks;
using WindowsVault.ViewModels;
using WindowsVault.Models;
using System;
using System.Collections.Generic;
using System.Linq;

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

            // Add keyboard shortcuts
            this.PreviewKeyDown += MainWindow_PreviewKeyDown;
        }

        private async void AddMediaButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                // Call the method directly via reflection or make it public
                // For now, let's implement the logic here
                await AddMediaAsync();
            }
        }

        private async Task AddMediaAsync()
        {
            if (_viewModel == null) return;

            try
            {
                _viewModel.StatusText = "Opening file dialog...";
                
                var openFileDialog = new Microsoft.Win32.OpenFileDialog
                {
                    Title = "Select Media Files",
                    Multiselect = true,
                    Filter = "All Supported|*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.tiff;*.webp;*.mp4;*.avi;*.mkv;*.mov;*.wmv;*.flv;*.webm;*.mp3;*.wav;*.flac;*.aac;*.ogg|" +
                            "Images|*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.tiff;*.webp|" +
                            "Videos|*.mp4;*.avi;*.mkv;*.mov;*.wmv;*.flv;*.webm|" +
                            "Audio|*.mp3;*.wav;*.flac;*.aac;*.ogg|" +
                            "All Files|*.*"
                };

                var dialogResult = openFileDialog.ShowDialog();
                System.Diagnostics.Debug.WriteLine($"File dialog result: {dialogResult}");

                if (dialogResult == true)
                {
                    _viewModel.IsLoading = true;
                    var addedCount = 0;
                    var skippedCount = 0;
                    var addedMediaFiles = new List<MediaFile>();

                    // Get MediaFileService from service provider
                    var mediaFileService = _serviceProvider.GetService<WindowsVault.Services.IMediaFileService>();

                    if (mediaFileService == null)
                    {
                        MessageBox.Show("Media file service not available", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }

                    foreach (var fileName in openFileDialog.FileNames)
                    {
                        try
                        {
                            _viewModel.StatusText = $"Adding {System.IO.Path.GetFileName(fileName)}...";
                            var mediaFile = await mediaFileService.AddMediaFileAsync(fileName);
                            _viewModel.MediaFiles.Add(mediaFile);
                            addedMediaFiles.Add(mediaFile);
                            addedCount++;
                        }
                        catch (InvalidOperationException)
                        {
                            skippedCount++; // Duplicate file
                        }
                        catch (Exception ex)
                        {
                            _viewModel.StatusText = $"Error adding {System.IO.Path.GetFileName(fileName)}: {ex.Message}";
                            System.Diagnostics.Debug.WriteLine($"AddMedia Error: {ex}");
                        }
                    }

                    // Refresh the filtered list
                    _viewModel.ApplyFilters();
                    _viewModel.StatusText = $"Added {addedCount} files, skipped {skippedCount} duplicates";

                    // Prompt for tag assignment if files were added
                    if (addedMediaFiles.Count > 0)
                    {
                        await _viewModel.PromptForTagAssignmentAsync(addedMediaFiles);
                    }
                }
                else
                {
                    _viewModel.StatusText = "File selection cancelled";
                }
            }
            catch (Exception ex)
            {
                _viewModel.StatusText = $"Error opening file dialog: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"AddMediaAsync Error: {ex}");
            }
            finally
            {
                _viewModel.IsLoading = false;
            }
        }

        private async void SettingsButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null)
            {
                _viewModel.StatusText = "Opening settings...";
                var settingsWindow = _serviceProvider.GetRequiredService<SettingsWindow>();
                settingsWindow.Owner = this;
                settingsWindow.ShowDialog();
                _viewModel.StatusText = "Ready";
            }
        }

        private async void DeleteSelectedButton_Click(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null && _viewModel.SelectedMediaFiles.Count > 0)
            {
                await _viewModel.DeleteSelectedAsync();
            }
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
                    await _viewModel.LoadMediaFilesAsync();
                    _viewModel.ApplyFilters();
                }
            }
        }

        // Keyboard shortcuts handler
        private async void MainWindow_PreviewKeyDown(object sender, KeyEventArgs e)
        {
            if (_viewModel == null) return;

            // Ctrl+A - Select All
            if (e.Key == Key.A && (Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control)
            {
                _viewModel.SelectAll();
                e.Handled = true;
            }
            // Delete - Delete selected items
            else if (e.Key == Key.Delete)
            {
                if (_viewModel.SelectedMediaFiles.Count > 0)
                {
                    await _viewModel.DeleteSelectedAsync();
                    e.Handled = true;
                }
            }
            // Escape - Clear selection
            else if (e.Key == Key.Escape)
            {
                if (_viewModel.SelectedMediaFiles.Count > 0)
                {
                    _viewModel.ClearSelection();
                    e.Handled = true;
                }
            }
            // Enter - Open selected item (if only one selected)
            else if (e.Key == Key.Enter)
            {
                if (_viewModel.SelectedMediaFiles.Count == 1)
                {
                    var mediaFile = _viewModel.SelectedMediaFiles.First();
                    var mediaDetailWindow = _serviceProvider.GetRequiredService<MediaDetailWindow>();
                    await mediaDetailWindow.InitializeAsync(mediaFile);
                    mediaDetailWindow.Owner = this;
                    mediaDetailWindow.ShowDialog();

                    // Refresh after closing
                    await _viewModel.LoadMediaFilesAsync();
                    _viewModel.ApplyFilters();
                    e.Handled = true;
                }
            }
        }

        // Drag & Drop Implementation
        private void MediaGrid_DragOver(object sender, DragEventArgs e)
        {
            // Check if the dragged data contains files
            if (e.Data.GetDataPresent(DataFormats.FileDrop))
            {
                e.Effects = DragDropEffects.Copy;
            }
            else
            {
                e.Effects = DragDropEffects.None;
            }
            e.Handled = true;
        }

        private async void MediaGrid_Drop(object sender, DragEventArgs e)
        {
            if (_viewModel == null) return;

            try
            {
                if (e.Data.GetDataPresent(DataFormats.FileDrop))
                {
                    string[] files = (string[])e.Data.GetData(DataFormats.FileDrop);
                    
                    System.Diagnostics.Debug.WriteLine($"===== DROP: Received {files?.Length ?? 0} file(s) =====");

                    if (files != null && files.Length > 0)
                    {
                        _viewModel.IsLoading = true;
                        _viewModel.StatusText = $"Processing {files.Length} dropped file(s)...";

                        var addedCount = 0;
                        var skippedCount = 0;
                        var addedMediaFiles = new List<MediaFile>();

                        // Get MediaFileService from service provider
                        var mediaFileService = _serviceProvider.GetService<WindowsVault.Services.IMediaFileService>();

                        if (mediaFileService == null)
                        {
                            MessageBox.Show("Media file service not available", "Error",
                                MessageBoxButton.OK, MessageBoxImage.Error);
                            return;
                        }

                        foreach (var filePath in files)
                        {
                            System.Diagnostics.Debug.WriteLine($"Processing dropped file: {filePath}");
                            
                            try
                            {
                                // Check if it's a directory
                                if (System.IO.Directory.Exists(filePath))
                                {
                                    System.Diagnostics.Debug.WriteLine($"  -> Is directory, importing...");
                                    _viewModel.StatusText = $"Importing folder: {System.IO.Path.GetFileName(filePath)}...";

                                    // Track files before import for tag assignment
                                    var beforeCount = _viewModel.MediaFiles.Count;
                                    var beforeIds = _viewModel.MediaFiles.Select(m => m.Id).ToHashSet();

                                    var progress = new Progress<string>(message => _viewModel.StatusText = message);
                                    await mediaFileService.ImportDirectoryAsync(filePath, progress);
                                    
                                    // Reload all media files after directory import
                                    await _viewModel.LoadMediaFilesAsync();
                                    
                                    // Find newly added files for tag assignment
                                    var newFiles = _viewModel.MediaFiles.Where(m => !beforeIds.Contains(m.Id)).ToList();
                                    addedMediaFiles.AddRange(newFiles);
                                    addedCount += newFiles.Count;
                                    System.Diagnostics.Debug.WriteLine($"  -> Directory imported: {newFiles.Count} new files found");
                                }
                                else if (System.IO.File.Exists(filePath))
                                {
                                    System.Diagnostics.Debug.WriteLine($"  -> Is file, adding...");
                                    _viewModel.StatusText = $"Adding {System.IO.Path.GetFileName(filePath)}...";
                                    var mediaFile = await mediaFileService.AddMediaFileAsync(filePath);
                                    _viewModel.MediaFiles.Add(mediaFile); // Add to the collection
                                    addedMediaFiles.Add(mediaFile);
                                    addedCount++;
                                    System.Diagnostics.Debug.WriteLine($"  -> Successfully added file!");
                                }
                                else
                                {
                                    System.Diagnostics.Debug.WriteLine($"  -> Path doesn't exist!");
                                }
                            }
                            catch (InvalidOperationException ex)
                            {
                                System.Diagnostics.Debug.WriteLine($"  -> Skipped (InvalidOperation): {ex.Message}");
                                MessageBox.Show($"File skipped: {System.IO.Path.GetFileName(filePath)}\n\nReason: {ex.Message}", 
                                    "File Skipped", MessageBoxButton.OK, MessageBoxImage.Warning);
                                skippedCount++; // Duplicate or unsupported file
                            }
                            catch (Exception ex)
                            {
                                System.Diagnostics.Debug.WriteLine($"  -> Error: {ex.Message}");
                                System.Diagnostics.Debug.WriteLine($"  -> Stack: {ex.StackTrace}");
                                MessageBox.Show($"Error adding file: {System.IO.Path.GetFileName(filePath)}\n\nError: {ex.Message}", 
                                    "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                                System.Diagnostics.Debug.WriteLine($"Error adding dropped file {filePath}: {ex}");
                                skippedCount++;
                            }
                        }

                        // Refresh the filtered list
                        _viewModel.ApplyFilters();

                        _viewModel.StatusText = $"Added {addedCount} file(s), skipped {skippedCount}";

                        System.Diagnostics.Debug.WriteLine($"Drop completed: addedCount={addedCount}, addedMediaFiles.Count={addedMediaFiles.Count}");

                        // Prompt for tag assignment if files were added
                        if (addedMediaFiles.Count > 0)
                        {
                            System.Diagnostics.Debug.WriteLine($"Calling PromptForTagAssignmentAsync with {addedMediaFiles.Count} files");
                            await _viewModel.PromptForTagAssignmentAsync(addedMediaFiles);
                        }
                        else
                        {
                            System.Diagnostics.Debug.WriteLine("No files in addedMediaFiles list - not showing tag dialog");
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                _viewModel.StatusText = $"Error processing dropped files: {ex.Message}";
                System.Diagnostics.Debug.WriteLine($"Drop Error: {ex}");
                MessageBox.Show($"Error processing dropped files: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                if (_viewModel != null)
                {
                    _viewModel.IsLoading = false;
                }
            }
        }
    }
}