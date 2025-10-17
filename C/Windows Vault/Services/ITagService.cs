using System.Collections.Generic;
using System.Threading.Tasks;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public interface ITagService
    {
        Task<IEnumerable<Tag>> GetAllTagsAsync();
        Task<Tag?> GetTagByIdAsync(int id);
        Task<Tag?> GetTagByNameAsync(string name);
        Task<Tag> CreateTagAsync(string name, string? color = null, string? description = null);
        Task<bool> UpdateTagAsync(Tag tag);
        Task<bool> DeleteTagAsync(int id);
        Task<bool> AssignTagToMediaFileAsync(int mediaFileId, int tagId);
        Task<bool> RemoveTagFromMediaFileAsync(int mediaFileId, int tagId);
        Task<IEnumerable<Tag>> GetTagsForMediaFileAsync(int mediaFileId);
        Task<IEnumerable<MediaFile>> GetMediaFilesForTagAsync(int tagId);
        Task<bool> TagExistsAsync(string name);
        Task UpdateTagUsageCountsAsync();
        Task<IEnumerable<Tag>> SearchTagsAsync(string searchQuery);
    }
}