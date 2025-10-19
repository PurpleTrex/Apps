using System.Text.Json.Serialization;

namespace RiskPortfolio.Infrastructure.Services;

// ============ ALPHA VANTAGE API RESPONSE MODELS ============

internal class AlphaVantageQuoteResponse
{
    [JsonPropertyName("Global Quote")]
    public AlphaVantageGlobalQuote? GlobalQuote { get; set; }
}

internal class AlphaVantageGlobalQuote
{
    [JsonPropertyName("01. symbol")]
    public string? Symbol { get; set; }

    [JsonPropertyName("02. open")]
    public string? Open { get; set; }

    [JsonPropertyName("03. high")]
    public string? High { get; set; }

    [JsonPropertyName("04. low")]
    public string? Low { get; set; }

    [JsonPropertyName("05. price")]
    public string? Price { get; set; }

    [JsonPropertyName("06. volume")]
    public string? Volume { get; set; }

    [JsonPropertyName("07. latest trading day")]
    public string? LatestTradingDay { get; set; }

    [JsonPropertyName("08. previous close")]
    public string? PreviousClose { get; set; }

    [JsonPropertyName("09. change")]
    public string? Change { get; set; }

    [JsonPropertyName("10. change percent")]
    public string? ChangePercent { get; set; }
}

internal class AlphaVantageSearchResponse
{
    [JsonPropertyName("bestMatches")]
    public List<AlphaVantageSearchMatch>? BestMatches { get; set; }
}

internal class AlphaVantageSearchMatch
{
    [JsonPropertyName("1. symbol")]
    public string? Symbol { get; set; }

    [JsonPropertyName("2. name")]
    public string? Name { get; set; }

    [JsonPropertyName("3. type")]
    public string? Type { get; set; }

    [JsonPropertyName("4. region")]
    public string? Region { get; set; }

    [JsonPropertyName("5. marketOpen")]
    public string? MarketOpen { get; set; }

    [JsonPropertyName("6. marketClose")]
    public string? MarketClose { get; set; }

    [JsonPropertyName("7. timezone")]
    public string? Timezone { get; set; }

    [JsonPropertyName("8. currency")]
    public string? Currency { get; set; }

    [JsonPropertyName("9. matchScore")]
    public string? MatchScore { get; set; }
}

internal class AlphaVantageMarketStatusResponse
{
    [JsonPropertyName("markets")]
    public List<AlphaVantageMarket>? Markets { get; set; }
}

internal class AlphaVantageMarket
{
    [JsonPropertyName("market_type")]
    public string? MarketType { get; set; }

    [JsonPropertyName("region")]
    public string? Region { get; set; }

    [JsonPropertyName("primary_exchanges")]
    public string? PrimaryExchanges { get; set; }

    [JsonPropertyName("local_open")]
    public string? LocalOpen { get; set; }

    [JsonPropertyName("local_close")]
    public string? LocalClose { get; set; }

    [JsonPropertyName("current_status")]
    public string? CurrentStatus { get; set; }

    [JsonPropertyName("notes")]
    public string? Notes { get; set; }
}

internal class AlphaVantageCompanyOverview
{
    [JsonPropertyName("Symbol")]
    public string? Symbol { get; set; }

    [JsonPropertyName("Name")]
    public string? Name { get; set; }

    [JsonPropertyName("Description")]
    public string? Description { get; set; }

    [JsonPropertyName("Sector")]
    public string? Sector { get; set; }

    [JsonPropertyName("Industry")]
    public string? Industry { get; set; }

    [JsonPropertyName("Exchange")]
    public string? Exchange { get; set; }

    [JsonPropertyName("Currency")]
    public string? Currency { get; set; }

    [JsonPropertyName("Country")]
    public string? Country { get; set; }

    [JsonPropertyName("MarketCapitalization")]
    public string? MarketCapitalization { get; set; }

    [JsonPropertyName("PERatio")]
    public string? PERatio { get; set; }

    [JsonPropertyName("PEGRatio")]
    public string? PEGRatio { get; set; }

    [JsonPropertyName("BookValue")]
    public string? BookValue { get; set; }

    [JsonPropertyName("DividendYield")]
    public string? DividendYield { get; set; }

    [JsonPropertyName("EPS")]
    public string? EPS { get; set; }

    [JsonPropertyName("RevenuePerShareTTM")]
    public string? RevenuePerShareTTM { get; set; }

    [JsonPropertyName("ProfitMargin")]
    public string? ProfitMargin { get; set; }

