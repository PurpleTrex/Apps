using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public interface IMediaFileService
    {
        Task<IEnumerable<MediaFile>> GetAllMediaFilesAsync();
        Task<IEnumerable<MediaFile>> GetMediaFilesByTypeAsync(MediaType mediaType);
        Task<IEnumerable<MediaFile>> SearchMediaFilesAsync(string searchQuery);
        Task<IEnumerable<MediaFile>> GetMediaFilesByTagsAsync(IEnumerable<int> tagIds);
        Task<MediaFile?> GetMediaFileByIdAsync(int id);
        Task<MediaFile> AddMediaFileAsync(string filePath, bool copyToVault = true);
        Task<bool> UpdateMediaFileAsync(MediaFile mediaFile);
        Task<bool> DeleteMediaFileAsync(int id);
        Task<int> DeleteMediaFilesAsync(IEnumerable<int> ids);
        Task<string> GenerateThumbnailAsync(MediaFile mediaFile);
        Task<IEnumerable<MediaFile>> GetFavoriteMediaFilesAsync();
        Task<bool> SetFavoriteAsync(int mediaFileId, bool isFavorite);
        Task<string> CalculateFileHashAsync(string filePath);
        Task<bool> IsDuplicateAsync(string fileHash);
        Task ImportDirectoryAsync(string directoryPath, IProgress<string>? progress = null);
        Task<bool> AddTagToMediaFileAsync(int mediaFileId, int tagId);
        Task<bool> RemoveTagFromMediaFileAsync(int mediaFileId, int tagId);
    }
}