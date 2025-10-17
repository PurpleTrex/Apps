using System;

namespace RiskPortfolio.Core.Entities;

/// <summary>
/// Represents a single tradable instrument inside a portfolio.
/// </summary>
public class AssetPosition
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid PortfolioId { get; private set; }
    public string Symbol { get; private set; }
    public decimal Quantity { get; private set; }
    public decimal AveragePrice { get; private set; }
    public decimal CurrentPrice { get; private set; }
    public decimal Volatility { get; private set; }

    public decimal MarketValue => Math.Round(CurrentPrice * Quantity, 2, MidpointRounding.AwayFromZero);
    public decimal RiskContribution => Math.Round(MarketValue * Volatility, 2, MidpointRounding.AwayFromZero);

    private AssetPosition()
    {
        Symbol = string.Empty;
    }

    public AssetPosition(Guid portfolioId, string symbol, decimal quantity, decimal averagePrice, decimal currentPrice, decimal volatility)
    {
        if (string.IsNullOrWhiteSpace(symbol))
        {
            throw new ArgumentException("Symbol must be provided.", nameof(symbol));
        }

        if (quantity <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(quantity), quantity, "Quantity must be positive.");
        }

        if (averagePrice <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(averagePrice), averagePrice, "Average price must be positive.");
        }

        if (currentPrice <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(currentPrice), currentPrice, "Current price must be positive.");
        }

        if (volatility < 0 || volatility > 1)
        {
            throw new ArgumentOutOfRangeException(nameof(volatility), volatility, "Volatility must be between 0 and 1.");
        }

        PortfolioId = portfolioId;
        Symbol = symbol.ToUpperInvariant();
        Quantity = Math.Round(quantity, 4, MidpointRounding.AwayFromZero);
        AveragePrice = Math.Round(averagePrice, 4, MidpointRounding.AwayFromZero);
        CurrentPrice = Math.Round(currentPrice, 4, MidpointRounding.AwayFromZero);
        Volatility = Math.Round(volatility, 4, MidpointRounding.AwayFromZero);
    }

    public void UpdateMarketData(decimal currentPrice, decimal volatility)
    {
        if (currentPrice <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(currentPrice), currentPrice, "Current price must be positive.");
        }

        if (volatility < 0 || volatility > 1)
        {
            throw new ArgumentOutOfRangeException(nameof(volatility), volatility, "Volatility must be between 0 and 1.");
        }

        CurrentPrice = Math.Round(currentPrice, 4, MidpointRounding.AwayFromZero);
        Volatility = Math.Round(volatility, 4, MidpointRounding.AwayFromZero);
    }

    public void UpdateQuantity(decimal quantity, decimal averagePrice)
    {
        if (quantity <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(quantity), quantity, "Quantity must be positive.");
        }

        if (averagePrice <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(averagePrice), averagePrice, "Average price must be positive.");
        }

        Quantity = Math.Round(quantity, 4, MidpointRounding.AwayFromZero);
        AveragePrice = Math.Round(averagePrice, 4, MidpointRounding.AwayFromZero);
    }

    public void ReassignToPortfolio(Guid portfolioId)
    {
        PortfolioId = portfolioId;
    }
}
