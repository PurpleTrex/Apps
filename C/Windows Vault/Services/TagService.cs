using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WindowsVault.Data;
using WindowsVault.Models;

namespace WindowsVault.Services
{
    public class TagService : ITagService
    {
        private readonly VaultDbContext _context;

        public TagService(VaultDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<Tag>> GetAllTagsAsync()
        {
            return await _context.Tags
                .OrderBy(t => t.Name)
                .ToListAsync();
        }

        public async Task<Tag?> GetTagByIdAsync(int id)
        {
            return await _context.Tags
                .Include(t => t.MediaFileTags)
                    .ThenInclude(mft => mft.MediaFile)
                .FirstOrDefaultAsync(t => t.Id == id);
        }

        public async Task<Tag?> GetTagByNameAsync(string name)
        {
            return await _context.Tags
                .FirstOrDefaultAsync(t => t.Name.ToLower() == name.ToLower());
        }

        public async Task<Tag> CreateTagAsync(string name, string? color = null, string? description = null)
        {
            // Input validation
            if (string.IsNullOrWhiteSpace(name))
                throw new ArgumentException("Tag name cannot be null or empty", nameof(name));

            if (name.Length > 100)
                throw new ArgumentException("Tag name cannot exceed 100 characters", nameof(name));

            // Validate color format if provided
            if (!string.IsNullOrEmpty(color) && !System.Text.RegularExpressions.Regex.IsMatch(color, @"^#[0-9A-Fa-f]{6}$"))
                throw new ArgumentException("Color must be in hex format (#RRGGBB)", nameof(color));

            // Validate description length
            if (description != null && description.Length > 500)
                throw new ArgumentException("Description cannot exceed 500 characters", nameof(description));

            if (await TagExistsAsync(name))
                throw new InvalidOperationException($"Tag '{name}' already exists");

            var tag = new Tag
            {
                Name = name.Trim(),
                Color = color ?? GenerateRandomColor(),
                Description = description?.Trim(),
                DateCreated = DateTime.UtcNow,
                UsageCount = 0
            };

            _context.Tags.Add(tag);
            await _context.SaveChangesAsync();
            return tag;
        }

        public async Task<bool> UpdateTagAsync(Tag tag)
        {
            // Input validation
            if (tag == null)
                throw new ArgumentNullException(nameof(tag));

            if (tag.Id <= 0)
                throw new ArgumentException("Invalid tag ID", nameof(tag));

            if (string.IsNullOrWhiteSpace(tag.Name))
                throw new ArgumentException("Tag name cannot be null or empty", nameof(tag));

            if (tag.Name.Length > 100)
                throw new ArgumentException("Tag name cannot exceed 100 characters", nameof(tag));

            // Validate color format
            if (!string.IsNullOrEmpty(tag.Color) && !System.Text.RegularExpressions.Regex.IsMatch(tag.Color, @"^#[0-9A-Fa-f]{6}$"))
                throw new ArgumentException("Color must be in hex format (#RRGGBB)", nameof(tag));

            var existingTag = await _context.Tags.FindAsync(tag.Id);
            if (existingTag == null) return false;

            // Check if name is being changed to one that already exists
            var duplicateTag = await _context.Tags
                .FirstOrDefaultAsync(t => t.Name.ToLower() == tag.Name.ToLower() && t.Id != tag.Id);

            if (duplicateTag != null)
                throw new InvalidOperationException($"A tag with name '{tag.Name}' already exists");

            existingTag.Name = tag.Name.Trim();
            existingTag.Color = tag.Color;
            existingTag.Description = tag.Description?.Trim();

            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<bool> DeleteTagAsync(int id)
        {
            // Input validation
            if (id <= 0)
                throw new ArgumentException("Invalid tag ID", nameof(id));

            var tag = await _context.Tags.FindAsync(id);
            if (tag == null) return false;

            // Remove all associations first
            var associations = await _context.MediaFileTags
                .Where(mft => mft.TagId == id)
                .ToListAsync();

            _context.MediaFileTags.RemoveRange(associations);
            _context.Tags.Remove(tag);

            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<bool> AssignTagToMediaFileAsync(int mediaFileId, int tagId)
        {
            var mediaFile = await _context.MediaFiles.FindAsync(mediaFileId);
            var tag = await _context.Tags.FindAsync(tagId);
            
            if (mediaFile == null || tag == null) return false;

            var existingAssociation = await _context.MediaFileTags
                .FirstOrDefaultAsync(mft => mft.MediaFileId == mediaFileId && mft.TagId == tagId);

            if (existingAssociation != null) return true; // Already associated

            var association = new MediaFileTag
            {
                MediaFileId = mediaFileId,
                TagId = tagId,
                DateAssigned = DateTime.UtcNow
            };

            _context.MediaFileTags.Add(association);
            
            // Update usage count
            tag.UsageCount++;
            
            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<bool> RemoveTagFromMediaFileAsync(int mediaFileId, int tagId)
        {
            var association = await _context.MediaFileTags
                .FirstOrDefaultAsync(mft => mft.MediaFileId == mediaFileId && mft.TagId == tagId);

            if (association == null) return false;

            _context.MediaFileTags.Remove(association);

            // Update usage count
            var tag = await _context.Tags.FindAsync(tagId);
            if (tag != null && tag.UsageCount > 0)
            {
                tag.UsageCount--;
            }

            return await _context.SaveChangesAsync() > 0;
        }

        public async Task<IEnumerable<Tag>> GetTagsForMediaFileAsync(int mediaFileId)
        {
            return await _context.MediaFileTags
                .Where(mft => mft.MediaFileId == mediaFileId)
                .Include(mft => mft.Tag)
                .Select(mft => mft.Tag)
                .ToListAsync();
        }

        public async Task<IEnumerable<MediaFile>> GetMediaFilesForTagAsync(int tagId)
        {
            return await _context.MediaFileTags
                .Where(mft => mft.TagId == tagId)
                .Include(mft => mft.MediaFile)
                .Select(mft => mft.MediaFile)
                .ToListAsync();
        }

        public async Task<bool> TagExistsAsync(string name)
        {
            return await _context.Tags
                .AnyAsync(t => t.Name.ToLower() == name.ToLower());
        }

        public async Task UpdateTagUsageCountsAsync()
        {
            var tagUsageCounts = await _context.MediaFileTags
                .GroupBy(mft => mft.TagId)
                .Select(g => new { TagId = g.Key, Count = g.Count() })
                .ToListAsync();

            foreach (var tagUsage in tagUsageCounts)
            {
                var tag = await _context.Tags.FindAsync(tagUsage.TagId);
                if (tag != null)
                {
                    tag.UsageCount = tagUsage.Count;
                }
            }

            await _context.SaveChangesAsync();
        }

        public async Task<IEnumerable<Tag>> SearchTagsAsync(string searchQuery)
        {
            if (string.IsNullOrWhiteSpace(searchQuery))
                return await GetAllTagsAsync();

            var query = searchQuery.ToLower();
            return await _context.Tags
                .Where(t => t.Name.ToLower().Contains(query) || 
                           (t.Description != null && t.Description.ToLower().Contains(query)))
                .OrderBy(t => t.Name)
                .ToListAsync();
        }

        private string GenerateRandomColor()
        {
            var colors = new[]
            {
                "#0078D4", // Windows Blue
                "#107C10", // Green
                "#FF8C00", // Orange
                "#E74856", // Red
                "#881798", // Purple
                "#00BCF2", // Cyan
                "#FFB900", // Yellow
                "#00CC6A", // Light Green
                "#FF4343", // Light Red
                "#9A0089"  // Dark Purple
            };

            var random = new Random();
            return colors[random.Next(colors.Length)];
        }
    }
}