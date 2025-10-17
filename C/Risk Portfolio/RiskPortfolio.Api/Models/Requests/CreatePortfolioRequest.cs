using System.Collections.Generic;

namespace RiskPortfolio.Api.Models.Requests;

public sealed class CreatePortfolioRequest
{
    public string Name { get; init; } = string.Empty;
    public string Owner { get; init; } = string.Empty;
    public string BaseCurrency { get; init; } = "USD";
    public ICollection<CreatePortfolioPositionRequest> Positions { get; init; } = new List<CreatePortfolioPositionRequest>();
}

public sealed class CreatePortfolioPositionRequest
{
    public string Symbol { get; init; } = string.Empty;
    public decimal Quantity { get; init; }
    public decimal AveragePrice { get; init; }
    public decimal CurrentPrice { get; init; }
    public decimal Volatility { get; init; }
}
