using CommunityToolkit.Mvvm.ComponentModel;
using System;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Threading.Tasks;
using WindowsVault.Services;
using WindowsVault.Data;
using Microsoft.EntityFrameworkCore;

namespace WindowsVault.ViewModels
{
    public partial class SettingsViewModel : ObservableObject
    {
        private readonly ISettingsService _settingsService;
        private readonly IMediaFileService _mediaFileService;
        private readonly ITagService _tagService;
        private readonly VaultDbContext _context;

        [ObservableProperty]
        private string _vaultPath = string.Empty;

        [ObservableProperty]
        private bool _autoBackupEnabled;

        [ObservableProperty]
        private int _backupIntervalHours;

        [ObservableProperty]
        private int _thumbnailSize;

        [ObservableProperty]
        private bool _darkModeEnabled;

        [ObservableProperty]
        private int _totalMediaFiles;

        [ObservableProperty]
        private int _totalTags;

        [ObservableProperty]
        private string _databaseSize = "0 KB";

        [ObservableProperty]
        private string _statusMessage = string.Empty;

        public SettingsViewModel(
            ISettingsService settingsService,
            IMediaFileService mediaFileService,
            ITagService tagService,
            VaultDbContext context)
        {
            _settingsService = settingsService;
            _mediaFileService = mediaFileService;
            _tagService = tagService;
            _context = context;
        }

        public async Task LoadSettingsAsync()
        {
            VaultPath = await _settingsService.GetVaultPathAsync();
            AutoBackupEnabled = await _settingsService.GetAutoBackupEnabledAsync();
            BackupIntervalHours = await _settingsService.GetBackupIntervalHoursAsync();
            ThumbnailSize = await _settingsService.GetThumbnailSizeAsync();
            DarkModeEnabled = await _settingsService.GetDarkModeEnabledAsync();
        }

        public async Task SaveSettingsAsync()
        {
            await _settingsService.SetVaultPathAsync(VaultPath);
            await _settingsService.SetAutoBackupEnabledAsync(AutoBackupEnabled);
            await _settingsService.SetBackupIntervalHoursAsync(BackupIntervalHours);
            await _settingsService.SetThumbnailSizeAsync(ThumbnailSize);
            await _settingsService.SetDarkModeEnabledAsync(DarkModeEnabled);
        }

        public async Task LoadStatsAsync()
        {
            var mediaFiles = await _mediaFileService.GetAllMediaFilesAsync();
            TotalMediaFiles = mediaFiles.Count();

            var tags = await _tagService.GetAllTagsAsync();
            TotalTags = tags.Count();

            try
            {
                var dbPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "vault.db");
                if (File.Exists(dbPath))
                {
                    var fileInfo = new FileInfo(dbPath);
                    var sizeInKb = fileInfo.Length / 1024.0;
                    if (sizeInKb > 1024)
                    {
                        DatabaseSize = $"{sizeInKb / 1024.0:F2} MB";
                    }
                    else
                    {
                        DatabaseSize = $"{sizeInKb:F2} KB";
                    }
                }
            }
            catch
            {
                DatabaseSize = "Unknown";
            }
        }

        public async Task CreateBackupAsync()
        {
            var backupFolder = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Backups");
            Directory.CreateDirectory(backupFolder);

            var timestamp = DateTime.Now.ToString("yyyyMMdd_HHmmss");
            var backupFile = Path.Combine(backupFolder, $"vault_backup_{timestamp}.zip");

            var dbPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "vault.db");
            var vaultPath = await _settingsService.GetVaultPathAsync();

            using (var archive = ZipFile.Open(backupFile, ZipArchiveMode.Create))
            {
                // Backup database
                if (File.Exists(dbPath))
                {
                    archive.CreateEntryFromFile(dbPath, "vault.db");
                }

                // Backup settings
                var settingsPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "appsettings.json");
                if (File.Exists(settingsPath))
                {
                    archive.CreateEntryFromFile(settingsPath, "appsettings.json");
                }
            }

            await Task.CompletedTask;
        }

        public async Task OptimizeDatabaseAsync()
        {
            await _context.Database.ExecuteSqlRawAsync("VACUUM");
            await _context.Database.ExecuteSqlRawAsync("ANALYZE");
            await LoadStatsAsync();
        }

        public async Task<int> CleanOrphanedFilesAsync()
        {
            var vaultPath = await _settingsService.GetVaultPathAsync();
            if (!Directory.Exists(vaultPath))
                return 0;

            var allMediaFiles = await _mediaFileService.GetAllMediaFilesAsync();
            var validPaths = allMediaFiles.Select(m => m.VaultPath).ToHashSet();

            var filesInVault = Directory.GetFiles(vaultPath, "*.*", SearchOption.AllDirectories);
            var orphanedCount = 0;

            foreach (var file in filesInVault)
            {
                if (!validPaths.Contains(file) && !file.EndsWith(".db") && !file.EndsWith(".db-shm") && !file.EndsWith(".db-wal"))
                {
                    try
                    {
                        File.Delete(file);
                        orphanedCount++;
                    }
                    catch
                    {
                        // Ignore errors
                    }
                }
            }

            return orphanedCount;
        }

        public async Task ExportLibraryAsync(string exportPath)
        {
            var dbPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "vault.db");

            using (var archive = ZipFile.Open(exportPath, ZipArchiveMode.Create))
            {
                // Export database
                if (File.Exists(dbPath))
                {
                    archive.CreateEntryFromFile(dbPath, "vault.db");
                }

                // Export metadata
                var metadata = new
                {
                    ExportDate = DateTime.Now,
                    Version = "1.0.0",
                    TotalMediaFiles = TotalMediaFiles,
                    TotalTags = TotalTags
                };

                var metadataEntry = archive.CreateEntry("metadata.json");
                using (var writer = new StreamWriter(metadataEntry.Open()))
                {
                    await writer.WriteAsync(System.Text.Json.JsonSerializer.Serialize(metadata));
                }
            }
        }

        public async Task ImportLibraryAsync(string importPath)
        {
            // This is a simplified implementation
            // In a real app, you'd want to properly merge databases
            await Task.Run(() =>
            {
                using (var archive = ZipFile.OpenRead(importPath))
                {
                    var dbEntry = archive.GetEntry("vault.db");
                    if (dbEntry != null)
                    {
                        // For now, just notify that import would happen
                        // Full implementation would require database merging logic
                        throw new NotImplementedException("Database import/merge functionality requires custom implementation to avoid data conflicts");
                    }
                }
            });
        }
    }
}