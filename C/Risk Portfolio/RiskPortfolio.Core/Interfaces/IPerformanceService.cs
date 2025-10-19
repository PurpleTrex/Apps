using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Core.Interfaces;

/// <summary>
/// Service interface for portfolio performance analysis and historical data
/// </summary>
public interface IPerformanceService
{
    /// <summary>
    /// Get historical performance data for a portfolio over a specified number of days
    /// </summary>
    Task<IReadOnlyList<PortfolioSnapshot>> GetHistoricalPerformanceAsync(
        Guid portfolioId, 
        int days, 
        CancellationToken cancellationToken = default);

    /// <summary>
    /// Get aggregated performance metrics for a portfolio
    /// </summary>
    Task<PerformanceMetrics?> GetPerformanceMetricsAsync(
        Guid portfolioId, 
        int days, 
        CancellationToken cancellationToken = default);

    /// <summary>
    /// Get the latest snapshot for a portfolio
    /// </summary>
    Task<PortfolioSnapshot?> GetLatestSnapshotAsync(
        Guid portfolioId, 
        CancellationToken cancellationToken = default);

    /// <summary>
    /// Calculate performance metrics from a list of snapshots
    /// </summary>
    PerformanceMetrics CalculateMetrics(IEnumerable<PortfolioSnapshot> snapshots);
}
