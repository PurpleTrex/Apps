namespace RiskPortfolio.Api.Models.Requests;

public sealed class AddOrUpdatePositionRequest
{
    public string Symbol { get; init; } = string.Empty;
    public decimal Quantity { get; init; }
    public decimal AveragePrice { get; init; }
    public decimal CurrentPrice { get; init; }
    public decimal Volatility { get; init; }
}
