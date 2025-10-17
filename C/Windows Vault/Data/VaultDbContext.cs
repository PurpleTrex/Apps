using Microsoft.EntityFrameworkCore;
using WindowsVault.Models;

namespace WindowsVault.Data
{
    public class VaultDbContext : DbContext
    {
        public VaultDbContext(DbContextOptions<VaultDbContext> options) : base(options)
        {
        }

        public DbSet<MediaFile> MediaFiles { get; set; }
        public DbSet<Tag> Tags { get; set; }
        public DbSet<MediaFileTag> MediaFileTags { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Configure MediaFile entity
            modelBuilder.Entity<MediaFile>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.FileName).IsRequired().HasMaxLength(500);
                entity.Property(e => e.FilePath).IsRequired().HasMaxLength(1000);
                entity.Property(e => e.VaultPath).IsRequired().HasMaxLength(1000);
                entity.Property(e => e.FileExtension).IsRequired().HasMaxLength(10);
                entity.Property(e => e.FileHash).HasMaxLength(32);
                entity.Property(e => e.Description).HasMaxLength(1000);
                entity.Property(e => e.ThumbnailPath).HasMaxLength(500);
                
                entity.HasIndex(e => e.FileHash);
                entity.HasIndex(e => e.FileName);
                entity.HasIndex(e => e.MediaType);
                entity.HasIndex(e => e.DateAdded);
                entity.HasIndex(e => e.IsFavorite);
                entity.HasIndex(e => e.IsDeleted);
            });

            // Configure Tag entity
            modelBuilder.Entity<Tag>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Name).IsRequired().HasMaxLength(100);
                entity.Property(e => e.Color).HasMaxLength(7);
                entity.Property(e => e.Description).HasMaxLength(500);
                
                entity.HasIndex(e => e.Name).IsUnique();
            });

            // Configure MediaFileTag many-to-many relationship
            modelBuilder.Entity<MediaFileTag>(entity =>
            {
                entity.HasKey(e => e.Id);
                
                entity.HasOne(e => e.MediaFile)
                    .WithMany(e => e.MediaFileTags)
                    .HasForeignKey(e => e.MediaFileId)
                    .OnDelete(DeleteBehavior.Cascade);
                
                entity.HasOne(e => e.Tag)
                    .WithMany(e => e.MediaFileTags)
                    .HasForeignKey(e => e.TagId)
                    .OnDelete(DeleteBehavior.Cascade);
                
                entity.HasIndex(e => new { e.MediaFileId, e.TagId }).IsUnique();
            });
        }
    }
}