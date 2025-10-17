using System.Collections.Generic;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    public interface ISettingsService
    {
        Task<T> GetSettingAsync<T>(string key, T defaultValue = default!);
        Task SetSettingAsync<T>(string key, T value);
        Task<string> GetVaultPathAsync();
        Task SetVaultPathAsync(string path);
        Task<bool> GetAutoBackupEnabledAsync();
        Task SetAutoBackupEnabledAsync(bool enabled);
        Task<int> GetBackupIntervalHoursAsync();
        Task SetBackupIntervalHoursAsync(int hours);
        Task<int> GetThumbnailSizeAsync();
        Task SetThumbnailSizeAsync(int size);
        Task<bool> GetDarkModeEnabledAsync();
        Task SetDarkModeEnabledAsync(bool enabled);
        Task ResetToDefaultsAsync();
        Task<Dictionary<string, object>> GetAllSettingsAsync();
    }
}