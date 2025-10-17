using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public interface IMetadataExtractionService
    {
        Task<ImageMetadata?> ExtractImageMetadataAsync(string filePath);
        Task<VideoMetadata?> ExtractVideoMetadataAsync(string filePath);
        Task<AudioMetadata?> ExtractAudioMetadataAsync(string filePath);
        Task<Dictionary<string, string>> ExtractExifDataAsync(string filePath);
        Task<bool> UpdateMediaFileMetadataAsync(int mediaFileId);
    }

    public class ImageMetadata
    {
        public int Width { get; set; }
        public int Height { get; set; }
        public string? CameraModel { get; set; }
        public string? CameraMake { get; set; }
        public DateTime? DateTaken { get; set; }
        public double? Latitude { get; set; }
        public double? Longitude { get; set; }
        public string? ISO { get; set; }
        public string? FNumber { get; set; }
        public string? ExposureTime { get; set; }
        public string? FocalLength { get; set; }
        public Dictionary<string, string> ExifData { get; set; } = new();
    }

    public class VideoMetadata
    {
        public int Width { get; set; }
        public int Height { get; set; }
        public TimeSpan Duration { get; set; }
        public double FrameRate { get; set; }
        public string? VideoCodec { get; set; }
        public string? AudioCodec { get; set; }
        public long Bitrate { get; set; }
        public DateTime? DateCreated { get; set; }
    }

    public class AudioMetadata
    {
        public TimeSpan Duration { get; set; }
        public int Bitrate { get; set; }
        public int SampleRate { get; set; }
        public int Channels { get; set; }
        public string? Artist { get; set; }
        public string? Album { get; set; }
        public string? Title { get; set; }
        public string? Genre { get; set; }
        public int? Year { get; set; }
        public int? TrackNumber { get; set; }
    }
}