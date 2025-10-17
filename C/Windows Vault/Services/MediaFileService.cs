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
using FFMpegCore;
using MetadataExtractor;
using MetadataExtractor.Formats.Exif;
using MetadataExtractor.Formats.Exif.Makernotes;

namespace WindowsVault.Services
{
    /// <summary>
    /// Service for managing media files in the vault.
    /// </summary>
    public class MediaFileService : IMediaFileService
    {
        private readonly VaultDbContext _context;
        private readonly IConfiguration _configuration;
        private readonly IThumbnailService _thumbnailService;
        private readonly IFileSystemService _fileSystemService;
        private readonly ILoggingService _logger;

        private readonly HashSet<string> _imageExtensions;
        private readonly HashSet<string> _videoExtensions;
        private readonly HashSet<string> _audioExtensions;
        private readonly long _maxFileSize;

        public MediaFileService(
            VaultDbContext context,
            IConfiguration configuration,
            IThumbnailService thumbnailService,
            IFileSystemService fileSystemService,
            ILoggingService logger)
        {
            _context = context;
            _configuration = configuration;
            _thumbnailService = thumbnailService;
            _fileSystemService = fileSystemService;
            _logger = logger;

            _imageExtensions = new HashSet<string>(
                _configuration.GetSection("VaultSettings:SupportedImageFormats").Get<string[]>() ?? Array.Empty<string>(),
                StringComparer.OrdinalIgnoreCase);

            _videoExtensions = new HashSet<string>(
                _configuration.GetSection("VaultSettings:SupportedVideoFormats").Get<string[]>() ?? Array.Empty<string>(),
                StringComparer.OrdinalIgnoreCase);

            _audioExtensions = new HashSet<string>(
                _configuration.GetSection("VaultSettings:SupportedAudioFormats").Get<string[]>() ?? Array.Empty<string>(),
                StringComparer.OrdinalIgnoreCase);

            _maxFileSize = _configuration.GetValue<long>("VaultSettings:MaxFileSize", 2147483648); // 2GB default
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

        /// <summary>
        /// Adds a media file to the vault.
        /// </summary>
        public async Task<MediaFile> AddMediaFileAsync(string filePath, bool copyToVault = true)
        {
            try
            {
                // Input validation - prevent null or empty paths
                if (string.IsNullOrWhiteSpace(filePath))
                    throw new ArgumentException("File path cannot be null or empty", nameof(filePath));

                // Validate file path to prevent path traversal attacks
                var fullPath = Path.GetFullPath(filePath);
                if (!fullPath.Equals(filePath, StringComparison.OrdinalIgnoreCase))
                {
                    _logger.LogWarning("Potential path traversal attempt detected");
                    throw new ArgumentException("Invalid file path", nameof(filePath));
                }

                if (!File.Exists(filePath))
                    throw new FileNotFoundException($"File not found: {Path.GetFileName(filePath)}");

                var fileInfo = new FileInfo(filePath);

                // Validate file size
                if (fileInfo.Length > _maxFileSize)
                {
                    _logger.LogWarning($"File exceeds maximum size: {filePath} ({fileInfo.Length} bytes)");
                    throw new InvalidOperationException($"File size ({fileInfo.Length} bytes) exceeds maximum allowed size ({_maxFileSize} bytes)");
                }

                if (fileInfo.Length == 0)
                {
                    _logger.LogWarning($"File is empty: {filePath}");
                    throw new InvalidOperationException("Cannot add empty file to vault");
                }

                var extension = fileInfo.Extension.ToLowerInvariant();
                var mediaType = GetMediaType(extension);

                _logger.LogInformation($"Adding media file: {filePath} (Type: {mediaType})");

                var fileHash = await CalculateFileHashAsync(filePath);
                if (await IsDuplicateAsync(fileHash))
                {
                    _logger.LogWarning($"Duplicate file detected: {filePath}");
                    throw new InvalidOperationException("This file already exists in the vault");
                }

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
                    DateTaken = await GetDateTakenAsync(filePath, mediaType)
                };

                // Get media dimensions/duration
                await PopulateMediaPropertiesAsync(mediaFile);

                _context.MediaFiles.Add(mediaFile);
                await _context.SaveChangesAsync();

                // Generate thumbnail in background
                _ = Task.Run(async () =>
                {
                    try
                    {
                        mediaFile.ThumbnailPath = await _thumbnailService.GenerateThumbnailAsync(mediaFile);
                        await _context.SaveChangesAsync();
                        _logger.LogInformation($"Thumbnail generated for: {mediaFile.FileName}");
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError($"Failed to generate thumbnail for: {mediaFile.FileName}", ex);
                    }
                });

                _logger.LogInformation($"Media file added successfully: {mediaFile.FileName}");
                return mediaFile;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error adding media file: {filePath}", ex);
                throw;
            }
        }

