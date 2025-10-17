using Microsoft.Extensions.Configuration;
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Threading.Tasks;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public class ThumbnailService : IThumbnailService
    {
        private readonly IConfiguration _configuration;
        private readonly string _thumbnailDirectory;
        private readonly int _thumbnailSize;

        public ThumbnailService(IConfiguration configuration)
        {
            _configuration = configuration;
            _thumbnailSize = _configuration.GetValue<int>("VaultSettings:ThumbnailSize", 200);
            
            var vaultPath = Environment.ExpandEnvironmentVariables(
                _configuration.GetValue<string>("VaultSettings:DefaultVaultPath") ?? 
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "WindowsVault"));
            
            _thumbnailDirectory = Path.Combine(vaultPath, "Thumbnails");
            Directory.CreateDirectory(_thumbnailDirectory);
        }

        public async Task<string> GenerateThumbnailAsync(MediaFile mediaFile)
        {
            return await GenerateThumbnailAsync(mediaFile.VaultPath, mediaFile.MediaType);
        }

        public async Task<string> GenerateThumbnailAsync(string filePath, MediaType mediaType)
        {
            if (!File.Exists(filePath))
                throw new FileNotFoundException($"Source file not found: {filePath}");

            var fileHash = Path.GetFileNameWithoutExtension(filePath) + "_" + 
                          Path.GetFileNameWithoutExtension(Path.GetRandomFileName());
            var thumbnailPath = Path.Combine(_thumbnailDirectory, $"{fileHash}.jpg");

            try
            {
                switch (mediaType)
                {
                    case MediaType.Image:
                        await GenerateImageThumbnailAsync(filePath, thumbnailPath);
                        break;
                    case MediaType.Video:
                        await GenerateVideoThumbnailAsync(filePath, thumbnailPath);
                        break;
                    case MediaType.Audio:
                        await GenerateAudioThumbnailAsync(filePath, thumbnailPath);
                        break;
                    default:
                        await GenerateDefaultThumbnailAsync(thumbnailPath);
                        break;
                }

                return thumbnailPath;
            }
            catch (Exception ex)
            {
                // If thumbnail generation fails, create a default thumbnail
                await GenerateDefaultThumbnailAsync(thumbnailPath);
                return thumbnailPath;
            }
        }

        public async Task<bool> ThumbnailExistsAsync(string thumbnailPath)
        {
            return File.Exists(thumbnailPath);
        }

        public async Task DeleteThumbnailAsync(string thumbnailPath)
        {
            if (File.Exists(thumbnailPath))
            {
                File.Delete(thumbnailPath);
            }
        }

        public string GetThumbnailDirectory()
        {
            return _thumbnailDirectory;
        }

        private async Task GenerateImageThumbnailAsync(string imagePath, string thumbnailPath)
        {
            await Task.Run(() =>
            {
                using var originalImage = Image.FromFile(imagePath);
                using var thumbnail = CreateThumbnail(originalImage, _thumbnailSize, _thumbnailSize);
                thumbnail.Save(thumbnailPath, ImageFormat.Jpeg);
            });
        }

        private async Task GenerateVideoThumbnailAsync(string videoPath, string thumbnailPath)
        {
            // This is a placeholder - in a real implementation, you'd use FFMpeg to extract a frame
            // For now, we'll create a default video thumbnail
            await Task.Run(() =>
            {
                using var bitmap = new Bitmap(_thumbnailSize, _thumbnailSize);
                using var graphics = Graphics.FromImage(bitmap);
                
                graphics.FillRectangle(Brushes.Black, 0, 0, _thumbnailSize, _thumbnailSize);
                
                // Draw a play button
                var playButtonSize = _thumbnailSize / 3;
                var playButtonX = (_thumbnailSize - playButtonSize) / 2;
                var playButtonY = (_thumbnailSize - playButtonSize) / 2;
                
                var points = new Point[]
                {
                    new Point(playButtonX, playButtonY),
                    new Point(playButtonX + playButtonSize, playButtonY + playButtonSize / 2),
                    new Point(playButtonX, playButtonY + playButtonSize)
                };
                
                graphics.FillPolygon(Brushes.White, points);
                
                bitmap.Save(thumbnailPath, ImageFormat.Jpeg);
            });
        }

        private async Task GenerateAudioThumbnailAsync(string audioPath, string thumbnailPath)
        {
            await Task.Run(() =>
            {
                using var bitmap = new Bitmap(_thumbnailSize, _thumbnailSize);
                using var graphics = Graphics.FromImage(bitmap);
                
                graphics.FillRectangle(Brushes.DarkBlue, 0, 0, _thumbnailSize, _thumbnailSize);
                
                // Draw a musical note
                using var font = new Font("Segoe UI Symbol", _thumbnailSize / 4);
                var noteText = "♪";
                var textSize = graphics.MeasureString(noteText, font);
                var x = (_thumbnailSize - textSize.Width) / 2;
                var y = (_thumbnailSize - textSize.Height) / 2;
                
                graphics.DrawString(noteText, font, Brushes.White, x, y);
                
                bitmap.Save(thumbnailPath, ImageFormat.Jpeg);
            });
        }

        private async Task GenerateDefaultThumbnailAsync(string thumbnailPath)
        {
            await Task.Run(() =>
            {
                using var bitmap = new Bitmap(_thumbnailSize, _thumbnailSize);
                using var graphics = Graphics.FromImage(bitmap);
                
                graphics.FillRectangle(Brushes.Gray, 0, 0, _thumbnailSize, _thumbnailSize);
                
                // Draw a document icon
                using var font = new Font("Segoe UI Symbol", _thumbnailSize / 4);
                var iconText = "📄";
                var textSize = graphics.MeasureString(iconText, font);
                var x = (_thumbnailSize - textSize.Width) / 2;
                var y = (_thumbnailSize - textSize.Height) / 2;
                
                graphics.DrawString(iconText, font, Brushes.White, x, y);
                
                bitmap.Save(thumbnailPath, ImageFormat.Jpeg);
            });
        }

        private static Image CreateThumbnail(Image original, int maxWidth, int maxHeight)
        {
            int width, height;
            
            if (original.Width > original.Height)
            {
                width = maxWidth;
                height = (int)(original.Height * (float)maxWidth / original.Width);
            }
            else
            {
                height = maxHeight;
                width = (int)(original.Width * (float)maxHeight / original.Height);
            }

            var thumbnail = new Bitmap(width, height);
            using var graphics = Graphics.FromImage(thumbnail);
            
            graphics.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
            graphics.DrawImage(original, 0, 0, width, height);
            
            return thumbnail;
        }
    }
}