using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System.IO;
using System.Windows;
using WindowsVault.Data;
using WindowsVault.Services;
using WindowsVault.ViewModels;
using WindowsVault.Views;
using Serilog;

namespace WindowsVault
{
    public partial class App : Application
    {
        private IHost? _host;

        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);
            
            try 
            {
                // Initialize the full application with dependency injection
                InitializeApplication();
            }
            catch (System.Exception ex)
            {
                MessageBox.Show($"Startup Error: {ex.Message}\n\nDetails: {ex.InnerException?.Message}\n\nStack: {ex.StackTrace}", 
                    "Windows Vault Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
        
        private async void InitializeApplication()
        {
            try
            {
                var builder = Host.CreateApplicationBuilder();

                // Configuration
                builder.Configuration.AddJsonFile("appsettings.json", optional: false, reloadOnChange: true);

                // Configure Serilog
                Log.Logger = new LoggerConfiguration()
                    .MinimumLevel.Information()
                    .WriteTo.Console()
                    .WriteTo.File("logs/vault-.log", rollingInterval: RollingInterval.Day)
                    .CreateLogger();

                builder.Services.AddSerilog();
                builder.Services.AddSingleton(Log.Logger);
                builder.Services.AddSingleton<ILoggingService, LoggingService>();

                // Database
                var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
                builder.Services.AddDbContext<VaultDbContext>(options =>
                    options.UseSqlite(connectionString), ServiceLifetime.Transient);

                // Services
                builder.Services.AddTransient<IMediaFileService, MediaFileService>();
                builder.Services.AddTransient<ITagService, TagService>();
                builder.Services.AddSingleton<IThumbnailService, ThumbnailService>();
                builder.Services.AddSingleton<IFileSystemService, FileSystemService>();
                builder.Services.AddSingleton<ISettingsService, SettingsService>();
                builder.Services.AddSingleton<IBackupService, BackupService>();

                // ViewModels
                builder.Services.AddTransient<MainViewModel>();
                builder.Services.AddTransient<MediaLibraryViewModel>();
                builder.Services.AddTransient<TagManagementViewModel>();
                builder.Services.AddTransient<MediaDetailViewModel>();
                builder.Services.AddTransient<SettingsViewModel>();
                builder.Services.AddTransient<BulkTagAssignmentViewModel>();

                // Views
                builder.Services.AddTransient<MainWindow>();
                builder.Services.AddTransient<TagManagementWindow>();
                builder.Services.AddTransient<MediaDetailWindow>();
                builder.Services.AddTransient<SettingsWindow>();

                _host = builder.Build();

                Log.Information("Windows Vault application starting...");

                // Initialize database synchronously to avoid issues
                using (var scope = _host.Services.CreateScope())
                {
                    var context = scope.ServiceProvider.GetRequiredService<VaultDbContext>();
                    context.Database.EnsureCreated();
                    
                    // Cleanup previously soft-deleted records after switching to hard delete
                    var mediaFileService = scope.ServiceProvider.GetRequiredService<IMediaFileService>();
                    var cleanedUpCount = await mediaFileService.CleanupDeletedRecordsAsync();
                    if (cleanedUpCount > 0)
                    {
                        Log.Information($"Cleaned up {cleanedUpCount} previously deleted media file records");
                    }
                }

                // Show main window
                var mainWindow = _host.Services.GetRequiredService<MainWindow>();
                mainWindow.Show();
            }
            catch (System.Exception ex)
            {
                MessageBox.Show($"Initialization Error: {ex.Message}\n\nDetails: {ex.InnerException?.Message}\n\nStack: {ex.StackTrace}", 
                    "Windows Vault Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        protected override async void OnExit(ExitEventArgs e)
        {
            Log.Information("Windows Vault application shutting down...");

            if (_host != null)
            {
                await _host.StopAsync();
                _host.Dispose();
            }

            Log.CloseAndFlush();
            base.OnExit(e);
        }
    }
}