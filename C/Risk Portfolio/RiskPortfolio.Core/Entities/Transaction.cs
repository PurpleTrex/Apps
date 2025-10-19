using System;

namespace RiskPortfolio.Core.Entities;

/// <summary>
/// Represents a buy or sell transaction for a portfolio position
/// </summary>
public class Transaction
{
    public Guid Id { get; private set; }
    public Guid PortfolioId { get; private set; }
    public string Symbol { get; private set; } = string.Empty;
    public TransactionType Type { get; private set; }
    public decimal Quantity { get; private set; }
    public decimal Price { get; private set; }
    public decimal TotalAmount { get; private set; }
    public decimal Commission { get; private set; }
    public DateTime TransactionDate { get; private set; }
    public string? Notes { get; private set; }
    public DateTime CreatedAt { get; private set; }

    // Navigation property
    public Portfolio Portfolio { get; private set; } = null!;

    private Transaction() { } // EF Core constructor

    public Transaction(
        Guid portfolioId,
        string symbol,
        TransactionType type,
        decimal quantity,
        decimal price,
        DateTime transactionDate,
        decimal commission = 0,
        string? notes = null)
    {
        if (string.IsNullOrWhiteSpace(symbol))
            throw new ArgumentException("Symbol cannot be empty", nameof(symbol));
        if (quantity <= 0)
            throw new ArgumentException("Quantity must be positive", nameof(quantity));
        if (price <= 0)
            throw new ArgumentException("Price must be positive", nameof(price));

        Id = Guid.NewGuid();
        PortfolioId = portfolioId;
        Symbol = symbol.ToUpperInvariant();
        Type = type;
        Quantity = quantity;
        Price = price;
        TotalAmount = (quantity * price) + commission;
        Commission = commission;
        TransactionDate = transactionDate;
        Notes = notes;
        CreatedAt = DateTime.UtcNow;
    }

    public void UpdateNotes(string? notes)
    {
        Notes = notes;
    }
}

public enum TransactionType
{
    Buy = 1,
    Sell = 2
}
