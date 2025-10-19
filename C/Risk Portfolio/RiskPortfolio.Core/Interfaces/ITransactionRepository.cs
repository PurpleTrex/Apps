using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Core.Interfaces;

/// <summary>
/// Repository interface for managing portfolio transactions
/// </summary>
public interface ITransactionRepository
{
    /// <summary>
    /// Get a transaction by ID
    /// </summary>
    Task<Transaction?> GetAsync(Guid id, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get all transactions for a specific portfolio
    /// </summary>
    Task<IReadOnlyList<Transaction>> GetByPortfolioAsync(Guid portfolioId, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get transactions for a specific symbol within a portfolio
    /// </summary>
    Task<IReadOnlyList<Transaction>> GetBySymbolAsync(Guid portfolioId, string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get transactions within a date range
    /// </summary>
    Task<IReadOnlyList<Transaction>> GetByDateRangeAsync(Guid portfolioId, DateTime startDate, DateTime endDate, CancellationToken cancellationToken = default);

    /// <summary>
    /// Add a new transaction
    /// </summary>
    Task AddAsync(Transaction transaction, CancellationToken cancellationToken = default);

    /// <summary>
    /// Update an existing transaction
    /// </summary>
    Task UpdateAsync(Transaction transaction, CancellationToken cancellationToken = default);

    /// <summary>
    /// Delete a transaction
    /// </summary>
    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);

    /// <summary>
    /// Check if a transaction exists
    /// </summary>
    Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default);
}
