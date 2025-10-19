using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Infrastructure.Data.Configurations;

public class TransactionConfiguration : IEntityTypeConfiguration<Transaction>
{
    public void Configure(EntityTypeBuilder<Transaction> builder)
    {
        builder.ToTable("Transactions");

        builder.HasKey(t => t.Id);

        builder.Property(t => t.PortfolioId).IsRequired();
        builder.Property(t => t.Symbol).IsRequired().HasMaxLength(10);
        builder.Property(t => t.Type).IsRequired();
        builder.Property(t => t.Quantity).HasColumnType("decimal(18,8)").IsRequired();
        builder.Property(t => t.Price).HasColumnType("decimal(18,2)").IsRequired();
        builder.Property(t => t.TotalAmount).HasColumnType("decimal(18,2)").IsRequired();
        builder.Property(t => t.Commission).HasColumnType("decimal(18,2)").IsRequired();
        builder.Property(t => t.TransactionDate).IsRequired();
        builder.Property(t => t.Notes).HasMaxLength(500);
        builder.Property(t => t.CreatedAt).IsRequired();

        builder.HasOne(t => t.Portfolio)
            .WithMany()
            .HasForeignKey(t => t.PortfolioId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasIndex(t => t.PortfolioId)
            .HasDatabaseName("IX_Transactions_Portfolio");

        builder.HasIndex(t => t.Symbol)
            .HasDatabaseName("IX_Transactions_Symbol");

        builder.HasIndex(t => t.TransactionDate)
            .HasDatabaseName("IX_Transactions_Date");
    }
}
