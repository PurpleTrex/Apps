using System.Collections.Generic;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    public interface IFileSystemService
    {
        Task<string> CopyToVaultAsync(string sourceFilePath);
        Task<string> MoveToVaultAsync(string sourceFilePath);
        Task<bool> DeleteFromVaultAsync(string vaultPath);
        Task<string> GetVaultDirectoryAsync();
        Task<bool> EnsureVaultDirectoryExistsAsync();
        Task<long> GetVaultSizeAsync();
        Task<IEnumerable<string>> GetOrphanedFilesAsync();
        Task CleanupOrphanedFilesAsync();
    }
}