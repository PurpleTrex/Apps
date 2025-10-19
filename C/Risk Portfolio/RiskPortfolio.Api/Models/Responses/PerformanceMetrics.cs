namespace RiskPortfolio.Api.Models.Responses;

public sealed record PerformanceMetrics(
    decimal TotalReturn,
    decimal AnnualizedReturn,
    decimal Volatility,
    decimal SharpeRatio,
    decimal MaxDrawdown,
    decimal CurrentValue,
    decimal StartValue,
    int PeriodDays
);
