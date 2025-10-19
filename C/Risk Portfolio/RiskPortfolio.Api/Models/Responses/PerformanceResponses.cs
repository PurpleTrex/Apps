using System;

namespace RiskPortfolio.Api.Models.Responses;

public record TransactionResponse(
    Guid Id,
    Guid PortfolioId,
    string Symbol,
    string Type, // "Buy" or "Sell"
    decimal Quantity,
    decimal Price,
    decimal TotalAmount,
    decimal Commission,
    DateTime TransactionDate,
    string? Notes,
    DateTime CreatedAt
);
