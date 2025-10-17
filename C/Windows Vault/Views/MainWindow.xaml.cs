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
                            try
                            {
                                // Check if it's a directory
                                if (System.IO.Directory.Exists(filePath))
                                {
                                    _viewModel.StatusText = $"Importing folder: {System.IO.Path.GetFileName(filePath)}...";

                                    var progress = new Progress<string>(message => _viewModel.StatusText = message);
                                    await mediaFileService.ImportDirectoryAsync(filePath, progress);
                                }
                                else if (System.IO.File.Exists(filePath))
                                {
                                    _viewModel.StatusText = $"Adding {System.IO.Path.GetFileName(filePath)}...";
                                    var mediaFile = await mediaFileService.AddMediaFileAsync(filePath);
                                    addedMediaFiles.Add(mediaFile);
                                    addedCount++;
                                }
                            }
                            catch (InvalidOperationException)
                            {
                                skippedCount++; // Duplicate or unsupported file
                            }
                            catch (Exception ex)
                            {
                                System.Diagnostics.Debug.WriteLine($"Error adding dropped file {filePath}: {ex}");
                                skippedCount++;
                            }
                        }

                        // Reload media files
                        var loadMethod = _viewModel.GetType().GetMethod("LoadMediaFilesAsync",
                            System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);

                        if (loadMethod != null)
                        {
                            await (loadMethod.Invoke(_viewModel, null) as Task);
                        }

                        var applyFiltersMethod = _viewModel.GetType().GetMethod("ApplyFilters",
                            System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);

                        applyFiltersMethod?.Invoke(_viewModel, null);

                        _viewModel.StatusText = $"Added {addedCount} file(s), skipped {skippedCount}";

                        // Prompt for tag assignment if files were added
                        if (addedMediaFiles.Count > 0)
                        {
                            var promptMethod = _viewModel.GetType().GetMethod("PromptForTagAssignmentAsync",
                                System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance);

                            if (promptMethod != null)
                            {
                                await (promptMethod.Invoke(_viewModel, new object[] { addedMediaFiles }) as Task);
                            }
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