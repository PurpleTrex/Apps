using System.Threading.Tasks;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public interface IThumbnailService
    {
        Task<string> GenerateThumbnailAsync(MediaFile mediaFile);
        Task<string> GenerateThumbnailAsync(string filePath, MediaType mediaType);
        Task<bool> ThumbnailExistsAsync(string thumbnailPath);
        Task DeleteThumbnailAsync(string thumbnailPath);
        string GetThumbnailDirectory();
    }
}