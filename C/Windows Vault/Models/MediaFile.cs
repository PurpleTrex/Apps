using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;

namespace WindowsVault.Models
{
    public class MediaFile
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(500)]
        public string FileName { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string FilePath { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string VaultPath { get; set; } = string.Empty;

        [Required]
        [MaxLength(10)]
        public string FileExtension { get; set; } = string.Empty;

        [Required]
        public MediaType MediaType { get; set; }

        public long FileSizeBytes { get; set; }

        [MaxLength(32)]
        public string? FileHash { get; set; }

        public DateTime DateAdded { get; set; } = DateTime.UtcNow;

        public DateTime DateModified { get; set; } = DateTime.UtcNow;

        public DateTime? DateTaken { get; set; }

        public int? Width { get; set; }
        
        public int? Height { get; set; }

        public TimeSpan? Duration { get; set; }

        [MaxLength(1000)]
        public string? Description { get; set; }

        public double Rating { get; set; } = 0.0;

        public bool IsFavorite { get; set; } = false;

        public bool IsDeleted { get; set; } = false;

        [MaxLength(500)]
        public string? ThumbnailPath { get; set; }

        // Navigation properties
        public virtual ICollection<MediaFileTag> MediaFileTags { get; set; } = new List<MediaFileTag>();
        
        [NotMapped]
        public IEnumerable<Tag> Tags => MediaFileTags.Select(mft => mft.Tag);

        [NotMapped]
        public bool IsSelected { get; set; } = false;

        [NotMapped]
        public bool IsSelectionMode { get; set; } = false;

        [NotMapped]
        public string FileSizeFormatted
        {
            get
            {
                string[] sizes = { "B", "KB", "MB", "GB", "TB" };
                int order = 0;
                double size = FileSizeBytes;

                while (size >= 1024 && order < sizes.Length - 1)
                {
                    order++;
                    size /= 1024;
                }

                return $"{size:0.##} {sizes[order]}";
            }
        }
    }

    public enum MediaType
    {
        Image = 1,
        Video = 2,
        Audio = 3,
        Document = 4,
        Other = 5
    }
}