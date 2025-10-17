using System;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    /// <summary>
    /// Interface for backup service operations.
    /// </summary>
    public interface IBackupService : IDisposable
    {
        /// <summary>
        /// Creates a backup of the vault database and settings.
        /// </summary>
        /// <returns>The path to the created backup file.</returns>
        Task<string> CreateBackupAsync();

        /// <summary>
        /// Restores the vault from a backup file.
        /// </summary>
        /// <param name="backupPath">The path to the backup file.</param>
        /// <returns>True if restoration was successful, false otherwise.</returns>
        Task<bool> RestoreBackupAsync(string backupPath);

        /// <summary>
        /// Gets the list of available backup files.
        /// </summary>
        /// <returns>Array of backup file paths.</returns>
        Task<string[]> GetBackupFilesAsync();

        /// <summary>
        /// Cleans up old backup files based on retention policy.
        /// </summary>
        /// <returns>Number of files deleted.</returns>
        Task<int> CleanupOldBackupsAsync();

        /// <summary>
        /// Starts the automatic backup timer.
        /// </summary>
        void StartAutoBackup();

        /// <summary>
        /// Stops the automatic backup timer.
        /// </summary>
        void StopAutoBackup();
    }
}