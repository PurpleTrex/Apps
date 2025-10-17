using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    public class FileSystemService : IFileSystemService
    {
        private readonly IConfiguration _configuration;
        private readonly string _vaultDirectory;

        public FileSystemService(IConfiguration configuration)
        {
            _configuration = configuration;
            _vaultDirectory = Environment.ExpandEnvironmentVariables(
                _configuration.GetValue<string>("VaultSettings:DefaultVaultPath") ?? 
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "WindowsVault"));
        }

        public async Task<string> CopyToVaultAsync(string sourceFilePath)
        {
            if (!File.Exists(sourceFilePath))
                throw new FileNotFoundException($"Source file not found: {sourceFilePath}");

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

            await Task.Run(() => File.Copy(sourceFilePath, targetPath));
            return targetPath;
        }

        public async Task<string> MoveToVaultAsync(string sourceFilePath)
        {
            var targetPath = await CopyToVaultAsync(sourceFilePath);
            File.Delete(sourceFilePath);
            return targetPath;
        }

        public async Task<bool> DeleteFromVaultAsync(string vaultPath)
        {
            try
            {
                if (File.Exists(vaultPath))
                {
                    File.Delete(vaultPath);
                    return true;
                }
                return false;
            }
            catch
            {
                return false;
            }
        }

        public async Task<string> GetVaultDirectoryAsync()
        {
            await EnsureVaultDirectoryExistsAsync();
            return _vaultDirectory;
        }

        public async Task<bool> EnsureVaultDirectoryExistsAsync()
        {
            try
            {
                if (!Directory.Exists(_vaultDirectory))
                {
                    Directory.CreateDirectory(_vaultDirectory);
                    
                    // Create subdirectories
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Images"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Videos"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Audio"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Documents"));
                    Directory.CreateDirectory(Path.Combine(_vaultDirectory, "Thumbnails"));
                }
                return true;
            }
            catch
            {
                return false;
            }
        }

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

        public async Task<IEnumerable<string>> GetOrphanedFilesAsync()
        {
            if (!Directory.Exists(_vaultDirectory))
                return Enumerable.Empty<string>();

            // This is a placeholder - in a real implementation, you'd check against the database
            // to find files that exist in the vault but not in the database
            return await Task.Run(() =>
            {
                return Directory.GetFiles(_vaultDirectory, "*", SearchOption.AllDirectories)
                    .Where(f => !Path.GetDirectoryName(f)?.EndsWith("Thumbnails") == true)
                    .ToList();
            });
        }

        public async Task CleanupOrphanedFilesAsync()
        {
            var orphanedFiles = await GetOrphanedFilesAsync();
            
            foreach (var file in orphanedFiles)
            {
                try
                {
                    File.Delete(file);
                }
                catch
                {
                    // Ignore errors during cleanup
                }
            }
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