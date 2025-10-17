using Microsoft.Extensions.Configuration;
using System;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Timers;
using WindowsVault.Data;
using Timer = System.Timers.Timer;

namespace WindowsVault.Services
{
    /// <summary>
    /// Automatic backup service that creates periodic backups of the vault database.
    /// </summary>
    public class BackupService : IBackupService, IDisposable
    {
        private readonly IConfiguration _configuration;
        private readonly ILoggingService _logger;
        private readonly ISettingsService _settingsService;
        private Timer? _backupTimer;
        private bool _disposed = false;

        public BackupService(
            IConfiguration configuration,
            ILoggingService logger,
            ISettingsService settingsService)
        {
            _configuration = configuration;
            _logger = logger;
            _settingsService = settingsService;
        }

        /// <summary>
        /// Starts the automatic backup service.
        /// </summary>
        public async Task StartAsync()
        {
            try
            {
                var isEnabled = await _settingsService.GetAutoBackupEnabledAsync();

                if (!isEnabled)
                {
                    _logger.LogInformation("Automatic backup is disabled in settings");
                    return;
                }

                var intervalHours = await _settingsService.GetBackupIntervalHoursAsync();

                if (intervalHours <= 0)
                {
                    _logger.LogWarning("Invalid backup interval, using default 24 hours");
                    intervalHours = 24;
                }

                // Convert hours to milliseconds
                var intervalMs = intervalHours * 60 * 60 * 1000;

                _backupTimer = new Timer(intervalMs);
                _backupTimer.Elapsed += async (sender, e) => await OnBackupTimerElapsed();
                _backupTimer.AutoReset = true;
                _backupTimer.Start();

                _logger.LogInformation($"Automatic backup service started with {intervalHours} hour interval");

                // Create an initial backup on startup
                await CreateBackupAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError("Error starting automatic backup service", ex);
                throw;
            }
        }

        /// <summary>
        /// Stops the automatic backup service.
        /// </summary>
        public void Stop()
        {
            if (_backupTimer != null)
            {
                _backupTimer.Stop();
                _backupTimer.Dispose();
                _backupTimer = null;
                _logger.LogInformation("Automatic backup service stopped");
            }
        }

        private async Task OnBackupTimerElapsed()
        {
            await CreateBackupAsync();
        }

        /// <summary>
        /// Creates a backup of the vault database.
        /// </summary>
        public async Task<string> CreateBackupAsync()
        {
            try
            {
                _logger.LogInformation("Starting backup creation");

                var backupFolder = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Backups");
                Directory.CreateDirectory(backupFolder);

                var timestamp = DateTime.Now.ToString("yyyyMMdd_HHmmss");
                var backupFile = Path.Combine(backupFolder, $"vault_backup_{timestamp}.zip");

                var dbPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "vault.db");

                if (!File.Exists(dbPath))
                {
                    _logger.LogWarning("Database file not found, backup skipped");
                    throw new FileNotFoundException("Database file not found");
                }

                await Task.Run(() =>
                {
                    using (var archive = ZipFile.Open(backupFile, ZipArchiveMode.Create))
                    {
                        // Backup database
                        archive.CreateEntryFromFile(dbPath, "vault.db");

                        // Backup settings if available
                        var settingsPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "appsettings.json");
                        if (File.Exists(settingsPath))
                        {
                            archive.CreateEntryFromFile(settingsPath, "appsettings.json");
                        }

                        // Add metadata
                        var metadataEntry = archive.CreateEntry("backup_metadata.txt");
                        using (var writer = new StreamWriter(metadataEntry.Open()))
                        {
                            writer.WriteLine($"Backup Created: {DateTime.Now}");
                            writer.WriteLine($"Windows Vault Version: 1.0.0");
                            writer.WriteLine($"Database Path: {dbPath}");
                        }
                    }
                });

                // Clean up old backups (keep only last 10)
                await CleanupOldBackupsAsync(backupFolder, 10);

