using RiskPortfolio.Core.Models.MarketData;

namespace RiskPortfolio.Core.Interfaces;

/// <summary>
/// Comprehensive interface for retrieving real-time and historical market data from Alpha Vantage API
/// </summary>
public interface IMarketDataService
{
    // ============ CORE STOCK TIME SERIES ============
    
    /// <summary>
    /// Get current price for a stock symbol (using GLOBAL_QUOTE)
    /// </summary>
    Task<decimal?> GetCurrentPriceAsync(string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get detailed quote information including price and volatility (GLOBAL_QUOTE)
    /// </summary>
    Task<MarketQuote?> GetQuoteAsync(string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get quotes for multiple symbols in a single call (up to 100 on premium)
    /// </summary>
    Task<Dictionary<string, MarketQuote>> GetBatchQuotesAsync(IEnumerable<string> symbols, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get intraday time series data (TIME_SERIES_INTRADAY)
    /// </summary>
    Task<List<TimeSeriesDataPoint>> GetIntradayDataAsync(string symbol, string interval = "60min", bool outputFull = false, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get daily time series data (TIME_SERIES_DAILY)
    /// </summary>
    Task<List<TimeSeriesDataPoint>> GetDailyDataAsync(string symbol, bool outputFull = false, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get daily adjusted time series data with splits/dividends (TIME_SERIES_DAILY_ADJUSTED)
    /// </summary>
    Task<List<TimeSeriesDataPoint>> GetDailyAdjustedDataAsync(string symbol, bool outputFull = false, CancellationToken cancellationToken = default);

    /// <summary>
    /// Search for stock symbols by keyword (SYMBOL_SEARCH)
    /// </summary>
    Task<List<SymbolSearchResult>> SearchSymbolsAsync(string keyword, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get global market open/close status (MARKET_STATUS)
    /// </summary>
    Task<MarketStatus?> GetMarketStatusAsync(CancellationToken cancellationToken = default);

    // ============ FUNDAMENTAL DATA ============
    
    /// <summary>
    /// Get company overview and fundamental metrics (OVERVIEW)
    /// </summary>
    Task<FundamentalData?> GetCompanyOverviewAsync(string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get earnings history and estimates (EARNINGS)
    /// </summary>
    Task<List<EarningsData>> GetEarningsAsync(string symbol, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get insider transactions (INSIDER_TRANSACTIONS)
    /// </summary>
    Task<List<InsiderTransaction>> GetInsiderTransactionsAsync(string symbol, CancellationToken cancellationToken = default);

    // ============ ALPHA INTELLIGENCE ============
    
    /// <summary>
    /// Get market news with sentiment analysis (NEWS_SENTIMENT)
    /// </summary>
    Task<List<NewsSentiment>> GetNewsSentimentAsync(string? tickers = null, string? topics = null, int limit = 50, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get top gainers, losers, and most active stocks (TOP_GAINERS_LOSERS)
    /// </summary>
    Task<TopMoversData?> GetTopMoversAsync(CancellationToken cancellationToken = default);

    // ============ TECHNICAL INDICATORS ============
    
    /// <summary>
    /// Get Simple Moving Average (SMA)
    /// </summary>
    Task<List<TechnicalIndicatorValue>> GetSMAAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Exponential Moving Average (EMA)
    /// </summary>
    Task<List<TechnicalIndicatorValue>> GetEMAAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Relative Strength Index (RSI)
    /// </summary>
    Task<List<TechnicalIndicatorValue>> GetRSIAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Moving Average Convergence/Divergence (MACD)
    /// </summary>
    Task<List<TechnicalIndicatorValue>> GetMACDAsync(string symbol, string interval, string seriesType = "close", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Bollinger Bands (BBANDS)
    /// </summary>
    Task<List<TechnicalIndicatorValue>> GetBollingerBandsAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default);

    // ============ FOREX ============
    
    /// <summary>
    /// Get real-time forex exchange rate (CURRENCY_EXCHANGE_RATE)
    /// </summary>
    Task<ForexRate?> GetForexRateAsync(string fromCurrency, string toCurrency, CancellationToken cancellationToken = default);

    // ============ CRYPTO ============
    
    /// <summary>
    /// Get cryptocurrency exchange rate (CURRENCY_EXCHANGE_RATE)
    /// </summary>
    Task<CryptoData?> GetCryptoRateAsync(string symbol, string market = "USD", CancellationToken cancellationToken = default);

    // ============ ECONOMIC INDICATORS ============
    
    /// <summary>
    /// Get real GDP data (REAL_GDP)
    /// </summary>
    Task<EconomicIndicator?> GetRealGDPAsync(string interval = "annual", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get unemployment rate (UNEMPLOYMENT)
    /// </summary>
    Task<EconomicIndicator?> GetUnemploymentAsync(CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Consumer Price Index / inflation data (CPI)
    /// </summary>
    Task<EconomicIndicator?> GetCPIAsync(string interval = "monthly", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Federal Funds Rate (FEDERAL_FUNDS_RATE)
    /// </summary>
    Task<EconomicIndicator?> GetFederalFundsRateAsync(string interval = "monthly", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Treasury Yield (TREASURY_YIELD)
    /// </summary>
    Task<EconomicIndicator?> GetTreasuryYieldAsync(string interval = "monthly", string maturity = "10year", CancellationToken cancellationToken = default);

    // ============ COMMODITIES ============
    
    /// <summary>
    /// Get WTI Crude Oil prices (WTI)
    /// </summary>
    Task<CommodityData?> GetWTIOilAsync(string interval = "monthly", CancellationToken cancellationToken = default);

    /// <summary>
    /// Get Natural Gas prices (NATURAL_GAS)
    /// </summary>
    Task<CommodityData?> GetNaturalGasAsync(string interval = "monthly", CancellationToken cancellationToken = default);
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