        public async Task<bool> UpdateMediaFileAsync(MediaFile mediaFile)
        {
            try
            {
                if (mediaFile == null)
                    throw new ArgumentNullException(nameof(mediaFile));

                if (mediaFile.Id <= 0)
                    throw new ArgumentException("Invalid media file ID", nameof(mediaFile));

                // Validate that the media file exists
                var existingFile = await _context.MediaFiles.FindAsync(mediaFile.Id);
                if (existingFile == null)
                {
                    _logger.LogWarning($"Attempted to update non-existent media file with ID: {mediaFile.Id}");
                    return false;
                }

                _context.MediaFiles.Update(mediaFile);
                return await _context.SaveChangesAsync() > 0;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error updating media file with ID: {mediaFile?.Id}", ex);
                throw;
            }
        }

        public async Task<bool> DeleteMediaFileAsync(int id)
        {
            try
            {
                if (id <= 0)
                    throw new ArgumentException("Invalid media file ID", nameof(id));

                var mediaFile = await _context.MediaFiles.FindAsync(id);
                if (mediaFile == null)
                {
                    _logger.LogWarning($"Attempted to delete non-existent media file with ID: {id}");
                    return false;
                }

                mediaFile.IsDeleted = true;
                var result = await _context.SaveChangesAsync() > 0;

                if (result)
                    _logger.LogInformation($"Media file marked as deleted: ID {id}");

                return result;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error deleting media file with ID: {id}", ex);
                throw;
            }
        }

        public async Task<int> DeleteMediaFilesAsync(IEnumerable<int> ids)
        {
            try
            {
                if (ids == null || !ids.Any())
                    throw new ArgumentException("IDs list cannot be null or empty", nameof(ids));

                // Validate all IDs are positive
                if (ids.Any(id => id <= 0))
                    throw new ArgumentException("All IDs must be positive integers", nameof(ids));

                var mediaFiles = await _context.MediaFiles
                    .Where(m => ids.Contains(m.Id))
                    .ToListAsync();

                if (!mediaFiles.Any())
                {
                    _logger.LogWarning("No media files found for deletion");
                    return 0;
                }

                foreach (var mediaFile in mediaFiles)
                {
                    mediaFile.IsDeleted = true;
                }

                var deletedCount = await _context.SaveChangesAsync();
                _logger.LogInformation($"Batch deleted {deletedCount} media files");

                return deletedCount;
            }
            catch (Exception ex)
            {
                _logger.LogError("Error during batch deletion", ex);
                throw;
            }
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
            try
            {
                if (mediaFileId <= 0)
                    throw new ArgumentException("Invalid media file ID", nameof(mediaFileId));

                var mediaFile = await _context.MediaFiles.FindAsync(mediaFileId);
                if (mediaFile == null)
                {
                    _logger.LogWarning($"Attempted to set favorite on non-existent media file ID: {mediaFileId}");
                    return false;
                }

                mediaFile.IsFavorite = isFavorite;
                var result = await _context.SaveChangesAsync() > 0;

                if (result)
                    _logger.LogInformation($"Media file favorite status updated: ID {mediaFileId}, IsFavorite: {isFavorite}");

                return result;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error setting favorite status for media file ID: {mediaFileId}", ex);
                throw;
            }
        }

