using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Core.Models.MarketData;
using RiskPortfolio.Infrastructure.Options;

namespace RiskPortfolio.Infrastructure.Services;

/// <summary>
/// Comprehensive implementation of market data service using Alpha Vantage API
/// Supports: Stock time series, Fundamentals, News, Technical indicators, Economic data, Forex, Crypto, Commodities
/// Rate Limit: Free tier = 25 calls/day, 5 calls/minute
/// </summary>
public class AlphaVantageMarketDataService : IMarketDataService
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<AlphaVantageMarketDataService> _logger;
    private readonly AlphaVantageOptions _options;
    private readonly IMemoryCache? _cache;
    private static readonly SemaphoreSlim _rateLimiter = new(1, 1);
    private static DateTime _lastApiCall = DateTime.MinValue;

    public AlphaVantageMarketDataService(
        HttpClient httpClient,
        ILogger<AlphaVantageMarketDataService> logger,
        IOptions<AlphaVantageOptions> options,
        IMemoryCache? cache = null)
    {
        _httpClient = httpClient;
        _logger = logger;
        _options = options.Value;
        _cache = cache;
        
        _httpClient.BaseAddress = new Uri(_options.BaseUrl);
        _httpClient.Timeout = TimeSpan.FromSeconds(_options.RequestTimeoutSeconds);
    }

    // ============ HELPER METHODS ============

    private async Task<T?> FetchFromApiAsync<T>(string url, string cacheKey, TimeSpan cacheDuration, CancellationToken cancellationToken) where T : class
    {
        // Check cache first
        if (_cache != null && _cache.TryGetValue<T>(cacheKey, out var cached))
        {
            _logger.LogDebug("Returning cached data for key: {CacheKey}", cacheKey);
            return cached;
        }

        // Rate limiting: ensure 12 seconds between API calls (5 calls per minute)
        await _rateLimiter.WaitAsync(cancellationToken);
        try
        {
            var timeSinceLastCall = DateTime.UtcNow - _lastApiCall;
            var minDelay = TimeSpan.FromSeconds(12);
            if (timeSinceLastCall < minDelay)
            {
                var delay = minDelay - timeSinceLastCall;
                _logger.LogDebug("Rate limiting: waiting {Delay}ms", delay.TotalMilliseconds);
                await Task.Delay(delay, cancellationToken);
            }

            var response = await _httpClient.GetAsync(url, cancellationToken);
            _lastApiCall = DateTime.UtcNow;

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("API request failed. URL: {Url}, Status: {Status}", url, response.StatusCode);
                return null;
            }

            var content = await response.Content.ReadAsStringAsync(cancellationToken);
            
            // Check for API error messages
            if (content.Contains("\"Error Message\"") || content.Contains("\"Note\""))
            {
                _logger.LogWarning("API returned error or rate limit message: {Content}", content.Substring(0, Math.Min(200, content.Length)));
                return null;
            }

            var result = JsonSerializer.Deserialize<T>(content, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true,
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull
            });

            // Cache the result
            if (_cache != null && result != null)
            {
                _cache.Set(cacheKey, result, cacheDuration);
            }

            return result;
        }
        finally
        {
            _rateLimiter.Release();
        }
    }

    private decimal ParseDecimal(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
            return 0;
        
        // Remove % sign if present
        value = value.Replace("%", "").Trim();
        
        if (decimal.TryParse(value, out var result))
            return result;
        
        return 0;
    }

    private long ParseLong(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
            return 0;
        
        if (long.TryParse(value, out var result))
            return result;
        
        return 0;
    }

    // ============ CORE STOCK TIME SERIES ============

    public async Task<decimal?> GetCurrentPriceAsync(string symbol, CancellationToken cancellationToken = default)
    {
        var quote = await GetQuoteAsync(symbol, cancellationToken);
        return quote?.Price;
    }

    public async Task<MarketQuote?> GetQuoteAsync(string symbol, CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=GLOBAL_QUOTE&symbol={symbol}&apikey={_options.ApiKey}";
            var cacheKey = $"quote_{symbol}";
            
            var response = await FetchFromApiAsync<AlphaVantageQuoteResponse>(
                url, cacheKey, TimeSpan.FromMinutes(5), cancellationToken);

            if (response?.GlobalQuote == null)
            {
                _logger.LogWarning("No quote data returned for {Symbol}", symbol);
                return null;
            }

            var quote = new MarketQuote
            {
                Symbol = symbol,
                Price = ParseDecimal(response.GlobalQuote.Price),
                Change = ParseDecimal(response.GlobalQuote.Change),
                ChangePercent = ParseDecimal(response.GlobalQuote.ChangePercent),
                Volume = ParseDecimal(response.GlobalQuote.Volume),
                High = ParseDecimal(response.GlobalQuote.High),
                Low = ParseDecimal(response.GlobalQuote.Low),
                Open = ParseDecimal(response.GlobalQuote.Open),
                PreviousClose = ParseDecimal(response.GlobalQuote.PreviousClose),
                LastUpdated = DateTime.UtcNow
            };

            // Calculate simple volatility estimate from daily change
            if (quote.ChangePercent.HasValue && quote.ChangePercent.Value != 0)
            {
                quote.Volatility = Math.Abs(quote.ChangePercent.Value / 100m) * (decimal)Math.Sqrt(252);
            }

            return quote;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching quote for {Symbol}", symbol);
            return null;
        }
    }

    public async Task<Dictionary<string, MarketQuote>> GetBatchQuotesAsync(IEnumerable<string> symbols, CancellationToken cancellationToken = default)
    {
        var result = new Dictionary<string, MarketQuote>();
        
        foreach (var symbol in symbols)
        {
            if (cancellationToken.IsCancellationRequested)
                break;

            var quote = await GetQuoteAsync(symbol, cancellationToken);
            if (quote != null)
            {
                result[symbol] = quote;
            }
        }

        return result;
    }

    public async Task<List<TimeSeriesDataPoint>> GetIntradayDataAsync(string symbol, string interval = "60min", bool outputFull = false, CancellationToken cancellationToken = default)
    {
        try
        {
            var size = outputFull ? "full" : "compact";
            var url = $"?function=TIME_SERIES_INTRADAY&symbol={symbol}&interval={interval}&outputsize={size}&apikey={_options.ApiKey}";
            var cacheKey = $"intraday_{symbol}_{interval}_{size}";
            
            var response = await FetchFromApiAsync<Dictionary<string, object>>(
                url, cacheKey, TimeSpan.FromMinutes(15), cancellationToken);

            if (response == null)
                return new List<TimeSeriesDataPoint>();

            var timeSeriesKey = $"Time Series ({interval})";
            if (!response.ContainsKey(timeSeriesKey))
                return new List<TimeSeriesDataPoint>();

            var timeSeries = JsonSerializer.Deserialize<Dictionary<string, Dictionary<string, string>>>(
                response[timeSeriesKey].ToString()!);

            if (timeSeries == null)
                return new List<TimeSeriesDataPoint>();

            return timeSeries.Select(kvp => new TimeSeriesDataPoint
            {
                Timestamp = DateTime.Parse(kvp.Key),
                Open = ParseDecimal(kvp.Value.GetValueOrDefault("1. open")),
                High = ParseDecimal(kvp.Value.GetValueOrDefault("2. high")),
                Low = ParseDecimal(kvp.Value.GetValueOrDefault("3. low")),
                Close = ParseDecimal(kvp.Value.GetValueOrDefault("4. close")),
                Volume = ParseLong(kvp.Value.GetValueOrDefault("5. volume"))
            }).OrderByDescending(d => d.Timestamp).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching intraday data for {Symbol}", symbol);
            return new List<TimeSeriesDataPoint>();
        }
    }

    public async Task<List<TimeSeriesDataPoint>> GetDailyDataAsync(string symbol, bool outputFull = false, CancellationToken cancellationToken = default)
    {
        try
        {
            var size = outputFull ? "full" : "compact";
            var url = $"?function=TIME_SERIES_DAILY&symbol={symbol}&outputsize={size}&apikey={_options.ApiKey}";
            var cacheKey = $"daily_{symbol}_{size}";
            
            var response = await FetchFromApiAsync<Dictionary<string, object>>(
                url, cacheKey, TimeSpan.FromHours(1), cancellationToken);

            if (response == null || !response.ContainsKey("Time Series (Daily)"))
                return new List<TimeSeriesDataPoint>();

            var timeSeries = JsonSerializer.Deserialize<Dictionary<string, Dictionary<string, string>>>(
                response["Time Series (Daily)"].ToString()!);

            if (timeSeries == null)
                return new List<TimeSeriesDataPoint>();

            return timeSeries.Select(kvp => new TimeSeriesDataPoint
            {
                Timestamp = DateTime.Parse(kvp.Key),
                Open = ParseDecimal(kvp.Value.GetValueOrDefault("1. open")),
                High = ParseDecimal(kvp.Value.GetValueOrDefault("2. high")),
                Low = ParseDecimal(kvp.Value.GetValueOrDefault("3. low")),
                Close = ParseDecimal(kvp.Value.GetValueOrDefault("4. close")),
                Volume = ParseLong(kvp.Value.GetValueOrDefault("5. volume"))
            }).OrderByDescending(d => d.Timestamp).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching daily data for {Symbol}", symbol);
            return new List<TimeSeriesDataPoint>();
        }
    }

    public async Task<List<TimeSeriesDataPoint>> GetDailyAdjustedDataAsync(string symbol, bool outputFull = false, CancellationToken cancellationToken = default)
    {
        try
        {
            var size = outputFull ? "full" : "compact";
            var url = $"?function=TIME_SERIES_DAILY_ADJUSTED&symbol={symbol}&outputsize={size}&apikey={_options.ApiKey}";
            var cacheKey = $"daily_adjusted_{symbol}_{size}";
            
            var response = await FetchFromApiAsync<Dictionary<string, object>>(
                url, cacheKey, TimeSpan.FromHours(1), cancellationToken);

            if (response == null || !response.ContainsKey("Time Series (Daily)"))
                return new List<TimeSeriesDataPoint>();

            var timeSeries = JsonSerializer.Deserialize<Dictionary<string, Dictionary<string, string>>>(
                response["Time Series (Daily)"].ToString()!);

            if (timeSeries == null)
                return new List<TimeSeriesDataPoint>();

            return timeSeries.Select(kvp => new TimeSeriesDataPoint
            {
                Timestamp = DateTime.Parse(kvp.Key),
                Open = ParseDecimal(kvp.Value.GetValueOrDefault("1. open")),
                High = ParseDecimal(kvp.Value.GetValueOrDefault("2. high")),
                Low = ParseDecimal(kvp.Value.GetValueOrDefault("3. low")),
                Close = ParseDecimal(kvp.Value.GetValueOrDefault("4. close")),
                AdjustedClose = ParseDecimal(kvp.Value.GetValueOrDefault("5. adjusted close")),
                Volume = ParseLong(kvp.Value.GetValueOrDefault("6. volume")),
                Dividend = ParseDecimal(kvp.Value.GetValueOrDefault("7. dividend amount")),
                SplitCoefficient = ParseDecimal(kvp.Value.GetValueOrDefault("8. split coefficient"))
            }).OrderByDescending(d => d.Timestamp).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching daily adjusted data for {Symbol}", symbol);
            return new List<TimeSeriesDataPoint>();
        }
    }

    public async Task<List<SymbolSearchResult>> SearchSymbolsAsync(string keyword, CancellationToken cancellationToken = default)
    {
        try
        {
            if (string.IsNullOrWhiteSpace(keyword))
                return new List<SymbolSearchResult>();

            var url = $"?function=SYMBOL_SEARCH&keywords={Uri.EscapeDataString(keyword)}&apikey={_options.ApiKey}";
            var cacheKey = $"search_{keyword.ToLower()}";
            
            var response = await FetchFromApiAsync<AlphaVantageSearchResponse>(
                url, cacheKey, TimeSpan.FromMinutes(30), cancellationToken);

            if (response?.BestMatches == null || !response.BestMatches.Any())
                return new List<SymbolSearchResult>();

            return response.BestMatches.Select(match => new SymbolSearchResult
            {
                Symbol = match.Symbol ?? string.Empty,
                Name = match.Name ?? string.Empty,
                Type = match.Type ?? string.Empty,
                Region = match.Region ?? string.Empty,
                Currency = match.Currency ?? string.Empty,
                MarketOpen = match.MarketOpen,
                MarketClose = match.MarketClose,
                Timezone = match.Timezone
            }).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error searching symbols for keyword: {Keyword}", keyword);
            return new List<SymbolSearchResult>();
        }
    }

    public async Task<MarketStatus?> GetMarketStatusAsync(CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=MARKET_STATUS&apikey={_options.ApiKey}";
            var cacheKey = "market_status";
            
            var response = await FetchFromApiAsync<AlphaVantageMarketStatusResponse>(
                url, cacheKey, TimeSpan.FromMinutes(30), cancellationToken);

            if (response?.Markets == null)
                return null;

            return new MarketStatus
            {
                Markets = response.Markets.Select(m => new MarketInfo
                {
                    MarketType = m.MarketType ?? string.Empty,
                    Region = m.Region ?? string.Empty,
                    PrimaryExchanges = m.PrimaryExchanges ?? string.Empty,
                    LocalOpen = m.LocalOpen ?? string.Empty,
                    LocalClose = m.LocalClose ?? string.Empty,
                    CurrentStatus = m.CurrentStatus ?? string.Empty,
                    Notes = m.Notes ?? string.Empty
                }).ToList()
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching market status");
            return null;
        }
    }

    // ============ FUNDAMENTAL DATA ============

    public async Task<FundamentalData?> GetCompanyOverviewAsync(string symbol, CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=OVERVIEW&symbol={symbol}&apikey={_options.ApiKey}";
            var cacheKey = $"fundamentals_{symbol}";
            
            var response = await FetchFromApiAsync<AlphaVantageCompanyOverview>(
                url, cacheKey, TimeSpan.FromHours(24), cancellationToken);

            if (response == null || string.IsNullOrEmpty(response.Symbol))
                return null;

            return new FundamentalData
            {
                Symbol = response.Symbol ?? string.Empty,
                Name = response.Name ?? string.Empty,
                Description = response.Description ?? string.Empty,
                Sector = response.Sector ?? string.Empty,
                Industry = response.Industry ?? string.Empty,
                Exchange = response.Exchange ?? string.Empty,
                Currency = response.Currency ?? string.Empty,
                Country = response.Country ?? string.Empty,
                MarketCapitalization = ParseDecimal(response.MarketCapitalization),
                PERatio = ParseDecimal(response.PERatio),
                PEGRatio = ParseDecimal(response.PEGRatio),
                BookValue = ParseDecimal(response.BookValue),
                DividendYield = ParseDecimal(response.DividendYield),
                EPS = ParseDecimal(response.EPS),
                RevenuePerShare = ParseDecimal(response.RevenuePerShareTTM),
                ProfitMargin = ParseDecimal(response.ProfitMargin),
                OperatingMargin = ParseDecimal(response.OperatingMarginTTM),
                ReturnOnAssets = ParseDecimal(response.ReturnOnAssetsTTM),
                ReturnOnEquity = ParseDecimal(response.ReturnOnEquityTTM),
                Beta = ParseDecimal(response.Beta),
                FiscalYearEnd = response.FiscalYearEnd,
                LatestQuarter = string.IsNullOrEmpty(response.LatestQuarter) ? null : DateTime.Parse(response.LatestQuarter)
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching company overview for {Symbol}", symbol);
            return null;
        }
    }

    public async Task<List<EarningsData>> GetEarningsAsync(string symbol, CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=EARNINGS&symbol={symbol}&apikey={_options.ApiKey}";
            var cacheKey = $"earnings_{symbol}";
            
            var response = await FetchFromApiAsync<AlphaVantageEarningsResponse>(
                url, cacheKey, TimeSpan.FromHours(24), cancellationToken);

            if (response?.QuarterlyEarnings == null)
                return new List<EarningsData>();

            return response.QuarterlyEarnings.Select(e => new EarningsData
            {
                Symbol = symbol,
                FiscalDateEnding = DateTime.Parse(e.FiscalDateEnding ?? DateTime.Now.ToString()),
                ReportedEPS = ParseDecimal(e.ReportedEPS),
                EstimatedEPS = ParseDecimal(e.EstimatedEPS),
                Surprise = ParseDecimal(e.Surprise),
                SurprisePercentage = ParseDecimal(e.SurprisePercentage)
            }).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching earnings for {Symbol}", symbol);
            return new List<EarningsData>();
        }
    }

    public async Task<List<InsiderTransaction>> GetInsiderTransactionsAsync(string symbol, CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=INSIDER_TRANSACTIONS&symbol={symbol}&apikey={_options.ApiKey}";
            var cacheKey = $"insider_{symbol}";
            
            var response = await FetchFromApiAsync<AlphaVantageInsiderResponse>(
                url, cacheKey, TimeSpan.FromHours(24), cancellationToken);

            if (response?.Data == null)
                return new List<InsiderTransaction>();

            return response.Data.Select(t => new InsiderTransaction
            {
                Symbol = symbol,
                Name = t.Name ?? string.Empty,
                Title = t.Title ?? string.Empty,
                TransactionDate = DateTime.Parse(t.TransactionDate ?? DateTime.Now.ToString()),
                TransactionType = t.AcquisitionOrDisposition == "A" ? "Purchase" : "Sale",
                Shares = int.TryParse(t.Shares, out var shares) ? shares : 0,
                SharePrice = ParseDecimal(t.SecurityPrice),
                TransactionValue = ParseDecimal(t.TransactionValue)
            }).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching insider transactions for {Symbol}", symbol);
            return new List<InsiderTransaction>();
        }
    }

    // ============ ALPHA INTELLIGENCE ============

    public async Task<List<NewsSentiment>> GetNewsSentimentAsync(string? tickers = null, string? topics = null, int limit = 50, CancellationToken cancellationToken = default)
    {
        try
        {
            var urlParams = $"?function=NEWS_SENTIMENT&limit={limit}&apikey={_options.ApiKey}";
            if (!string.IsNullOrEmpty(tickers))
                urlParams += $"&tickers={Uri.EscapeDataString(tickers)}";
            if (!string.IsNullOrEmpty(topics))
                urlParams += $"&topics={Uri.EscapeDataString(topics)}";

            var cacheKey = $"news_{tickers}_{topics}_{limit}";
            
            var response = await FetchFromApiAsync<AlphaVantageNewsResponse>(
                urlParams, cacheKey, TimeSpan.FromHours(1), cancellationToken);

            if (response?.Feed == null)
                return new List<NewsSentiment>();

            return response.Feed.Select(article => new NewsSentiment
            {
                Title = article.Title ?? string.Empty,
                Url = article.Url ?? string.Empty,
                TimePublished = DateTime.TryParse(article.TimePublished, out var time) ? time : DateTime.Now,
                Authors = article.Authors ?? new List<string>(),
                Summary = article.Summary ?? string.Empty,
                BannerImage = article.BannerImage ?? string.Empty,
                Source = article.Source ?? string.Empty,
                Topics = article.Topics?.Select(t => t.Topic ?? string.Empty).ToList() ?? new List<string>(),
                OverallSentimentScore = ParseDecimal(article.OverallSentimentScore),
                OverallSentimentLabel = article.OverallSentimentLabel,
                TickerSentiments = article.TickerSentiment?.Select(ts => new TickerSentiment
                {
                    Ticker = ts.Ticker ?? string.Empty,
                    SentimentScore = ParseDecimal(ts.TickerSentimentScore),
                    SentimentLabel = ts.TickerSentimentLabel ?? string.Empty
                }).ToList() ?? new List<TickerSentiment>()
            }).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching news sentiment");
            return new List<NewsSentiment>();
        }
    }

    public async Task<TopMoversData?> GetTopMoversAsync(CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=TOP_GAINERS_LOSERS&apikey={_options.ApiKey}";
            var cacheKey = "top_movers";
            
            var response = await FetchFromApiAsync<AlphaVantageTopMoversResponse>(
                url, cacheKey, TimeSpan.FromMinutes(15), cancellationToken);

            if (response == null)
                return null;

            return new TopMoversData
            {
                TopGainers = response.TopGainers?.Select(MapToMoverStock).ToList() ?? new List<MoverStock>(),
                TopLosers = response.TopLosers?.Select(MapToMoverStock).ToList() ?? new List<MoverStock>(),
                MostActivelyTraded = response.MostActivelyTraded?.Select(MapToMoverStock).ToList() ?? new List<MoverStock>(),
                LastUpdated = DateTime.TryParse(response.LastUpdated, out var time) ? time : DateTime.Now
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching top movers");
            return null;
        }
    }

    private MoverStock MapToMoverStock(AlphaVantageMover mover)
    {
        return new MoverStock
        {
            Symbol = mover.Ticker ?? string.Empty,
            Price = ParseDecimal(mover.Price),
            ChangeAmount = ParseDecimal(mover.ChangeAmount),
            ChangePercentage = ParseDecimal(mover.ChangePercentage),
            Volume = ParseLong(mover.Volume)
        };
    }

    // ============ TECHNICAL INDICATORS ============

    public async Task<List<TechnicalIndicatorValue>> GetSMAAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default)
    {
        return await GetTechnicalIndicatorAsync("SMA", symbol, interval, timePeriod, seriesType, cancellationToken);
    }

    public async Task<List<TechnicalIndicatorValue>> GetEMAAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default)
    {
        return await GetTechnicalIndicatorAsync("EMA", symbol, interval, timePeriod, seriesType, cancellationToken);
    }

    public async Task<List<TechnicalIndicatorValue>> GetRSIAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default)
    {
        return await GetTechnicalIndicatorAsync("RSI", symbol, interval, timePeriod, seriesType, cancellationToken);
    }

    public async Task<List<TechnicalIndicatorValue>> GetMACDAsync(string symbol, string interval, string seriesType = "close", CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=MACD&symbol={symbol}&interval={interval}&series_type={seriesType}&apikey={_options.ApiKey}";
            var cacheKey = $"macd_{symbol}_{interval}_{seriesType}";
            
            var response = await FetchFromApiAsync<Dictionary<string, object>>(
                url, cacheKey, TimeSpan.FromHours(1), cancellationToken);

            if (response == null || !response.ContainsKey("Technical Analysis: MACD"))
                return new List<TechnicalIndicatorValue>();

            var data = JsonSerializer.Deserialize<Dictionary<string, Dictionary<string, string>>>(
                response["Technical Analysis: MACD"].ToString()!);

            if (data == null)
                return new List<TechnicalIndicatorValue>();

            return data.Select(kvp => new TechnicalIndicatorValue
            {
                Timestamp = DateTime.Parse(kvp.Key),
                Value = ParseDecimal(kvp.Value.GetValueOrDefault("MACD")),
                AdditionalValues = new Dictionary<string, decimal>
                {
                    ["MACD_Signal"] = ParseDecimal(kvp.Value.GetValueOrDefault("MACD_Signal")),
                    ["MACD_Hist"] = ParseDecimal(kvp.Value.GetValueOrDefault("MACD_Hist"))
                }
            }).OrderByDescending(v => v.Timestamp).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching MACD for {Symbol}", symbol);
            return new List<TechnicalIndicatorValue>();
        }
    }

    public async Task<List<TechnicalIndicatorValue>> GetBollingerBandsAsync(string symbol, string interval, int timePeriod, string seriesType = "close", CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=BBANDS&symbol={symbol}&interval={interval}&time_period={timePeriod}&series_type={seriesType}&apikey={_options.ApiKey}";
            var cacheKey = $"bbands_{symbol}_{interval}_{timePeriod}_{seriesType}";
            
            var response = await FetchFromApiAsync<Dictionary<string, object>>(
                url, cacheKey, TimeSpan.FromHours(1), cancellationToken);

            if (response == null || !response.ContainsKey("Technical Analysis: BBANDS"))
                return new List<TechnicalIndicatorValue>();

            var data = JsonSerializer.Deserialize<Dictionary<string, Dictionary<string, string>>>(
                response["Technical Analysis: BBANDS"].ToString()!);

            if (data == null)
                return new List<TechnicalIndicatorValue>();

            return data.Select(kvp => new TechnicalIndicatorValue
            {
                Timestamp = DateTime.Parse(kvp.Key),
                Value = ParseDecimal(kvp.Value.GetValueOrDefault("Real Middle Band")),
                AdditionalValues = new Dictionary<string, decimal>
                {
                    ["Upper_Band"] = ParseDecimal(kvp.Value.GetValueOrDefault("Real Upper Band")),
                    ["Lower_Band"] = ParseDecimal(kvp.Value.GetValueOrDefault("Real Lower Band"))
                }
            }).OrderByDescending(v => v.Timestamp).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching Bollinger Bands for {Symbol}", symbol);
            return new List<TechnicalIndicatorValue>();
        }
    }

    private async Task<List<TechnicalIndicatorValue>> GetTechnicalIndicatorAsync(string indicator, string symbol, string interval, int timePeriod, string seriesType, CancellationToken cancellationToken)
    {
        try
        {
            var url = $"?function={indicator}&symbol={symbol}&interval={interval}&time_period={timePeriod}&series_type={seriesType}&apikey={_options.ApiKey}";
            var cacheKey = $"{indicator.ToLower()}_{symbol}_{interval}_{timePeriod}_{seriesType}";
            
            var response = await FetchFromApiAsync<Dictionary<string, object>>(
                url, cacheKey, TimeSpan.FromHours(1), cancellationToken);

            if (response == null || !response.ContainsKey($"Technical Analysis: {indicator}"))
                return new List<TechnicalIndicatorValue>();

            var data = JsonSerializer.Deserialize<Dictionary<string, Dictionary<string, string>>>(
                response[$"Technical Analysis: {indicator}"].ToString()!);

            if (data == null)
                return new List<TechnicalIndicatorValue>();

            return data.Select(kvp => new TechnicalIndicatorValue
            {
                Timestamp = DateTime.Parse(kvp.Key),
                Value = ParseDecimal(kvp.Value.Values.FirstOrDefault())
            }).OrderByDescending(v => v.Timestamp).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching {Indicator} for {Symbol}", indicator, symbol);
            return new List<TechnicalIndicatorValue>();
        }
    }

    // ============ FOREX ============

    public async Task<ForexRate?> GetForexRateAsync(string fromCurrency, string toCurrency, CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=CURRENCY_EXCHANGE_RATE&from_currency={fromCurrency}&to_currency={toCurrency}&apikey={_options.ApiKey}";
            var cacheKey = $"forex_{fromCurrency}_{toCurrency}";
            
            var response = await FetchFromApiAsync<AlphaVantageForexResponse>(
                url, cacheKey, TimeSpan.FromMinutes(5), cancellationToken);

            if (response?.RealtimeCurrencyExchangeRate == null)
                return null;

            var data = response.RealtimeCurrencyExchangeRate;
            return new ForexRate
            {
                FromCurrency = data.FromCurrencyCode ?? fromCurrency,
                ToCurrency = data.ToCurrencyCode ?? toCurrency,
                ExchangeRate = ParseDecimal(data.ExchangeRate),
                BidPrice = ParseDecimal(data.BidPrice),
                AskPrice = ParseDecimal(data.AskPrice),
                LastRefreshed = DateTime.TryParse(data.LastRefreshed, out var time) ? time : DateTime.Now
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching forex rate for {From}/{To}", fromCurrency, toCurrency);
            return null;
        }
    }

    // ============ CRYPTO ============

    public async Task<CryptoData?> GetCryptoRateAsync(string symbol, string market = "USD", CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=CURRENCY_EXCHANGE_RATE&from_currency={symbol}&to_currency={market}&apikey={_options.ApiKey}";
            var cacheKey = $"crypto_{symbol}_{market}";
            
            var response = await FetchFromApiAsync<AlphaVantageForexResponse>(
                url, cacheKey, TimeSpan.FromMinutes(5), cancellationToken);

            if (response?.RealtimeCurrencyExchangeRate == null)
                return null;

            var data = response.RealtimeCurrencyExchangeRate;
            return new CryptoData
            {
                Symbol = symbol,
                Market = market,
                Price = ParseDecimal(data.ExchangeRate),
                Volume = 0, // Not provided in exchange rate endpoint
                LastRefreshed = DateTime.TryParse(data.LastRefreshed, out var time) ? time : DateTime.Now
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching crypto rate for {Symbol}/{Market}", symbol, market);
            return null;
        }
    }

    // ============ ECONOMIC INDICATORS ============

    public async Task<EconomicIndicator?> GetRealGDPAsync(string interval = "annual", CancellationToken cancellationToken = default)
    {
        return await GetEconomicDataAsync("REAL_GDP", "Real GDP", interval, cancellationToken);
    }

    public async Task<EconomicIndicator?> GetUnemploymentAsync(CancellationToken cancellationToken = default)
    {
        return await GetEconomicDataAsync("UNEMPLOYMENT", "Unemployment Rate", null, cancellationToken);
    }

    public async Task<EconomicIndicator?> GetCPIAsync(string interval = "monthly", CancellationToken cancellationToken = default)
    {
        return await GetEconomicDataAsync("CPI", "Consumer Price Index", interval, cancellationToken);
    }

    public async Task<EconomicIndicator?> GetFederalFundsRateAsync(string interval = "monthly", CancellationToken cancellationToken = default)
    {
        return await GetEconomicDataAsync("FEDERAL_FUNDS_RATE", "Federal Funds Rate", interval, cancellationToken);
    }

    public async Task<EconomicIndicator?> GetTreasuryYieldAsync(string interval = "monthly", string maturity = "10year", CancellationToken cancellationToken = default)
    {
        try
        {
            var url = $"?function=TREASURY_YIELD&interval={interval}&maturity={maturity}&apikey={_options.ApiKey}";
            var cacheKey = $"treasury_{interval}_{maturity}";
            
            var response = await FetchFromApiAsync<AlphaVantageEconomicResponse>(
                url, cacheKey, TimeSpan.FromDays(7), cancellationToken);

            if (response?.Data == null)
                return null;

            return new EconomicIndicator
            {
                Name = $"Treasury Yield ({maturity})",
                Unit = "percent",
                DataPoints = response.Data.Select(d => new EconomicDataPoint
                {
                    Date = DateTime.Parse(d.Date ?? DateTime.Now.ToString()),
                    Value = ParseDecimal(d.Value)
                }).ToList()
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching treasury yield");
            return null;
        }
    }

    private async Task<EconomicIndicator?> GetEconomicDataAsync(string function, string name, string? interval, CancellationToken cancellationToken)
    {
        try
        {
            var url = $"?function={function}&apikey={_options.ApiKey}";
            if (!string.IsNullOrEmpty(interval))
                url += $"&interval={interval}";

            var cacheKey = $"economic_{function}_{interval}";
            
            var response = await FetchFromApiAsync<AlphaVantageEconomicResponse>(
                url, cacheKey, TimeSpan.FromDays(7), cancellationToken);

            if (response?.Data == null)
                return null;

            return new EconomicIndicator
            {
                Name = name,
                Unit = response.Unit ?? "value",
                DataPoints = response.Data.Select(d => new EconomicDataPoint
                {
                    Date = DateTime.Parse(d.Date ?? DateTime.Now.ToString()),
                    Value = ParseDecimal(d.Value)
                }).ToList()
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching economic indicator {Function}", function);
            return null;
        }
    }

    // ============ COMMODITIES ============

    public async Task<CommodityData?> GetWTIOilAsync(string interval = "monthly", CancellationToken cancellationToken = default)
    {
        return await GetCommodityDataAsync("WTI", "WTI Crude Oil", interval, cancellationToken);
    }

    public async Task<CommodityData?> GetNaturalGasAsync(string interval = "monthly", CancellationToken cancellationToken = default)
    {
        return await GetCommodityDataAsync("NATURAL_GAS", "Natural Gas", interval, cancellationToken);
    }

    private async Task<CommodityData?> GetCommodityDataAsync(string function, string name, string interval, CancellationToken cancellationToken)
    {
        try
        {
            var url = $"?function={function}&interval={interval}&apikey={_options.ApiKey}";
            var cacheKey = $"commodity_{function}_{interval}";
            
            var response = await FetchFromApiAsync<AlphaVantageCommodityResponse>(
                url, cacheKey, TimeSpan.FromDays(7), cancellationToken);

            if (response?.Data == null)
                return null;

            return new CommodityData
            {
                Name = name,
                Unit = response.Unit ?? "USD",
                DataPoints = response.Data.Select(d => new CommodityDataPoint
                {
                    Date = DateTime.Parse(d.Date ?? DateTime.Now.ToString()),
                    Value = ParseDecimal(d.Value)
                }).ToList()
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching commodity data {Function}", function);
            return null;
        }
    }
}
