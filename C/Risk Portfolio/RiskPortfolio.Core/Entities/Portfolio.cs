using System;
using System.Collections.Generic;
using System.Linq;

namespace RiskPortfolio.Core.Entities;

/// <summary>
/// Aggregate root representing an investment portfolio with its risk posture.
/// </summary>
public class Portfolio
{
    private readonly List<AssetPosition> _positions = new();

    public Guid Id { get; private set; } = Guid.NewGuid();
    public string Name { get; private set; }
    public string Owner { get; private set; }
    public string BaseCurrency { get; private set; }
    public decimal RiskScore { get; private set; }
    public decimal ValueAtRisk { get; private set; }
    public DateTimeOffset CreatedAt { get; private set; } = DateTimeOffset.UtcNow;
    public DateTimeOffset? LastEvaluatedAt { get; private set; }
    public IReadOnlyCollection<AssetPosition> Positions => _positions.AsReadOnly();

    public Portfolio(string name, string owner, string baseCurrency)
    {
        if (string.IsNullOrWhiteSpace(name))
        {
            throw new ArgumentException("Portfolio name must be provided.", nameof(name));
        }

        if (string.IsNullOrWhiteSpace(owner))
        {
            throw new ArgumentException("Owner must be provided.", nameof(owner));
        }

        if (string.IsNullOrWhiteSpace(baseCurrency))
        {
            throw new ArgumentException("Base currency must be provided.", nameof(baseCurrency));
        }

        Name = name.Trim();
        Owner = owner.Trim();
        BaseCurrency = baseCurrency.ToUpperInvariant();
    }

    private Portfolio()
    {
        Name = string.Empty;
        Owner = string.Empty;
        BaseCurrency = "USD";
    }

    public decimal TotalValue => Math.Round(_positions.Sum(p => p.MarketValue), 2, MidpointRounding.AwayFromZero);

    public AssetPosition AddOrUpdatePosition(string symbol, decimal quantity, decimal averagePrice, decimal currentPrice, decimal volatility)
    {
        var existing = _positions.SingleOrDefault(p => p.Symbol.Equals(symbol, StringComparison.OrdinalIgnoreCase));
        if (existing is null)
        {
            var position = new AssetPosition(Id, symbol, quantity, averagePrice, currentPrice, volatility);
            _positions.Add(position);
            return position;
        }

        existing.UpdateQuantity(quantity, averagePrice);
        existing.UpdateMarketData(currentPrice, volatility);
        return existing;
    }

    public void RemovePosition(Guid positionId)
    {
        var index = _positions.FindIndex(p => p.Id == positionId);
        if (index < 0)
        {
            throw new InvalidOperationException($"Position {positionId} does not exist in portfolio {Id}.");
        }

        _positions.RemoveAt(index);
    }

    public void UpdateRiskMetrics(decimal riskScore, decimal valueAtRisk, DateTimeOffset evaluatedAt)
    {
        if (riskScore < 0)
        {
            throw new ArgumentOutOfRangeException(nameof(riskScore), riskScore, "Risk score cannot be negative.");
        }

        if (valueAtRisk < 0)
        {
            throw new ArgumentOutOfRangeException(nameof(valueAtRisk), valueAtRisk, "Value at risk cannot be negative.");
        }

        RiskScore = Math.Round(riskScore, 2, MidpointRounding.AwayFromZero);
        ValueAtRisk = Math.Round(valueAtRisk, 2, MidpointRounding.AwayFromZero);
        LastEvaluatedAt = evaluatedAt;
    }

    public void Rename(string name)
    {
        if (string.IsNullOrWhiteSpace(name))
        {
            throw new ArgumentException("Portfolio name must be provided.", nameof(name));
        }

        Name = name.Trim();
    }

    public void ChangeOwner(string owner)
    {
        if (string.IsNullOrWhiteSpace(owner))
        {
            throw new ArgumentException("Owner must be provided.", nameof(owner));
        }

        Owner = owner.Trim();
    }
}
