using System.Collections.Generic;
using System.Threading.Tasks;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public interface ISmartCollectionService
    {
        Task<SmartCollection> CreateSmartCollectionAsync(string name, SmartCollectionRuleSet ruleSet, string? description = null);
        Task<SmartCollection?> GetSmartCollectionAsync(int id);
        Task<IEnumerable<SmartCollection>> GetAllSmartCollectionsAsync();
        Task<bool> UpdateSmartCollectionAsync(SmartCollection collection);
        Task<bool> DeleteSmartCollectionAsync(int id);
        Task<IEnumerable<MediaFile>> GetMediaFilesForCollectionAsync(int collectionId);
        Task<IEnumerable<MediaFile>> EvaluateRuleSetAsync(SmartCollectionRuleSet ruleSet);
        Task InitializeDefaultCollectionsAsync();
    }
}