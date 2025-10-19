using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Core.Models;
using RiskPortfolio.Infrastructure.Data;

namespace RiskPortfolio.Infrastructure.Services;

public class PerformanceService : IPerformanceService
{
    private readonly RiskPortfolioDbContext _context;
    private readonly ILogger<PerformanceService> _logger;

    public PerformanceService(
        RiskPortfolioDbContext context,
        ILogger<PerformanceService> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<IReadOnlyList<PortfolioSnapshot>> GetHistoricalPerformanceAsync(
        Guid portfolioId, 
        int days, 
        CancellationToken cancellationToken = default)
    {
        try
        {
            var startDate = DateTime.UtcNow.Date.AddDays(-days);
            
            var snapshots = await _context.PortfolioSnapshots
                .Where(s => s.PortfolioId == portfolioId && s.SnapshotDate >= startDate)
                .OrderBy(s => s.SnapshotDate)
                .ToListAsync(cancellationToken);

            _logger.LogDebug("Retrieved {Count} snapshots for portfolio {PortfolioId} over {Days} days", 
                snapshots.Count, portfolioId, days);

            return snapshots;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving historical performance for portfolio {PortfolioId}", portfolioId);
            throw;
        }
    }

    public async Task<PerformanceMetrics?> GetPerformanceMetricsAsync(
        Guid portfolioId, 
        int days, 
        CancellationToken cancellationToken = default)
    {
        try
        {
            var snapshots = await GetHistoricalPerformanceAsync(portfolioId, days, cancellationToken);
            
            if (!snapshots.Any())
            {
                _logger.LogWarning("No snapshots found for portfolio {PortfolioId}", portfolioId);
                return null;
            }

            return CalculateMetrics(snapshots);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error calculating performance metrics for portfolio {PortfolioId}", portfolioId);
            throw;
        }
    }

    public async Task<PortfolioSnapshot?> GetLatestSnapshotAsync(
        Guid portfolioId, 
        CancellationToken cancellationToken = default)
    {
        try
        {
            return await _context.PortfolioSnapshots
                .Where(s => s.PortfolioId == portfolioId)
                .OrderByDescending(s => s.SnapshotDate)
                .FirstOrDefaultAsync(cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving latest snapshot for portfolio {PortfolioId}", portfolioId);
            throw;
        }
    }

    public PerformanceMetrics CalculateMetrics(IEnumerable<PortfolioSnapshot> snapshots)
    {
        var snapshotList = snapshots.ToList();
        
        if (!snapshotList.Any())
        {
            return new PerformanceMetrics
            {
                CurrentValue = 0,
                TotalReturn = 0,
                TotalReturnPercentage = 0,
                BestDayReturn = 0,
                WorstDayReturn = 0,
                AverageReturn = 0,
                Volatility = 0,
                DaysTracked = 0
            };
        }

        var currentValue = snapshotList.Last().TotalValue;
        var initialValue = snapshotList.First().TotalValue;
        
        // Calculate total return
        var totalReturn = currentValue - initialValue;
        var totalReturnPercentage = initialValue > 0 
            ? (totalReturn / initialValue) * 100 
            : 0;

        // Calculate best and worst day returns
        var dailyReturns = snapshotList
            .Select(s => s.DailyReturn)
            .ToList();

        var bestDayReturn = dailyReturns.Any() ? dailyReturns.Max() : 0;
        var worstDayReturn = dailyReturns.Any() ? dailyReturns.Min() : 0;
        var averageReturn = dailyReturns.Any() ? dailyReturns.Average() : 0;

        // Calculate volatility (standard deviation of daily returns)
        var volatility = 0m;
        if (dailyReturns.Count > 1)
        {
            var avg = averageReturn;
            var sumSquaredDiff = dailyReturns.Sum(r => (r - avg) * (r - avg));
            var variance = sumSquaredDiff / dailyReturns.Count;
            volatility = (decimal)Math.Sqrt((double)variance);
        }

        return new PerformanceMetrics
        {
            CurrentValue = currentValue,
            TotalReturn = totalReturn,
            TotalReturnPercentage = totalReturnPercentage,
            BestDayReturn = bestDayReturn,
            WorstDayReturn = worstDayReturn,
            AverageReturn = averageReturn,
            Volatility = volatility,
            DaysTracked = snapshotList.Count
        };
    }
}