    [JsonPropertyName("OperatingMarginTTM")]
    public string? OperatingMarginTTM { get; set; }

    [JsonPropertyName("ReturnOnAssetsTTM")]
    public string? ReturnOnAssetsTTM { get; set; }

    [JsonPropertyName("ReturnOnEquityTTM")]
    public string? ReturnOnEquityTTM { get; set; }

    [JsonPropertyName("Beta")]
    public string? Beta { get; set; }

    [JsonPropertyName("FiscalYearEnd")]
    public string? FiscalYearEnd { get; set; }

    [JsonPropertyName("LatestQuarter")]
    public string? LatestQuarter { get; set; }
}

internal class AlphaVantageEarningsResponse
{
    [JsonPropertyName("quarterlyEarnings")]
    public List<AlphaVantageQuarterlyEarnings>? QuarterlyEarnings { get; set; }
}

internal class AlphaVantageQuarterlyEarnings
{
    [JsonPropertyName("fiscalDateEnding")]
    public string? FiscalDateEnding { get; set; }

    [JsonPropertyName("reportedEPS")]
    public string? ReportedEPS { get; set; }

    [JsonPropertyName("estimatedEPS")]
    public string? EstimatedEPS { get; set; }

    [JsonPropertyName("surprise")]
    public string? Surprise { get; set; }

    [JsonPropertyName("surprisePercentage")]
    public string? SurprisePercentage { get; set; }
}

internal class AlphaVantageInsiderResponse
{
    [JsonPropertyName("data")]
    public List<AlphaVantageInsiderData>? Data { get; set; }
}

internal class AlphaVantageInsiderData
{
    [JsonPropertyName("name")]
    public string? Name { get; set; }

    [JsonPropertyName("title")]
    public string? Title { get; set; }

    [JsonPropertyName("transactionDate")]
    public string? TransactionDate { get; set; }

    [JsonPropertyName("acquisitionOrDisposition")]
    public string? AcquisitionOrDisposition { get; set; }

    [JsonPropertyName("shares")]
    public string? Shares { get; set; }

    [JsonPropertyName("securityPrice")]
    public string? SecurityPrice { get; set; }

    [JsonPropertyName("transactionValue")]
    public string? TransactionValue { get; set; }
}

internal class AlphaVantageNewsResponse
{
    [JsonPropertyName("feed")]
    public List<AlphaVantageNewsArticle>? Feed { get; set; }
}

internal class AlphaVantageNewsArticle
{
    [JsonPropertyName("title")]
    public string? Title { get; set; }

    [JsonPropertyName("url")]
    public string? Url { get; set; }

    [JsonPropertyName("time_published")]
    public string? TimePublished { get; set; }

    [JsonPropertyName("authors")]
    public List<string>? Authors { get; set; }

    [JsonPropertyName("summary")]
    public string? Summary { get; set; }

    [JsonPropertyName("banner_image")]
    public string? BannerImage { get; set; }

    [JsonPropertyName("source")]
    public string? Source { get; set; }

    [JsonPropertyName("topics")]
    public List<AlphaVantageTopic>? Topics { get; set; }

    [JsonPropertyName("overall_sentiment_score")]
    public string? OverallSentimentScore { get; set; }

    [JsonPropertyName("overall_sentiment_label")]
    public string? OverallSentimentLabel { get; set; }

    [JsonPropertyName("ticker_sentiment")]
    public List<AlphaVantageTickerSentiment>? TickerSentiment { get; set; }
}

internal class AlphaVantageTopic
{
    [JsonPropertyName("topic")]
    public string? Topic { get; set; }
}

internal class AlphaVantageTickerSentiment
{
    [JsonPropertyName("ticker")]
    public string? Ticker { get; set; }

    [JsonPropertyName("ticker_sentiment_score")]
    public string? TickerSentimentScore { get; set; }

    [JsonPropertyName("ticker_sentiment_label")]
    public string? TickerSentimentLabel { get; set; }
}

internal class AlphaVantageTopMoversResponse
{
    [JsonPropertyName("last_updated")]
    public string? LastUpdated { get; set; }

    [JsonPropertyName("top_gainers")]
    public List<AlphaVantageMover>? TopGainers { get; set; }

    [JsonPropertyName("top_losers")]
    public List<AlphaVantageMover>? TopLosers { get; set; }

    [JsonPropertyName("most_actively_traded")]
    public List<AlphaVantageMover>? MostActivelyTraded { get; set; }
}

