namespace RiskPortfolio.Core.Models.MarketData;

/// <summary>
/// Time series data point (OHLCV)
/// </summary>
public class TimeSeriesDataPoint
{
    public DateTime Timestamp { get; set; }
    public decimal Open { get; set; }
    public decimal High { get; set; }
    public decimal Low { get; set; }
    public decimal Close { get; set; }
    public long Volume { get; set; }
    public decimal? AdjustedClose { get; set; }
    public decimal? Dividend { get; set; }
    public decimal? SplitCoefficient { get; set; }
}

/// <summary>
/// Company fundamental data
/// </summary>
public class FundamentalData
{
    public string Symbol { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Sector { get; set; } = string.Empty;
    public string Industry { get; set; } = string.Empty;
    public string Exchange { get; set; } = string.Empty;
    public string Currency { get; set; } = string.Empty;
    public string Country { get; set; } = string.Empty;
    
    // Valuation metrics
    public decimal? MarketCapitalization { get; set; }
    public decimal? PERatio { get; set; }
    public decimal? PEGRatio { get; set; }
    public decimal? BookValue { get; set; }
    public decimal? DividendYield { get; set; }
    public decimal? EPS { get; set; }
    public decimal? RevenuePerShare { get; set; }
    public decimal? ProfitMargin { get; set; }
    public decimal? OperatingMargin { get; set; }
    public decimal? ReturnOnAssets { get; set; }
    public decimal? ReturnOnEquity { get; set; }
    public decimal? Beta { get; set; }
    
    // Dates
    public string? FiscalYearEnd { get; set; }
    public DateTime? LatestQuarter { get; set; }
}

/// <summary>
/// News sentiment data
/// </summary>
public class NewsSentiment
{
    public string Title { get; set; } = string.Empty;
    public string Url { get; set; } = string.Empty;
    public DateTime TimePublished { get; set; }
    public List<string> Authors { get; set; } = new();
    public string Summary { get; set; } = string.Empty;
    public string BannerImage { get; set; } = string.Empty;
    public string Source { get; set; } = string.Empty;
    public List<string> Topics { get; set; } = new();
    
    // Sentiment scores
    public decimal? OverallSentimentScore { get; set; }
    public string? OverallSentimentLabel { get; set; } // Bearish, Somewhat-Bearish, Neutral, Somewhat-Bullish, Bullish
    
    // Ticker-specific sentiment
    public List<TickerSentiment> TickerSentiments { get; set; } = new();
}

public class TickerSentiment
{
    public string Ticker { get; set; } = string.Empty;
    public decimal SentimentScore { get; set; }
    public string SentimentLabel { get; set; } = string.Empty;
}

/// <summary>
/// Earnings data
/// </summary>
public class EarningsData
{
    public string Symbol { get; set; } = string.Empty;
    public DateTime FiscalDateEnding { get; set; }
    public decimal? ReportedEPS { get; set; }
    public decimal? EstimatedEPS { get; set; }
    public decimal? Surprise { get; set; }
    public decimal? SurprisePercentage { get; set; }
}

/// <summary>
/// Insider transaction data
/// </summary>
public class InsiderTransaction
{
    public string Symbol { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Title { get; set; } = string.Empty;
    public DateTime TransactionDate { get; set; }
    public string TransactionType { get; set; } = string.Empty; // Purchase, Sale
    public int Shares { get; set; }
    public decimal? SharePrice { get; set; }
    public decimal? TransactionValue { get; set; }
}

/// <summary>
/// Technical indicator data point
/// </summary>
public class TechnicalIndicatorValue
{
    public DateTime Timestamp { get; set; }
    public decimal Value { get; set; }
    public Dictionary<string, decimal> AdditionalValues { get; set; } = new(); // For indicators with multiple outputs (e.g., MACD)
}

/// <summary>
/// Economic indicator data
/// </summary>
public class EconomicIndicator
{
    public string Name { get; set; } = string.Empty;
    public string Unit { get; set; } = string.Empty;
    public List<EconomicDataPoint> DataPoints { get; set; } = new();
}

public class EconomicDataPoint
{
    public DateTime Date { get; set; }
    public decimal Value { get; set; }
}

/// <summary>
/// Forex exchange rate
/// </summary>
public class ForexRate
{
    public string FromCurrency { get; set; } = string.Empty;
    public string ToCurrency { get; set; } = string.Empty;
    public decimal ExchangeRate { get; set; }
    public decimal? BidPrice { get; set; }
    public decimal? AskPrice { get; set; }
    public DateTime LastRefreshed { get; set; }
}

/// <summary>
/// Cryptocurrency data
/// </summary>
public class CryptoData
{
    public string Symbol { get; set; } = string.Empty;
    public string Market { get; set; } = string.Empty;
    public decimal Price { get; set; }
    public long Volume { get; set; }
    public DateTime LastRefreshed { get; set; }
}

/// <summary>
/// Commodity data
/// </summary>
public class CommodityData
{
    public string Name { get; set; } = string.Empty;
    public string Unit { get; set; } = string.Empty;
    public List<CommodityDataPoint> DataPoints { get; set; } = new();
}

public class CommodityDataPoint
{
    public DateTime Date { get; set; }
    public decimal Value { get; set; }
}

/// <summary>
/// Market status
/// </summary>
public class MarketStatus
{
    public List<MarketInfo> Markets { get; set; } = new();
}

public class MarketInfo
{
    public string MarketType { get; set; } = string.Empty; // "Equity", "Forex", "Crypto"
    public string Region { get; set; } = string.Empty;
    public string PrimaryExchanges { get; set; } = string.Empty;
    public string LocalOpen { get; set; } = string.Empty;
    public string LocalClose { get; set; } = string.Empty;
    public string CurrentStatus { get; set; } = string.Empty; // "open" or "closed"
    public string Notes { get; set; } = string.Empty;
}

/// <summary>
/// Top movers data
/// </summary>
public class TopMoversData
{
    public List<MoverStock> TopGainers { get; set; } = new();
    public List<MoverStock> TopLosers { get; set; } = new();
    public List<MoverStock> MostActivelyTraded { get; set; } = new();
    public DateTime LastUpdated { get; set; }
}

public class MoverStock
{
    public string Symbol { get; set; } = string.Empty;
    public decimal Price { get; set; }
    public decimal ChangeAmount { get; set; }
    public decimal ChangePercentage { get; set; }
    public long Volume { get; set; }
}
