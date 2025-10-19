using System;

namespace RiskPortfolio.Api.Models.Requests;

public record CreateTransactionRequest(
    string Symbol,
    string Type, // "Buy" or "Sell"
    decimal Quantity,
    decimal Price,
    DateTime TransactionDate,
    decimal Commission = 0,
    string? Notes = null
);

public record UpdateTransactionNotesRequest(
    string Notes
);
