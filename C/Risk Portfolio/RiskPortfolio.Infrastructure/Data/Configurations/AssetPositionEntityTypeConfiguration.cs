using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Infrastructure.Data.Configurations;

public class AssetPositionEntityTypeConfiguration : IEntityTypeConfiguration<AssetPosition>
{
    public void Configure(EntityTypeBuilder<AssetPosition> builder)
    {
        builder.ToTable("AssetPositions");

        builder.HasKey(p => p.Id);

        builder.Property(p => p.Symbol)
            .HasMaxLength(32)
            .IsRequired();

        builder.Property(p => p.Quantity)
            .HasPrecision(18, 4)
            .IsRequired();

        builder.Property(p => p.AveragePrice)
            .HasPrecision(18, 4)
            .IsRequired();

        builder.Property(p => p.CurrentPrice)
            .HasPrecision(18, 4)
            .IsRequired();

        builder.Property(p => p.Volatility)
            .HasPrecision(5, 4)
            .IsRequired();

        builder.Ignore(p => p.MarketValue);
        builder.Ignore(p => p.RiskContribution);
    }
}