internal class AlphaVantageMover
{
    [JsonPropertyName("ticker")]
    public string? Ticker { get; set; }

    [JsonPropertyName("price")]
    public string? Price { get; set; }

    [JsonPropertyName("change_amount")]
    public string? ChangeAmount { get; set; }

    [JsonPropertyName("change_percentage")]
    public string? ChangePercentage { get; set; }

    [JsonPropertyName("volume")]
    public string? Volume { get; set; }
}

internal class AlphaVantageForexResponse
{
    [JsonPropertyName("Realtime Currency Exchange Rate")]
    public AlphaVantageForexData? RealtimeCurrencyExchangeRate { get; set; }
}

internal class AlphaVantageForexData
{
    [JsonPropertyName("1. From_Currency Code")]
    public string? FromCurrencyCode { get; set; }

    [JsonPropertyName("3. To_Currency Code")]
    public string? ToCurrencyCode { get; set; }

    [JsonPropertyName("5. Exchange Rate")]
    public string? ExchangeRate { get; set; }

    [JsonPropertyName("6. Last Refreshed")]
    public string? LastRefreshed { get; set; }

    [JsonPropertyName("8. Bid Price")]
    public string? BidPrice { get; set; }

    [JsonPropertyName("9. Ask Price")]
    public string? AskPrice { get; set; }
}

internal class AlphaVantageEconomicResponse
{
    [JsonPropertyName("name")]
    public string? Name { get; set; }

    [JsonPropertyName("unit")]
    public string? Unit { get; set; }

    [JsonPropertyName("data")]
    public List<AlphaVantageEconomicData>? Data { get; set; }
}

internal class AlphaVantageEconomicData
{
    [JsonPropertyName("date")]
    public string? Date { get; set; }

    [JsonPropertyName("value")]
    public string? Value { get; set; }
}

internal class AlphaVantageCommodityResponse
{
    [JsonPropertyName("name")]
    public string? Name { get; set; }

    [JsonPropertyName("unit")]
    public string? Unit { get; set; }

    [JsonPropertyName("data")]
    public List<AlphaVantageCommodityData>? Data { get; set; }
}

internal class AlphaVantageCommodityData
{
    [JsonPropertyName("date")]
    public string? Date { get; set; }

    [JsonPropertyName("value")]
    public string? Value { get; set; }
}

// ============ TIME SERIES RESPONSE MODELS ============

internal class AlphaVantageTimeSeriesResponse
{
    [JsonPropertyName("Meta Data")]
    public AlphaVantageMetaData? MetaData { get; set; }

    // Dynamic property names for time series data
    // Will be parsed manually based on function type
    public Dictionary<string, Dictionary<string, string>>? TimeSeriesData { get; set; }
}

internal class AlphaVantageMetaData
{
    [JsonPropertyName("1. Information")]
    public string? Information { get; set; }

    [JsonPropertyName("2. Symbol")]
    public string? Symbol { get; set; }

    [JsonPropertyName("3. Last Refreshed")]
    public string? LastRefreshed { get; set; }

    [JsonPropertyName("4. Interval")]
    public string? Interval { get; set; }

    [JsonPropertyName("5. Output Size")]
    public string? OutputSize { get; set; }

    [JsonPropertyName("6. Time Zone")]
    public string? TimeZone { get; set; }
}

// ============ TECHNICAL INDICATOR RESPONSE MODELS ============

internal class AlphaVantageTechnicalIndicatorResponse
{
    [JsonPropertyName("Meta Data")]
    public AlphaVantageTechnicalMetaData? MetaData { get; set; }

    // Dynamic property name for indicator data (e.g., "Technical Analysis: SMA")
    public Dictionary<string, Dictionary<string, string>>? IndicatorData { get; set; }
}

internal class AlphaVantageTechnicalMetaData
{
    [JsonPropertyName("1: Symbol")]
    public string? Symbol { get; set; }

    [JsonPropertyName("2: Indicator")]
    public string? Indicator { get; set; }

    [JsonPropertyName("3: Last Refreshed")]
    public string? LastRefreshed { get; set; }

    [JsonPropertyName("4: Interval")]
    public string? Interval { get; set; }

    [JsonPropertyName("5: Time Period")]
    public string? TimePeriod { get; set; }

    [JsonPropertyName("6: Series Type")]
    public string? SeriesType { get; set; }

    [JsonPropertyName("7: Time Zone")]
    public string? TimeZone { get; set; }
}
