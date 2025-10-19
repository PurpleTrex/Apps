namespace RiskPortfolio.Api.Models.Responses;

public sealed record PerformanceDataPoint(
    DateTime Date,
    decimal TotalValue,
    decimal RiskScore,
    decimal ValueAtRisk,
    decimal DailyReturn
);
