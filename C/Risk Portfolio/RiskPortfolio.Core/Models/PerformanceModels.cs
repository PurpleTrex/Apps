namespace RiskPortfolio.Core.Models;

/// <summary>
/// Performance data point representing portfolio value at a specific date
/// </summary>
public class PerformanceDataPoint
{
    public DateTime Date { get; set; }
    public decimal TotalValue { get; set; }
    public decimal RiskScore { get; set; }
    public decimal ValueAtRisk { get; set; }
    public decimal? DailyReturn { get; set; }
}

/// <summary>
/// Aggregated performance metrics for a portfolio over a time period
/// </summary>
public class PerformanceMetrics
{
    public decimal CurrentValue { get; set; }
    public decimal TotalReturn { get; set; }
    public decimal TotalReturnPercentage { get; set; }
    public decimal BestDayReturn { get; set; }
    public decimal WorstDayReturn { get; set; }
    public decimal AverageReturn { get; set; }
    public decimal Volatility { get; set; }
    public int DaysTracked { get; set; }
}
