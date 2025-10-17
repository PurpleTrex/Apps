using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using WindowsVault.Data;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public class MediaFileService : IMediaFileService
    {
        private readonly VaultDbContext _context;
        private readonly IConfiguration _configuration;
        private readonly IThumbnailService _thumbnailService;
        private readonly IFileSystemService _fileSystemService;
        
        private readonly HashSet<string> _imageExtensions;
        private readonly HashSet<string> _videoExtensions;
        private readonly HashSet<string> _audioExtensions;

        public MediaFileService(
            VaultDbContext context, 
            IConfiguration configuration,
            IThumbnailService thumbnailService,
            IFileSystemService fileSystemService)
        {
            _context = context;
            _configuration = configuration;
            _thumbnailService = thumbnailService;
            _fileSystemService = fileSystemService;

            _imageExtensions = new HashSet<string>(
                _configuration.GetSection("VaultSettings:SupportedImageFormats").Get<string[]>() ?? Array.Empty<string>(),
                StringComparer.OrdinalIgnoreCase);
            
            _videoExtensions = new HashSet<string>(
                _configuration.GetSection("VaultSettings:SupportedVideoFormats").Get<string[]>() ?? Array.Empty<string>(),
                StringComparer.OrdinalIgnoreCase);
            
            _audioExtensions = new HashSet<string>(
                _configuration.GetSection("VaultSettings:SupportedAudioFormats").Get<string[]>() ?? Array.Empty<string>(),
                StringComparer.OrdinalIgnoreCase);
        }

        public async Task<IEnumerable<MediaFile>> GetAllMediaFilesAsync()
        {
            return await _context.MediaFiles
                .Include(m => m.MediaFileTags)
                    .ThenInclude(mft => mft.Tag)
                .Where(m => !m.IsDeleted)
                .OrderByDescending(m => m.DateAdded)
                .ToListAsync();
        }

        public async Task<IEnumerable<MediaFile>> GetMediaFilesByTypeAsync(MediaType mediaType)
        {
            return await _context.MediaFiles
                .Include(m => m.MediaFileTags)
                    .ThenInclude(mft => mft.Tag)
                .Where(m => m.MediaType == mediaType && !m.IsDeleted)
                .OrderByDescending(m => m.DateAdded)
                .ToListAsync();
        }

        public async Task<IEnumerable<MediaFile>> SearchMediaFilesAsync(string searchQuery)
        {
            if (string.IsNullOrWhiteSpace(searchQuery))
                return await GetAllMediaFilesAsync();

            var query = searchQuery.ToLower();
            return await _context.MediaFiles
                .Include(m => m.MediaFileTags)
                    .ThenInclude(mft => mft.Tag)
                .Where(m => !m.IsDeleted && (
                    m.FileName.ToLower().Contains(query) ||
                    (m.Description != null && m.Description.ToLower().Contains(query)) ||
                    m.MediaFileTags.Any(mft => mft.Tag.Name.ToLower().Contains(query))
                ))
                .OrderByDescending(m => m.DateAdded)
                .ToListAsync();
        }

        public async Task<IEnumerable<MediaFile>> GetMediaFilesByTagsAsync(IEnumerable<int> tagIds)
        {
            var tagIdList = tagIds.ToList();
            if (!tagIdList.Any())
                return await GetAllMediaFilesAsync();

            return await _context.MediaFiles
                .Include(m => m.MediaFileTags)
                    .ThenInclude(mft => mft.Tag)
                .Where(m => !m.IsDeleted && 
                       m.MediaFileTags.Any(mft => tagIdList.Contains(mft.TagId)))
                .OrderByDescending(m => m.DateAdded)
                .ToListAsync();
        }

        public async Task<MediaFile?> GetMediaFileByIdAsync(int id)
        {
            return await _context.MediaFiles
                .Include(m => m.MediaFileTags)
                    .ThenInclude(mft => mft.Tag)
                .FirstOrDefaultAsync(m => m.Id == id && !m.IsDeleted);
        }

        public async Task<MediaFile> AddMediaFileAsync(string filePath, bool copyToVault = true)
        {
            if (!File.Exists(filePath))
                throw new FileNotFoundException($"File not found: {filePath}");

            var fileInfo = new FileInfo(filePath);
            var extension = fileInfo.Extension.ToLowerInvariant();
            var mediaType = GetMediaType(extension);
            
            var fileHash = await CalculateFileHashAsync(filePath);
            if (await IsDuplicateAsync(fileHash))
                throw new InvalidOperationException("This file already exists in the vault");

            string vaultPath;
            if (copyToVault)
            {
                vaultPath = await _fileSystemService.CopyToVaultAsync(filePath);
            }
            else
            {
                vaultPath = filePath;
            }

            var mediaFile = new MediaFile
            {
                FileName = fileInfo.Name,
                FilePath = filePath,
                VaultPath = vaultPath,
                FileExtension = extension,
                MediaType = mediaType,
                FileSizeBytes = fileInfo.Length,
                FileHash = fileHash,
                DateAdded = DateTime.UtcNow,
                DateModified = fileInfo.LastWriteTimeUtc,
                DateTaken = await GetDateTakenAsync(filePath)
            };

            // Get media dimensions/duration
            await PopulateMediaPropertiesAsync(mediaFile);

            _context.MediaFiles.Add(mediaFile);
            await _context.SaveChangesAsync();

            // Generate thumbnail
            mediaFile.ThumbnailPath = await _thumbnailService.GenerateThumbnailAsync(mediaFile);
            await _context.SaveChangesAsync();

            return mediaFile;
        }

        public async Task<bool> UpdateMediaFileAsync(MediaFile mediaFile)
        {
            _context.MediaFiles.Update(mediaFile);
            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<bool> DeleteMediaFileAsync(int id)
        {
            var mediaFile = await _context.MediaFiles.FindAsync(id);
            if (mediaFile == null) return false;

            mediaFile.IsDeleted = true;
            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<int> DeleteMediaFilesAsync(IEnumerable<int> ids)
        {
            var mediaFiles = await _context.MediaFiles
                .Where(m => ids.Contains(m.Id))
                .ToListAsync();
            
            foreach (var mediaFile in mediaFiles)
            {
                mediaFile.IsDeleted = true;
            }
            
            return await _context.SaveChangesAsync();
        }

        public async Task<string> GenerateThumbnailAsync(MediaFile mediaFile)
        {
            return await _thumbnailService.GenerateThumbnailAsync(mediaFile);
        }

        public async Task<IEnumerable<MediaFile>> GetFavoriteMediaFilesAsync()
        {
            return await _context.MediaFiles
                .Include(m => m.MediaFileTags)
                    .ThenInclude(mft => mft.Tag)
                .Where(m => m.IsFavorite && !m.IsDeleted)
                .OrderByDescending(m => m.DateAdded)
                .ToListAsync();
        }

        public async Task<bool> SetFavoriteAsync(int mediaFileId, bool isFavorite)
        {
            var mediaFile = await _context.MediaFiles.FindAsync(mediaFileId);
            if (mediaFile == null) return false;

            mediaFile.IsFavorite = isFavorite;
            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<string> CalculateFileHashAsync(string filePath)
        {
            using var md5 = MD5.Create();
            await using var stream = File.OpenRead(filePath);
            var hash = await md5.ComputeHashAsync(stream);
            return Convert.ToHexString(hash);
        }

        public async Task<bool> IsDuplicateAsync(string fileHash)
        {
            return await _context.MediaFiles.AnyAsync(m => m.FileHash == fileHash && !m.IsDeleted);
        }

        public async Task ImportDirectoryAsync(string directoryPath, IProgress<string>? progress = null)
        {
            if (!Directory.Exists(directoryPath))
                throw new DirectoryNotFoundException($"Directory not found: {directoryPath}");

            var supportedExtensions = _imageExtensions.Concat(_videoExtensions).Concat(_audioExtensions).ToHashSet();
            
            var files = Directory.GetFiles(directoryPath, "*.*", SearchOption.AllDirectories)
                .Where(f => supportedExtensions.Contains(Path.GetExtension(f).ToLowerInvariant()))
                .ToList();

            for (int i = 0; i < files.Count; i++)
            {
                try
                {
                    progress?.Report($"Processing {Path.GetFileName(files[i])} ({i + 1}/{files.Count})");
                    await AddMediaFileAsync(files[i]);
                }
                catch (InvalidOperationException)
                {
                    // Skip duplicates
                }
                catch (Exception ex)
                {
                    progress?.Report($"Error processing {Path.GetFileName(files[i])}: {ex.Message}");
                }
            }
        }

        private MediaType GetMediaType(string extension)
        {
            if (_imageExtensions.Contains(extension)) return MediaType.Image;
            if (_videoExtensions.Contains(extension)) return MediaType.Video;
            if (_audioExtensions.Contains(extension)) return MediaType.Audio;
            return MediaType.Other;
        }

        private async Task<DateTime?> GetDateTakenAsync(string filePath)
        {
            try
            {
                // Try to get EXIF date for images
                // This is a simplified implementation - you might want to use a proper EXIF library
                var fileInfo = new FileInfo(filePath);
                return fileInfo.CreationTime;
            }
            catch
            {
                return null;
            }
        }

        private async Task PopulateMediaPropertiesAsync(MediaFile mediaFile)
        {
            try
            {
                if (mediaFile.MediaType == MediaType.Image)
                {
                    // Get image dimensions
                    using var image = System.Drawing.Image.FromFile(mediaFile.VaultPath);
                    mediaFile.Width = image.Width;
                    mediaFile.Height = image.Height;
                }
                else if (mediaFile.MediaType == MediaType.Video)
                {
                    // Use FFMpeg to get video properties
                    // This is simplified - you'd need to implement proper FFMpeg integration
                    mediaFile.Width = 1920; // Placeholder
                    mediaFile.Height = 1080; // Placeholder
                    mediaFile.Duration = TimeSpan.FromMinutes(2); // Placeholder
                }
            }
            catch
            {
                // Ignore errors in property extraction
            }
        }

        public async Task<bool> AddTagToMediaFileAsync(int mediaFileId, int tagId)
        {
            try
            {
                var mediaFile = await _context.MediaFiles
                    .Include(m => m.MediaFileTags)
                    .FirstOrDefaultAsync(m => m.Id == mediaFileId);
                
                if (mediaFile == null) return false;

                // Check if tag is already assigned
                if (mediaFile.MediaFileTags.Any(mft => mft.TagId == tagId))
                    return false;

                var tag = await _context.Tags.FindAsync(tagId);
                if (tag == null) return false;

                var mediaFileTag = new MediaFileTag
                {
                    MediaFileId = mediaFileId,
                    TagId = tagId
                };

                _context.MediaFileTags.Add(mediaFileTag);
                
                // Update tag usage count
                tag.UsageCount++;
                
                return await _context.SaveChangesAsync() > 0;
            }
            catch
            {
                return false;
            }
        }

        public async Task<bool> RemoveTagFromMediaFileAsync(int mediaFileId, int tagId)
        {
            try
            {
                var mediaFileTag = await _context.MediaFileTags
                    .FirstOrDefaultAsync(mft => mft.MediaFileId == mediaFileId && mft.TagId == tagId);
                
                if (mediaFileTag == null) return false;

                _context.MediaFileTags.Remove(mediaFileTag);
                
                // Update tag usage count
                var tag = await _context.Tags.FindAsync(tagId);
                if (tag != null && tag.UsageCount > 0)
                {
                    tag.UsageCount--;
                }
                
                return await _context.SaveChangesAsync() > 0;
            }
            catch
            {
                return false;
            }
        }
    }
}