                _logger.LogInformation($"Backup created successfully: {backupFile}");
                return backupFile;
            }
            catch (Exception ex)
            {
                _logger.LogError("Error creating backup", ex);
                throw;
            }
        }

        /// <summary>
        /// Restores the vault from a backup file.
        /// </summary>
        public async Task<bool> RestoreBackupAsync(string backupFilePath)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(backupFilePath))
                    throw new ArgumentException("Backup file path cannot be null or empty", nameof(backupFilePath));

                if (!File.Exists(backupFilePath))
                    throw new FileNotFoundException($"Backup file not found: {backupFilePath}");

                _logger.LogInformation($"Starting backup restoration from: {backupFilePath}");

                var dbPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "vault.db");

                // Create a backup of the current database before restoring
                if (File.Exists(dbPath))
                {
                    var backupCurrentDb = Path.Combine(
                        Path.GetDirectoryName(dbPath) ?? "",
                        $"vault_pre_restore_{DateTime.Now:yyyyMMdd_HHmmss}.db");

                    File.Copy(dbPath, backupCurrentDb, true);
                    _logger.LogInformation($"Current database backed up to: {backupCurrentDb}");
                }

                await Task.Run(() =>
                {
                    using (var archive = ZipFile.OpenRead(backupFilePath))
                    {
                        var dbEntry = archive.GetEntry("vault.db");
                        if (dbEntry != null)
                        {
                            dbEntry.ExtractToFile(dbPath, true);
                            _logger.LogInformation("Database restored successfully");
                        }
                        else
                        {
                            throw new InvalidOperationException("Backup file does not contain vault.db");
                        }
                    }
                });

                _logger.LogInformation("Backup restoration completed successfully");
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError("Error restoring backup", ex);
                return false;
            }
        }

        /// <summary>
        /// Gets the list of available backup files.
        /// </summary>
        public async Task<string[]> GetBackupFilesAsync()
        {
            try
            {
                var backupFolder = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Backups");
                if (!Directory.Exists(backupFolder))
                {
                    return Array.Empty<string>();
                }

                return await Task.Run(() =>
                {
                    return Directory.GetFiles(backupFolder, "vault_backup_*.zip")
                        .OrderByDescending(f => new FileInfo(f).CreationTime)
                        .ToArray();
                });
            }
            catch (Exception ex)
            {
                _logger.LogError("Error getting backup files", ex);
                return Array.Empty<string>();
            }
        }

        /// <summary>
        /// Cleans up old backup files based on retention policy.
        /// </summary>
        public async Task<int> CleanupOldBackupsAsync()
        {
            var backupFolder = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Backups");
            return await CleanupOldBackupsAsync(backupFolder, 10);
        }

        /// <summary>
        /// Starts the automatic backup timer.
        /// </summary>
        public void StartAutoBackup()
        {
            _ = StartAsync();
        }

        /// <summary>
        /// Stops the automatic backup timer.
        /// </summary>
        public void StopAutoBackup()
        {
            Stop();
        }

        /// <summary>
        /// Cleans up old backup files, keeping only the specified number of most recent backups.
        /// </summary>
        private async Task<int> CleanupOldBackupsAsync(string backupFolder, int keepCount)
        {
            try
            {
                return await Task.Run(() =>
                {
                    var backupFiles = new DirectoryInfo(backupFolder)
                        .GetFiles("vault_backup_*.zip")
                        .OrderByDescending(f => f.CreationTime)
                        .ToArray();

                    int deletedCount = 0;
                    if (backupFiles.Length > keepCount)
                    {
                        var filesToDelete = backupFiles.Skip(keepCount);
                        foreach (var file in filesToDelete)
                        {
                            try
                            {
                                file.Delete();
                                deletedCount++;
                                _logger.LogInformation($"Deleted old backup: {file.Name}");
                            }
                            catch (Exception ex)
                            {
                                _logger.LogWarning($"Could not delete old backup {file.Name}: {ex.Message}");
                            }
                        }
                    }
                    return deletedCount;
                });
            }
            catch (Exception ex)
            {
                _logger.LogWarning($"Error during backup cleanup: {ex.Message}");
                return 0;
            }
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!_disposed)
            {
                if (disposing)
                {
                    Stop();
                }
                _disposed = true;
            }
        }
    }
}
