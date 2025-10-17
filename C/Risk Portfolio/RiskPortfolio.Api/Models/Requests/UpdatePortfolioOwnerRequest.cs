namespace RiskPortfolio.Api.Models.Requests;

public sealed class UpdatePortfolioOwnerRequest
{
    public string Owner { get; init; } = string.Empty;
}
