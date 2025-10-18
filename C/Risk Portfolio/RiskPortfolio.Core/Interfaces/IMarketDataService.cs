namespace RiskPortfolio.Core.Interfaces;

/// <summary>
/// Interface for retrieving real-time market data from external sources
/// </summary>
public interface IMarketDataService
{
    /// <summary>
    /// Get current price for a stock symbol
    /// </summary>
    Task<decimal?> GetCurrentPriceAsync(string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get detailed quote information including price and volatility
    /// </summary>
    Task<MarketQuote?> GetQuoteAsync(string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get quotes for multiple symbols in a single call
    /// </summary>
    Task<Dictionary<string, MarketQuote>> GetBatchQuotesAsync(IEnumerable<string> symbols, CancellationToken cancellationToken = default);

    /// <summary>
    /// Search for stock symbols by keyword
    /// </summary>
    Task<List<SymbolSearchResult>> SearchSymbolsAsync(string keyword, CancellationToken cancellationToken = default);
}

/// <summary>
/// Symbol search result
/// </summary>
public class SymbolSearchResult
{
    public string Symbol { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public string Region { get; set; } = string.Empty;
    public string Currency { get; set; } = string.Empty;
    public string? MarketOpen { get; set; }
    public string? MarketClose { get; set; }
    public string? Timezone { get; set; }
}

/// <summary>
/// Market quote data
/// </summary>
public class MarketQuote
{
    public string Symbol { get; set; } = string.Empty;
    public decimal Price { get; set; }
    public decimal? Change { get; set; }
    public decimal? ChangePercent { get; set; }
    public decimal? Volume { get; set; }
    public decimal? High { get; set; }
    public decimal? Low { get; set; }
    public decimal? Open { get; set; }
    public decimal? PreviousClose { get; set; }
    public DateTime LastUpdated { get; set; }
    
    /// <summary>
    /// Historical volatility (standard deviation of returns)
    /// </summary>
    public decimal? Volatility { get; set; }
}
