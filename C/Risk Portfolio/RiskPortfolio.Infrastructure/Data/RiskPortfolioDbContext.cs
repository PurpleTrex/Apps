using Microsoft.EntityFrameworkCore;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Infrastructure.Data;

public class RiskPortfolioDbContext : DbContext
{
    public RiskPortfolioDbContext(DbContextOptions<RiskPortfolioDbContext> options) : base(options)
    {
    }

    public DbSet<Portfolio> Portfolios => Set<Portfolio>();
    public DbSet<AssetPosition> Positions => Set<AssetPosition>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(RiskPortfolioDbContext).Assembly);
    }
}
