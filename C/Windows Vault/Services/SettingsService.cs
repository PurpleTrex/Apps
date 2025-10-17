using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    public class SettingsService : ISettingsService
    {
        private readonly string _settingsFilePath;
        private readonly IConfiguration _configuration;
        private Dictionary<string, object> _settings;

        public SettingsService(IConfiguration configuration)
        {
            _configuration = configuration;
            _settingsFilePath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "WindowsVault",
                "settings.json");
            
            Directory.CreateDirectory(Path.GetDirectoryName(_settingsFilePath)!);
            _settings = LoadSettings();
        }

        public async Task<T> GetSettingAsync<T>(string key, T defaultValue = default!)
        {
            if (_settings.TryGetValue(key, out var value))
            {
                try
                {
                    if (value is T directValue)
                        return directValue;
                    
                    // Try to convert JSON values
                    if (value is string jsonValue)
                    {
                        return JsonConvert.DeserializeObject<T>(jsonValue) ?? defaultValue;
                    }
                    
                    return (T)Convert.ChangeType(value, typeof(T));
                }
                catch
                {
                    return defaultValue;
                }
            }
            
            return defaultValue;
        }

        public async Task SetSettingAsync<T>(string key, T value)
        {
            if (value == null)
            {
                _settings.Remove(key);
            }
            else
            {
                _settings[key] = value;
            }
            
            await SaveSettingsAsync();
        }

        public async Task<string> GetVaultPathAsync()
        {
            var path = await GetSettingAsync<string>("VaultPath");
            if (string.IsNullOrEmpty(path))
            {
                path = Environment.ExpandEnvironmentVariables(
                    _configuration.GetValue<string>("VaultSettings:DefaultVaultPath") ?? 
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "WindowsVault"));
            }
            return path;
        }

        public async Task SetVaultPathAsync(string path)
        {
            // Input validation
            if (string.IsNullOrWhiteSpace(path))
                throw new ArgumentException("Vault path cannot be null or empty", nameof(path));

            // Validate path
            if (!Path.IsPathRooted(path))
                throw new ArgumentException("Vault path must be an absolute path", nameof(path));

            // Ensure directory exists or can be created
            if (!Directory.Exists(path))
            {
                try
                {
                    Directory.CreateDirectory(path);
                }
                catch (Exception ex)
                {
                    throw new InvalidOperationException($"Cannot create vault directory: {path}", ex);
                }
            }

            await SetSettingAsync("VaultPath", path);
        }

        public async Task<bool> GetAutoBackupEnabledAsync()
        {
            return await GetSettingAsync("AutoBackupEnabled", 
                _configuration.GetValue<bool>("VaultSettings:EnableAutoBackup", true));
        }

        public async Task SetAutoBackupEnabledAsync(bool enabled)
        {
            await SetSettingAsync("AutoBackupEnabled", enabled);
        }

        public async Task<int> GetBackupIntervalHoursAsync()
        {
            return await GetSettingAsync("BackupIntervalHours", 
                _configuration.GetValue<int>("VaultSettings:BackupIntervalHours", 24));
        }

        public async Task SetBackupIntervalHoursAsync(int hours)
        {
            // Input validation
            if (hours < 1 || hours > 168) // 1 hour to 1 week
                throw new ArgumentException("Backup interval must be between 1 and 168 hours", nameof(hours));

            await SetSettingAsync("BackupIntervalHours", hours);
        }

        public async Task<int> GetThumbnailSizeAsync()
        {
            return await GetSettingAsync("ThumbnailSize", 
                _configuration.GetValue<int>("VaultSettings:ThumbnailSize", 200));
        }

        public async Task SetThumbnailSizeAsync(int size)
        {
            // Input validation
            if (size < 50 || size > 1000)
                throw new ArgumentException("Thumbnail size must be between 50 and 1000 pixels", nameof(size));

            await SetSettingAsync("ThumbnailSize", size);
        }

        public async Task<bool> GetDarkModeEnabledAsync()
        {
            return await GetSettingAsync("DarkModeEnabled", false);
        }

        public async Task SetDarkModeEnabledAsync(bool enabled)
        {
            await SetSettingAsync("DarkModeEnabled", enabled);
        }

        public async Task ResetToDefaultsAsync()
        {
            _settings.Clear();
            await SaveSettingsAsync();
        }

        public async Task<Dictionary<string, object>> GetAllSettingsAsync()
        {
            return new Dictionary<string, object>(_settings);
        }

        public async Task<List<int>> GetRecentTagIdsAsync()
        {
            var recentTagIds = await GetSettingAsync<List<int>>("RecentTagIds");
            return recentTagIds ?? new List<int>();
        }

        public async Task SaveRecentTagIdsAsync(List<int> tagIds)
        {
            // Keep only the last 20 recent tag IDs
            if (tagIds.Count > 20)
            {
                tagIds = tagIds.Take(20).ToList();
            }
            await SetSettingAsync("RecentTagIds", tagIds);
        }

        private Dictionary<string, object> LoadSettings()
        {
            if (File.Exists(_settingsFilePath))
            {
                try
                {
                    var json = File.ReadAllText(_settingsFilePath);
                    return JsonConvert.DeserializeObject<Dictionary<string, object>>(json) ?? new Dictionary<string, object>();
                }
                catch
                {
                    return new Dictionary<string, object>();
                }
            }
            
            return new Dictionary<string, object>();
        }

        private async Task SaveSettingsAsync()
        {
            try
            {
                var json = JsonConvert.SerializeObject(_settings, Formatting.Indented);
                await File.WriteAllTextAsync(_settingsFilePath, json);
            }
            catch
            {
                // Ignore errors when saving settings
            }
        }
    }
}