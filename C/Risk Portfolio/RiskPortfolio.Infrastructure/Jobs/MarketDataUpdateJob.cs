using Microsoft.Extensions.Logging;
using RiskPortfolio.Core.Interfaces;

namespace RiskPortfolio.Infrastructure.Jobs;

/// <summary>
/// Background job that updates market data (prices and volatility) for all portfolio positions
/// </summary>
public class MarketDataUpdateJob
{
    private readonly IPortfolioRepository _portfolioRepository;
    private readonly IMarketDataService _marketDataService;
    private readonly ILogger<MarketDataUpdateJob> _logger;

    public MarketDataUpdateJob(
        IPortfolioRepository portfolioRepository,
        IMarketDataService marketDataService,
        ILogger<MarketDataUpdateJob> logger)
    {
        _portfolioRepository = portfolioRepository;
        _marketDataService = marketDataService;
        _logger = logger;
    }

    public async Task ExecuteAsync(CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Starting market data update job...");

        try
        {
            var portfolios = await _portfolioRepository.GetAllAsync(cancellationToken);
            var totalPositions = 0;
            var updatedPositions = 0;
            var failedSymbols = new List<string>();

            foreach (var portfolio in portfolios)
            {
                _logger.LogDebug("Updating market data for portfolio {PortfolioId} ({PortfolioName})",
                    portfolio.Id, portfolio.Name);

                foreach (var position in portfolio.Positions)
                {
                    totalPositions++;

                    try
                    {
                        // Fetch latest market data for the symbol
                        var marketData = await _marketDataService.GetQuoteAsync(
                            position.Symbol,
                            cancellationToken);

                        if (marketData != null && marketData.Price > 0)
                        {
                            // Update position with latest price and volatility
                            var volatility = marketData.Volatility ?? position.Volatility;
                            position.UpdateMarketData(
                                marketData.Price,
                                volatility);

                            updatedPositions++;

                            _logger.LogTrace("Updated {Symbol}: Price={Price}, Volatility={Volatility}",
                                position.Symbol, marketData.Price, volatility);
                        }
                        else
                        {
                            _logger.LogWarning("No market data available for symbol {Symbol}", position.Symbol);
                            failedSymbols.Add(position.Symbol);
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Failed to update market data for symbol {Symbol}", position.Symbol);
                        failedSymbols.Add(position.Symbol);
                    }

                    // Small delay to avoid rate limiting
                    await Task.Delay(100, cancellationToken);
                }

                // Save updated portfolio
                await _portfolioRepository.UpdateAsync(portfolio, cancellationToken);
            }

            _logger.LogInformation(
                "Market data update job completed. Total positions: {Total}, Updated: {Updated}, Failed: {Failed}",
                totalPositions, updatedPositions, failedSymbols.Count);

            if (failedSymbols.Any())
            {
                _logger.LogWarning("Failed symbols: {Symbols}", string.Join(", ", failedSymbols.Distinct()));
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Market data update job failed with exception");
            throw;
        }
    }
}
