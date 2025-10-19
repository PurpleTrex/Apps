using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Data;

namespace RiskPortfolio.Infrastructure.Jobs;

/// <summary>
/// Background service that captures daily portfolio snapshots for historical tracking
/// </summary>
public class PortfolioSnapshotJob : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;
    private readonly ILogger<PortfolioSnapshotJob> _logger;
    private readonly TimeSpan _runInterval = TimeSpan.FromHours(24); // Run once per day
    private readonly TimeOnly _targetTime = new TimeOnly(23, 55); // Run at 11:55 PM

    public PortfolioSnapshotJob(
        IServiceProvider serviceProvider,
        ILogger<PortfolioSnapshotJob> logger)
    {
        _serviceProvider = serviceProvider;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("Portfolio Snapshot Job started");

        // Wait until target time on first run
        await WaitUntilTargetTime(stoppingToken);

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                await CaptureSnapshotsAsync(stoppingToken);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error capturing portfolio snapshots");
            }

            // Wait 24 hours until next run
            await Task.Delay(_runInterval, stoppingToken);
        }
    }

    private async Task WaitUntilTargetTime(CancellationToken cancellationToken)
    {
        var now = DateTime.Now;
        var today = DateOnly.FromDateTime(now);
        var targetDateTime = today.ToDateTime(_targetTime);

        if (now > targetDateTime)
        {
            // If past target time today, schedule for tomorrow
            targetDateTime = targetDateTime.AddDays(1);
        }

        var delay = targetDateTime - now;
        _logger.LogInformation("Portfolio Snapshot Job will run in {Delay}", delay);

        if (delay > TimeSpan.Zero)
        {
            await Task.Delay(delay, cancellationToken);
        }
    }

    private async Task CaptureSnapshotsAsync(CancellationToken cancellationToken)
    {
        using var scope = _serviceProvider.CreateScope();
        var dbContext = scope.ServiceProvider.GetRequiredService<RiskPortfolioDbContext>();
        var portfolioRepository = scope.ServiceProvider.GetRequiredService<IPortfolioRepository>();
        var riskService = scope.ServiceProvider.GetRequiredService<IRiskAssessmentService>();
        var riskCache = scope.ServiceProvider.GetRequiredService<IRiskMetricCache>();

        var portfolios = await portfolioRepository.GetAllAsync(cancellationToken);
        var snapshotDate = DateTime.UtcNow.Date;

        _logger.LogInformation("Capturing snapshots for {Count} portfolios on {Date}", portfolios.Count, snapshotDate);

        foreach (var portfolio in portfolios)
        {
            try
            {
                // Check if snapshot already exists for today
                var existingSnapshot = await dbContext.PortfolioSnapshots
                    .FirstOrDefaultAsync(ps => ps.PortfolioId == portfolio.Id && ps.SnapshotDate == snapshotDate, cancellationToken);

                if (existingSnapshot != null)
                {
                    _logger.LogInformation("Snapshot already exists for portfolio {PortfolioId} on {Date}", portfolio.Id, snapshotDate);
                    continue;
                }

                // Calculate current metrics
                var risk = await riskService.CalculateAsync(portfolio, cancellationToken);
                var totalValue = portfolio.Positions.Sum(p => p.MarketValue);

                // Calculate daily return (compare to yesterday's snapshot)
                var yesterday = snapshotDate.AddDays(-1);
                var previousSnapshot = await dbContext.PortfolioSnapshots
                    .Where(ps => ps.PortfolioId == portfolio.Id && ps.SnapshotDate == yesterday)
                    .FirstOrDefaultAsync(cancellationToken);

                decimal dailyReturn = 0;
                if (previousSnapshot != null && previousSnapshot.TotalValue > 0)
                {
                    dailyReturn = ((totalValue - previousSnapshot.TotalValue) / previousSnapshot.TotalValue) * 100;
                }

                // Create snapshot
                var snapshot = new PortfolioSnapshot(
                    portfolio.Id,
                    snapshotDate,
                    totalValue,
                    risk.RiskScore,
                    risk.ValueAtRisk,
                    portfolio.Positions.Count,
                    dailyReturn);

                await dbContext.PortfolioSnapshots.AddAsync(snapshot, cancellationToken);
                await dbContext.SaveChangesAsync(cancellationToken);

                _logger.LogInformation("Captured snapshot for portfolio {PortfolioId}: Value={Value}, Risk={Risk}, DailyReturn={Return}%",
                    portfolio.Id, totalValue, risk.RiskScore, dailyReturn);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error capturing snapshot for portfolio {PortfolioId}", portfolio.Id);
            }
        }

        _logger.LogInformation("Portfolio snapshot capture completed");
    }
}
