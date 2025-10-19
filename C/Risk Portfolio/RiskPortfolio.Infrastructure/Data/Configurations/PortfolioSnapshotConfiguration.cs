using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Infrastructure.Data.Configurations;

public class PortfolioSnapshotConfiguration : IEntityTypeConfiguration<PortfolioSnapshot>
{
    public void Configure(EntityTypeBuilder<PortfolioSnapshot> builder)
    {
        builder.ToTable("PortfolioSnapshots");

        builder.HasKey(ps => ps.Id);

        builder.Property(ps => ps.PortfolioId).IsRequired();
        builder.Property(ps => ps.SnapshotDate).IsRequired();
        builder.Property(ps => ps.TotalValue).HasColumnType("decimal(18,2)").IsRequired();
        builder.Property(ps => ps.RiskScore).HasColumnType("decimal(18,2)").IsRequired();
        builder.Property(ps => ps.ValueAtRisk).HasColumnType("decimal(18,2)").IsRequired();
        builder.Property(ps => ps.PositionCount).IsRequired();
        builder.Property(ps => ps.DailyReturn).HasColumnType("decimal(18,4)").IsRequired();
        builder.Property(ps => ps.CreatedAt).IsRequired();

        builder.HasOne(ps => ps.Portfolio)
            .WithMany()
            .HasForeignKey(ps => ps.PortfolioId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasIndex(ps => new { ps.PortfolioId, ps.SnapshotDate })
            .IsUnique()
            .HasDatabaseName("IX_PortfolioSnapshots_Portfolio_Date");

        builder.HasIndex(ps => ps.SnapshotDate)
            .HasDatabaseName("IX_PortfolioSnapshots_Date");
    }
}
