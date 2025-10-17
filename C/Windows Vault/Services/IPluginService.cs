using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace WindowsVault.Services
{
    public interface IPluginService
    {
        Task<IEnumerable<IPlugin>> LoadPluginsAsync();
        Task<bool> ExecutePluginAsync(string pluginId, Dictionary<string, object> parameters);
        Task<bool> RegisterPluginAsync(IPlugin plugin);
        Task<bool> UnregisterPluginAsync(string pluginId);
        IEnumerable<IPlugin> GetLoadedPlugins();
    }

    public interface IPlugin
    {
        string Id { get; }
        string Name { get; }
        string Description { get; }
        string Version { get; }
        string Author { get; }

        Task<bool> InitializeAsync();
        Task<object?> ExecuteAsync(Dictionary<string, object> parameters);
        Task CleanupAsync();
    }

    public enum PluginType
    {
        ImageProcessor,
        VideoProcessor,
        AudioProcessor,
        MetadataExtractor,
        CloudStorage,
        Export,
        Import,
        Custom
    }
}