        public async Task<bool> SetRatingAsync(int mediaFileId, double rating)
        {
            try
            {
                if (mediaFileId <= 0)
                    throw new ArgumentException("Invalid media file ID", nameof(mediaFileId));

                if (rating < 0 || rating > 5)
                    throw new ArgumentException("Rating must be between 0 and 5", nameof(rating));

                var mediaFile = await _context.MediaFiles.FindAsync(mediaFileId);
                if (mediaFile == null)
                {
                    _logger.LogWarning($"Attempted to set rating on non-existent media file ID: {mediaFileId}");
                    return false;
                }

                mediaFile.Rating = rating;
                var result = await _context.SaveChangesAsync() > 0;

                if (result)
                    _logger.LogInformation($"Media file rating updated: ID {mediaFileId}, Rating: {rating}");

                return result;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error setting rating for media file ID: {mediaFileId}", ex);
                throw;
            }
        }

        /// <summary>
        /// Calculates SHA256 hash of a file for duplicate detection.
        /// </summary>
        public async Task<string> CalculateFileHashAsync(string filePath)
        {
            try
            {
                using var sha256 = SHA256.Create();
                await using var stream = File.OpenRead(filePath);
                var hash = await sha256.ComputeHashAsync(stream);
                return Convert.ToHexString(hash);
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error calculating hash for file: {filePath}", ex);
                throw;
            }
        }

        public async Task<bool> IsDuplicateAsync(string fileHash)
        {
            return await _context.MediaFiles.AnyAsync(m => m.FileHash == fileHash && !m.IsDeleted);
        }

