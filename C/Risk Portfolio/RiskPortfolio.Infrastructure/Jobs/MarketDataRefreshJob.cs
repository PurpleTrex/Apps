using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Options;

namespace RiskPortfolio.Infrastructure.Jobs;

/// <summary>
/// Background service that automatically refreshes portfolio positions with live market data
/// </summary>
public class MarketDataRefreshJob : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;
    private readonly ILogger<MarketDataRefreshJob> _logger;
    private readonly MarketDataRefreshOptions _options;

    public MarketDataRefreshJob(
        IServiceProvider serviceProvider,
        ILogger<MarketDataRefreshJob> logger,
        IOptions<MarketDataRefreshOptions> options)
    {
        _serviceProvider = serviceProvider;
        _logger = logger;
        _options = options.Value;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        if (!_options.Enabled)
        {
            _logger.LogInformation("Market Data Refresh Job is disabled");
            return;
        }

        var refreshInterval = TimeSpan.FromMinutes(_options.RefreshIntervalMinutes);
        var startupDelay = TimeSpan.FromMinutes(_options.StartupDelayMinutes);

        _logger.LogInformation("Market Data Refresh Job started. Refresh interval: {Interval}, Startup delay: {Delay}", 
            refreshInterval, startupDelay);

        // Wait after startup before first run
        await Task.Delay(startupDelay, stoppingToken);

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                await RefreshAllPortfolioPositionsAsync(stoppingToken);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while refreshing market data");
            }

            // Wait for the next refresh interval
            await Task.Delay(refreshInterval, stoppingToken);
        }
    }

    private async Task RefreshAllPortfolioPositionsAsync(CancellationToken cancellationToken)
    {
        _logger.LogInformation("Starting market data refresh for all portfolios");

        using var scope = _serviceProvider.CreateScope();
        var portfolioRepository = scope.ServiceProvider.GetRequiredService<IPortfolioRepository>();
        var marketDataService = scope.ServiceProvider.GetRequiredService<IMarketDataService>();
        var riskAssessmentService = scope.ServiceProvider.GetRequiredService<IRiskAssessmentService>();

        try
        {
            // Get all portfolios
            var portfolios = await portfolioRepository.GetAllAsync(cancellationToken);
            
            if (!portfolios.Any())
            {
                _logger.LogInformation("No portfolios found to refresh");
                return;
            }

            _logger.LogInformation("Found {Count} portfolios to refresh", portfolios.Count);

            foreach (var portfolio in portfolios)
            {
                try
                {
                    _logger.LogInformation("Refreshing portfolio: {Name} (ID: {Id})", portfolio.Name, portfolio.Id);

                    // Get all unique symbols
                    var symbols = portfolio.Positions.Select(p => p.Symbol).Distinct().ToList();
                    
                    if (!symbols.Any())
                    {
                        _logger.LogWarning("Portfolio {Name} has no positions to refresh", portfolio.Name);
                        continue;
                    }

                    // Fetch live prices for all symbols
                    var updatedCount = 0;
                    foreach (var symbol in symbols)
                    {
                        try
                        {
                            var quote = await marketDataService.GetQuoteAsync(symbol, cancellationToken);
                            
                            if (quote != null && quote.Price > 0)
                            {
                                // Find all positions with this symbol
                                var positions = portfolio.Positions.Where(p => p.Symbol == symbol);
                                
                                foreach (var position in positions)
                                {
                                    var oldPrice = position.CurrentPrice;
                                    position.UpdatePrice(quote.Price);
                                    
                                    // Update volatility if available
                                    if (quote.Volatility.HasValue && quote.Volatility.Value > 0)
                                    {
                                        position.UpdateVolatility(quote.Volatility.Value);
                                    }
                                    
                                    updatedCount++;
                                    _logger.LogDebug("Updated {Symbol}: ${OldPrice} -> ${NewPrice}", 
                                        symbol, oldPrice, quote.Price);
                                }
                            }
                            else
                            {
                                _logger.LogWarning("No price data available for {Symbol}", symbol);
                            }

                            // Delay to respect rate limits
                            var rateLimitDelay = TimeSpan.FromSeconds(_options.RateLimitDelaySeconds);
                            await Task.Delay(rateLimitDelay, cancellationToken);
                        }
                        catch (Exception ex)
                        {
                            _logger.LogError(ex, "Error fetching price for {Symbol}", symbol);
                        }
                    }

                    // Recalculate risk metrics with updated prices
                    if (updatedCount > 0)
                    {
                        try
                        {
                            await riskAssessmentService.RecalculateRiskAsync(portfolio, cancellationToken);
                            await portfolioRepository.UpdateAsync(portfolio, cancellationToken);
                            
                            _logger.LogInformation("Portfolio {Name} updated: {Count} positions refreshed, new total value: ${Value:N2}", 
                                portfolio.Name, updatedCount, portfolio.TotalValue);
                        }
                        catch (Exception ex)
                        {
                            _logger.LogError(ex, "Error recalculating risk for portfolio {Name}", portfolio.Name);
                        }
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error refreshing portfolio {Name} (ID: {Id})", portfolio.Name, portfolio.Id);
                }
            }

            _logger.LogInformation("Market data refresh completed successfully");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fatal error during market data refresh");
            throw;
        }
    }
}
