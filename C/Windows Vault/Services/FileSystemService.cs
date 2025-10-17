using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    /// <summary>
    /// Service for managing file system operations in the vault.
    /// </summary>
    public class FileSystemService : IFileSystemService
    {
        private readonly IConfiguration _configuration;
        private readonly ILoggingService _logger;
        private readonly string _vaultDirectory;

        public FileSystemService(IConfiguration configuration, ILoggingService logger)
        {
            _configuration = configuration;
            _logger = logger;
            _vaultDirectory = Environment.ExpandEnvironmentVariables(
                _configuration.GetValue<string>("VaultSettings:DefaultVaultPath") ??
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "WindowsVault"));

            _logger.LogInformation($"Vault directory initialized: {_vaultDirectory}");
        }

        /// <summary>
        /// Validates that a file path is safe and within the vault directory.
        /// </summary>
        private bool IsPathSafe(string path)
        {
            try
            {
                var fullPath = Path.GetFullPath(path);
                var vaultPath = Path.GetFullPath(_vaultDirectory);

                // Ensure the path is within the vault directory (prevent path traversal)
                return fullPath.StartsWith(vaultPath, StringComparison.OrdinalIgnoreCase);
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Validates that a source file path exists and is safe to access.
        /// </summary>
        private void ValidateSourcePath(string sourcePath)
        {
            if (string.IsNullOrWhiteSpace(sourcePath))
                throw new ArgumentException("Source path cannot be null or empty", nameof(sourcePath));

            // Normalize the path first to prevent path traversal
            string normalizedPath;
            try
            {
                normalizedPath = Path.GetFullPath(sourcePath);
            }
            catch (Exception ex)
            {
                throw new ArgumentException($"Invalid source path: {sourcePath}", nameof(sourcePath), ex);
            }

            // Check for path traversal attempts
            if (sourcePath.Contains("..") || sourcePath.Contains("~"))
            {
                throw new ArgumentException("Path traversal detected in source path", nameof(sourcePath));
            }

            // Verify file exists
            if (!File.Exists(normalizedPath))
                throw new FileNotFoundException($"Source file not found: {normalizedPath}");

            // Check file size limit
            var fileInfo = new FileInfo(normalizedPath);
            var maxFileSize = _configuration.GetValue<long>("VaultSettings:MaxFileSize", 2147483648); // 2GB default
            if (fileInfo.Length > maxFileSize)
            {
                throw new InvalidOperationException($"File size ({fileInfo.Length} bytes) exceeds maximum allowed size ({maxFileSize} bytes)");
            }

            // Validate file extension
            var extension = Path.GetExtension(normalizedPath).ToLowerInvariant();
            if (!IsAllowedFileType(extension))
            {
                throw new ArgumentException($"File type '{extension}' is not allowed", nameof(sourcePath));
            }
        }

        /// <summary>
        /// Copies a file to the vault directory.
        /// </summary>
        public async Task<string> CopyToVaultAsync(string sourceFilePath)
        {
            ValidateSourcePath(sourceFilePath);
            _logger.LogInformation($"Copying file to vault: {sourceFilePath}");

            await EnsureVaultDirectoryExistsAsync();

            var fileName = Path.GetFileName(sourceFilePath);
            var extension = Path.GetExtension(fileName).ToLowerInvariant();

            // Organize by media type
            var mediaTypeFolder = GetMediaTypeFolder(extension);
            var yearFolder = DateTime.Now.Year.ToString();

            var targetDirectory = Path.Combine(_vaultDirectory, mediaTypeFolder, yearFolder);
            Directory.CreateDirectory(targetDirectory);

            // Handle duplicate file names
            var targetPath = Path.Combine(targetDirectory, fileName);
            var counter = 1;
            while (File.Exists(targetPath))
            {
                var nameWithoutExtension = Path.GetFileNameWithoutExtension(fileName);
                var newFileName = $"{nameWithoutExtension}_{counter}{extension}";
                targetPath = Path.Combine(targetDirectory, newFileName);
                counter++;
            }

            // Validate target path is safe
            if (!IsPathSafe(targetPath))
            {
                _logger.LogError($"Target path is outside vault directory: {targetPath}");
                throw new InvalidOperationException("Target path is outside vault directory");
            }

            await Task.Run(() => File.Copy(sourceFilePath, targetPath));
            _logger.LogInformation($"File copied successfully to: {targetPath}");
            return targetPath;
        }

        /// <summary>
        /// Moves a file to the vault directory.
        /// </summary>
        public async Task<string> MoveToVaultAsync(string sourceFilePath)
        {
            ValidateSourcePath(sourceFilePath);
            _logger.LogInformation($"Moving file to vault: {sourceFilePath}");

            var targetPath = await CopyToVaultAsync(sourceFilePath);
            File.Delete(sourceFilePath);
            _logger.LogInformation($"Source file deleted: {sourceFilePath}");
            return targetPath;
        }

        /// <summary>
        /// Deletes a file from the vault.
        /// </summary>
        public async Task<bool> DeleteFromVaultAsync(string vaultPath)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(vaultPath))
                {
                    _logger.LogWarning("Attempted to delete with null or empty path");
                    return false;
                }

                // Validate path is within vault directory
                if (!IsPathSafe(vaultPath))
                {
                    _logger.LogError($"Attempted to delete file outside vault: {vaultPath}");
                    throw new InvalidOperationException("Cannot delete files outside vault directory");
                }

                if (File.Exists(vaultPath))
                {
                    File.Delete(vaultPath);
                    _logger.LogInformation($"File deleted from vault: {vaultPath}");
                    return true;
                }

                _logger.LogWarning($"File not found for deletion: {vaultPath}");
                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error deleting file from vault: {vaultPath}", ex);
                return false;
            }
        }

        public async Task<string> GetVaultDirectoryAsync()
        {
            await EnsureVaultDirectoryExistsAsync();
            return _vaultDirectory;
        }

        /// <summary>
        /// Ensures the vault directory structure exists.
        /// </summary>
        public async Task<bool> EnsureVaultDirectoryExistsAsync()
        {
            try
            {
                if (!Directory.Exists(_vaultDirectory))
                {
                    Directory.CreateDirectory(_vaultDirectory);
                    _logger.LogInformation($"Created vault directory: {_vaultDirectory}");

                    // Create subdirectories
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Images"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Videos"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Audio"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Documents"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Thumbnails"));
                    _logger.LogInformation("Created vault subdirectories");
                }
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError("Failed to create vault directory", ex);
                return false;
            }
        }

        /// <summary>
        /// Gets the total size of the vault directory in bytes.
        /// </summary>
        public async Task<long> GetVaultSizeAsync()
        {
            if (!Directory.Exists(_vaultDirectory))
                return 0;

            return await Task.Run(() =>
            {
                var directoryInfo = new DirectoryInfo(_vaultDirectory);
                return GetDirectorySize(directoryInfo);
            });
        }

        /// <summary>
        /// Gets orphaned files that exist in vault but not referenced in database.
        /// NOTE: This method requires database context to properly identify orphaned files.
        /// </summary>
        public async Task<IEnumerable<string>> GetOrphanedFilesAsync()
        {
            if (!Directory.Exists(_vaultDirectory))
                return Enumerable.Empty<string>();

            // This returns all files as potentially orphaned
            // The calling code should check against the database
            return await Task.Run(() =>
            {
                return Directory.GetFiles(_vaultDirectory, "*", SearchOption.AllDirectories)
                    .Where(f =>
                    {
                        var dir = Path.GetDirectoryName(f);
                        return dir != null && !dir.EndsWith("Thumbnails", StringComparison.OrdinalIgnoreCase);
                    })
                    .ToList();
            });
        }

        /// <summary>
        /// Cleans up orphaned files from the vault.
        /// </summary>
        public async Task CleanupOrphanedFilesAsync()
        {
            var orphanedFiles = await GetOrphanedFilesAsync();
            _logger.LogInformation($"Cleaning up {orphanedFiles.Count()} orphaned files");

            int deletedCount = 0;
            foreach (var file in orphanedFiles)
            {
                try
                {
                    if (IsPathSafe(file))
                    {
                        File.Delete(file);
                        deletedCount++;
                    }
                    else
                    {
                        _logger.LogWarning($"Skipped unsafe path during cleanup: {file}");
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError($"Error deleting orphaned file: {file}", ex);
                }
            }

            _logger.LogInformation($"Cleanup completed: {deletedCount} files deleted");
        }

        private string GetMediaTypeFolder(string extension)
        {
            var imageExtensions = _configuration.GetSection("VaultSettings:SupportedImageFormats").Get<string[]>() ?? Array.Empty<string>();
            var videoExtensions = _configuration.GetSection("VaultSettings:SupportedVideoFormats").Get<string[]>() ?? Array.Empty<string>();
            var audioExtensions = _configuration.GetSection("VaultSettings:SupportedAudioFormats").Get<string[]>() ?? Array.Empty<string>();

            if (imageExtensions.Contains(extension, StringComparer.OrdinalIgnoreCase))
                return "Images";
            if (videoExtensions.Contains(extension, StringComparer.OrdinalIgnoreCase))
                return "Videos";
            if (audioExtensions.Contains(extension, StringComparer.OrdinalIgnoreCase))
                return "Audio";

            return "Documents";
        }

        /// <summary>
        /// Checks if a file type is allowed based on configuration.
        /// </summary>
        private bool IsAllowedFileType(string extension)
        {
            if (string.IsNullOrWhiteSpace(extension))
                return false;

            // Remove leading dot if present
            extension = extension.TrimStart('.');

            var imageExtensions = _configuration.GetSection("VaultSettings:SupportedImageFormats").Get<string[]>() ?? Array.Empty<string>();
            var videoExtensions = _configuration.GetSection("VaultSettings:SupportedVideoFormats").Get<string[]>() ?? Array.Empty<string>();
            var audioExtensions = _configuration.GetSection("VaultSettings:SupportedAudioFormats").Get<string[]>() ?? Array.Empty<string>();
            var documentExtensions = _configuration.GetSection("VaultSettings:SupportedDocumentFormats").Get<string[]>() ?? Array.Empty<string>();

            // Check against all allowed extensions
            return imageExtensions.Any(e => e.Equals(extension, StringComparison.OrdinalIgnoreCase)) ||
                   videoExtensions.Any(e => e.Equals(extension, StringComparison.OrdinalIgnoreCase)) ||
                   audioExtensions.Any(e => e.Equals(extension, StringComparison.OrdinalIgnoreCase)) ||
                   documentExtensions.Any(e => e.Equals(extension, StringComparison.OrdinalIgnoreCase));
        }

        private long GetDirectorySize(DirectoryInfo directoryInfo)
        {
            long size = 0;
            
            try
            {
                foreach (var file in directoryInfo.GetFiles())
                {
                    size += file.Length;
                }
                
                foreach (var directory in directoryInfo.GetDirectories())
                {
                    size += GetDirectorySize(directory);
                }
            }
            catch
            {
                // Ignore access errors
            }
            
            return size;
        }
    }
}