using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Infrastructure.Data.Configurations;

public class PortfolioEntityTypeConfiguration : IEntityTypeConfiguration<Portfolio>
{
    public void Configure(EntityTypeBuilder<Portfolio> builder)
    {
        builder.ToTable("Portfolios", schema: "portfolio");

        builder.HasKey(p => p.Id);

        builder.Property(p => p.Name)
            .HasMaxLength(200)
            .IsRequired();

        builder.Property(p => p.Owner)
            .HasMaxLength(200)
            .IsRequired();

        builder.Property(p => p.BaseCurrency)
            .HasMaxLength(3)
            .IsRequired();

        builder.Property(p => p.RiskScore)
            .HasPrecision(9, 4);

        builder.Property(p => p.ValueAtRisk)
            .HasPrecision(18, 2);

        builder.Property(p => p.CreatedAt)
            .HasDefaultValueSql("SYSUTCDATETIME()");

        builder.Property(p => p.LastEvaluatedAt);

        builder.HasMany<AssetPosition>("_positions")
            .WithOne()
            .HasForeignKey(p => p.PortfolioId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.Navigation("_positions").UsePropertyAccessMode(PropertyAccessMode.Field);

        builder.Ignore(p => p.TotalValue);
    }
}
