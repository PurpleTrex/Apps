using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Infrastructure.Data.Configurations;

public class DashboardWidgetConfiguration : IEntityTypeConfiguration<DashboardWidget>
{
    public void Configure(EntityTypeBuilder<DashboardWidget> builder)
    {
        builder.ToTable("DashboardWidgets");

        builder.HasKey(w => w.Id);

        builder.Property(w => w.UserId)
            .IsRequired()
            .HasMaxLength(100);

        builder.Property(w => w.Title)
            .IsRequired()
            .HasMaxLength(200);

        builder.Property(w => w.Type)
            .IsRequired()
            .HasConversion<string>()
            .HasMaxLength(50);

        builder.Property(w => w.Configuration)
            .IsRequired()
            .HasColumnType("TEXT"); // JSON stored as TEXT in SQLite

        builder.Property(w => w.PositionX)
            .IsRequired();

        builder.Property(w => w.PositionY)
            .IsRequired();

        builder.Property(w => w.Width)
            .IsRequired();

        builder.Property(w => w.Height)
            .IsRequired();

        builder.Property(w => w.IsVisible)
            .IsRequired()
            .HasDefaultValue(true);

        builder.Property(w => w.DisplayOrder)
            .IsRequired()
            .HasDefaultValue(0);

        builder.Property(w => w.CreatedAt)
            .IsRequired();

        builder.Property(w => w.UpdatedAt)
            .IsRequired();

        // Indexes
        builder.HasIndex(w => w.UserId)
            .HasDatabaseName("IX_DashboardWidgets_UserId");

        builder.HasIndex(w => new { w.UserId, w.DisplayOrder })
            .HasDatabaseName("IX_DashboardWidgets_User_Order");
    }
}
