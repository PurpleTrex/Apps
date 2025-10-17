using System;
using System.ComponentModel.DataAnnotations;

namespace WindowsVault.Models
{
    public class MediaFileTag
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public int MediaFileId { get; set; }

        [Required]
        public int TagId { get; set; }

        public DateTime DateAssigned { get; set; } = DateTime.UtcNow;

        // Navigation properties
        public virtual MediaFile MediaFile { get; set; } = null!;
        public virtual Tag Tag { get; set; } = null!;
    }
}