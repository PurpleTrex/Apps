using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
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
            await SetSettingAsync("BackupIntervalHours", hours);
        }

        public async Task<int> GetThumbnailSizeAsync()
        {
            return await GetSettingAsync("ThumbnailSize", 
                _configuration.GetValue<int>("VaultSettings:ThumbnailSize", 200));
        }

        public async Task SetThumbnailSizeAsync(int size)
        {
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