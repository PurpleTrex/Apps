using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;

namespace WindowsVault.Models
{
    public class Tag
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = string.Empty;

        [MaxLength(7)]
        public string? Color { get; set; } = "#0078D4"; // Default Windows accent color

        [MaxLength(500)]
        public string? Description { get; set; }

        public DateTime DateCreated { get; set; } = DateTime.UtcNow;

        public int UsageCount { get; set; } = 0;

        // Navigation properties
        public virtual ICollection<MediaFileTag> MediaFileTags { get; set; } = new List<MediaFileTag>();
        
        [NotMapped]
        public IEnumerable<MediaFile> MediaFiles => MediaFileTags.Select(mft => mft.MediaFile);

        [NotMapped]
        public bool IsSelected { get; set; } = false;
    }
}