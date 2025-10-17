using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace WindowsVault.Models
{
    public class SmartCollection
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = string.Empty;

        [MaxLength(500)]
        public string? Description { get; set; }

        [Required]
        public string RuleSetJson { get; set; } = string.Empty;

        public string Icon { get; set; } = "📁";

        public DateTime DateCreated { get; set; }
        public DateTime DateModified { get; set; }

        public bool IsSystem { get; set; } // For built-in collections like "Recent", "Favorites", etc.
        public bool IsEnabled { get; set; } = true;

        // Navigation properties
        public virtual ICollection<MediaFile> MediaFiles { get; set; } = new List<MediaFile>();
    }

    public class SmartCollectionRule
    {
        public string Field { get; set; } = string.Empty; // FileName, DateAdded, FileSize, Rating, etc.
        public string Operator { get; set; } = string.Empty; // Contains, Equals, GreaterThan, LessThan, Between, etc.
        public string Value { get; set; } = string.Empty;
        public string? Value2 { get; set; } // For "Between" operator
    }

    public class SmartCollectionRuleSet
    {
        public LogicalOperator Operator { get; set; } = LogicalOperator.And;
        public List<SmartCollectionRule> Rules { get; set; } = new();
        public List<SmartCollectionRuleSet>? NestedRuleSets { get; set; }
    }

    public enum LogicalOperator
    {
        And,
        Or
    }

    public enum RuleField
    {
        FileName,
        FileSize,
        DateAdded,
        DateModified,
        MediaType,
        Rating,
        IsFavorite,
        TagName,
        FileExtension,
        Width,
        Height,
        Duration
    }

    public enum RuleOperator
    {
        Equals,
        NotEquals,
        Contains,
        NotContains,
        StartsWith,
        EndsWith,
        GreaterThan,
        LessThan,
        GreaterThanOrEqual,
        LessThanOrEqual,
        Between,
        InLast, // For dates - "in last X days"
        NotInLast,
        IsEmpty,
        IsNotEmpty
    }
}