        public async Task ImportDirectoryAsync(string directoryPath, IProgress<string>? progress = null)
        {
            // Input validation
            if (string.IsNullOrWhiteSpace(directoryPath))
                throw new ArgumentException("Directory path cannot be null or empty", nameof(directoryPath));

            // Validate path to prevent directory traversal
            var fullPath = Path.GetFullPath(directoryPath);
            if (!fullPath.Equals(directoryPath, StringComparison.OrdinalIgnoreCase))
            {
                _logger.LogWarning("Potential path traversal attempt in directory import");
                throw new ArgumentException("Invalid directory path", nameof(directoryPath));
            }

            if (!System.IO.Directory.Exists(directoryPath))
                throw new DirectoryNotFoundException($"Directory not found: {Path.GetFileName(directoryPath)}");

            var supportedExtensions = _imageExtensions.Concat(_videoExtensions).Concat(_audioExtensions).ToHashSet();

            var files = System.IO.Directory.GetFiles(directoryPath, "*.*", SearchOption.AllDirectories)
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

        /// <summary>
        /// Extracts the date taken from EXIF data for images or file metadata.
        /// </summary>
        private async Task<DateTime?> GetDateTakenAsync(string filePath, MediaType mediaType)
        {
            try
            {
                if (mediaType == MediaType.Image)
                {
                    // Try to get EXIF date for images
                    try
                    {
                        var directories = ImageMetadataReader.ReadMetadata(filePath);
                        var exifSubDir = directories.OfType<ExifSubIfdDirectory>().FirstOrDefault();
                        if (exifSubDir != null && exifSubDir.TryGetDateTime(ExifDirectoryBase.TagDateTimeOriginal, out var dateTime))
                        {
                            _logger.LogDebug($"EXIF date found for {Path.GetFileName(filePath)}: {dateTime}");
                            return dateTime;
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.LogDebug($"Could not read EXIF data from {Path.GetFileName(filePath)}: {ex.Message}");
                    }
                }

                // Fallback to file creation time
                var fileInfo = new FileInfo(filePath);
                return fileInfo.CreationTime;
            }
            catch (Exception ex)
            {
                _logger.LogWarning($"Error getting date taken for {Path.GetFileName(filePath)}: {ex.Message}");
                return null;
            }
        }

        /// <summary>
        /// Populates media properties like dimensions and duration using FFMpeg and other tools.
        /// </summary>
        private async Task PopulateMediaPropertiesAsync(MediaFile mediaFile)
        {
            try
            {
                if (mediaFile.MediaType == MediaType.Image)
                {
                    // Get image dimensions and EXIF data
                    using var image = System.Drawing.Image.FromFile(mediaFile.VaultPath);
                    mediaFile.Width = image.Width;
                    mediaFile.Height = image.Height;

                    // Extract additional EXIF data
                    try
                    {
                        var directories = ImageMetadataReader.ReadMetadata(mediaFile.VaultPath);
                        var exifIfd0 = directories.OfType<ExifIfd0Directory>().FirstOrDefault();

                        // Try to get camera make/model
                        if (exifIfd0 != null)
                        {
                            var make = exifIfd0.GetString(ExifDirectoryBase.TagMake);
                            if (!string.IsNullOrEmpty(make))
                            {
                                _logger.LogDebug($"Camera make: {make}");
                            }

                            var model = exifIfd0.GetString(ExifDirectoryBase.TagModel);
                            if (!string.IsNullOrEmpty(model))
                            {
                                _logger.LogDebug($"Camera model: {model}");
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.LogDebug($"Could not extract additional EXIF data: {ex.Message}");
                    }
                }
                else if (mediaFile.MediaType == MediaType.Video)
                {
                    // Use FFMpeg to get video properties
                    try
                    {
                        var mediaInfo = await FFProbe.AnalyseAsync(mediaFile.VaultPath);

                        if (mediaInfo.PrimaryVideoStream != null)
                        {
                            mediaFile.Width = mediaInfo.PrimaryVideoStream.Width;
                            mediaFile.Height = mediaInfo.PrimaryVideoStream.Height;
                            mediaFile.Duration = mediaInfo.Duration;

                            _logger.LogInformation($"Video properties extracted: {mediaFile.Width}x{mediaFile.Height}, Duration: {mediaFile.Duration}");
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning($"FFMpeg failed to extract video properties for {mediaFile.FileName}: {ex.Message}");
                    }
                }
                else if (mediaFile.MediaType == MediaType.Audio)
                {
                    // Use FFMpeg to get audio properties
                    try
                    {
                        var mediaInfo = await FFProbe.AnalyseAsync(mediaFile.VaultPath);
                        mediaFile.Duration = mediaInfo.Duration;

                        _logger.LogInformation($"Audio duration extracted: {mediaFile.Duration}");
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning($"FFMpeg failed to extract audio properties for {mediaFile.FileName}: {ex.Message}");
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning($"Error populating media properties for {mediaFile.FileName}: {ex.Message}");
            }
        }

        public async Task<bool> AddTagToMediaFileAsync(int mediaFileId, int tagId)
        {
            try
            {
                // Input validation
                if (mediaFileId <= 0)
                    throw new ArgumentException("Invalid media file ID", nameof(mediaFileId));

                if (tagId <= 0)
                    throw new ArgumentException("Invalid tag ID", nameof(tagId));

                var mediaFile = await _context.MediaFiles
                    .Include(m => m.MediaFileTags)
                    .FirstOrDefaultAsync(m => m.Id == mediaFileId && !m.IsDeleted);

                if (mediaFile == null)
                {
                    _logger.LogWarning($"Attempted to add tag to non-existent media file ID: {mediaFileId}");
                    return false;
                }

                // Check if tag is already assigned
                if (mediaFile.MediaFileTags.Any(mft => mft.TagId == tagId))
                {
                    _logger.LogDebug($"Tag {tagId} already assigned to media file {mediaFileId}");
                    return false;
                }

                var tag = await _context.Tags.FindAsync(tagId);
                if (tag == null)
                {
                    _logger.LogWarning($"Attempted to assign non-existent tag ID: {tagId}");
                    return false;
                }

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
                // Input validation
                if (mediaFileId <= 0)
                    throw new ArgumentException("Invalid media file ID", nameof(mediaFileId));

                if (tagId <= 0)
                    throw new ArgumentException("Invalid tag ID", nameof(tagId));

                var mediaFileTag = await _context.MediaFileTags
                    .FirstOrDefaultAsync(mft => mft.MediaFileId == mediaFileId && mft.TagId == tagId);

                if (mediaFileTag == null)
                {
                    _logger.LogWarning($"Tag {tagId} not found on media file {mediaFileId}");
                    return false;
                }

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