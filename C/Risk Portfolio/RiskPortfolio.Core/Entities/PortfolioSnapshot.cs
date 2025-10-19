using System;

namespace RiskPortfolio.Core.Entities;

/// <summary>
/// Represents a daily snapshot of portfolio performance metrics
/// </summary>
public class PortfolioSnapshot
{
    public Guid Id { get; private set; }
    public Guid PortfolioId { get; private set; }
    public DateTime SnapshotDate { get; private set; }
    public decimal TotalValue { get; private set; }
    public decimal RiskScore { get; private set; }
    public decimal ValueAtRisk { get; private set; }
    public int PositionCount { get; private set; }
    public decimal DailyReturn { get; private set; }
    public DateTime CreatedAt { get; private set; }

    // Navigation property
    public Portfolio Portfolio { get; private set; } = null!;

    private PortfolioSnapshot() { } // EF Core constructor

    public PortfolioSnapshot(
        Guid portfolioId,
        DateTime snapshotDate,
        decimal totalValue,
        decimal riskScore,
        decimal valueAtRisk,
        int positionCount,
        decimal dailyReturn = 0)
    {
        Id = Guid.NewGuid();
        PortfolioId = portfolioId;
        SnapshotDate = snapshotDate.Date; // Store only date part
        TotalValue = totalValue;
        RiskScore = riskScore;
        ValueAtRisk = valueAtRisk;
        PositionCount = positionCount;
        DailyReturn = dailyReturn;
        CreatedAt = DateTime.UtcNow;
    }
}
