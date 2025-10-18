using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Options;

namespace RiskPortfolio.Infrastructure.Services;

/// <summary>
/// Implementation of market data service using Alpha Vantage API
/// </summary>
public class AlphaVantageMarketDataService : IMarketDataService
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<AlphaVantageMarketDataService> _logger;
    private readonly AlphaVantageOptions _options;
    private readonly IRiskMetricCache? _cache;

    public AlphaVantageMarketDataService(
        HttpClient httpClient,
        ILogger<AlphaVantageMarketDataService> logger,
        IOptions<AlphaVantageOptions> options,
        IRiskMetricCache? cache = null)
    {
        _httpClient = httpClient;
        _logger = logger;
        _options = options.Value;
        _cache = cache;
        
        _httpClient.BaseAddress = new Uri(_options.BaseUrl);
        _httpClient.Timeout = TimeSpan.FromSeconds(_options.RequestTimeoutSeconds);
    }

    public async Task<decimal?> GetCurrentPriceAsync(string symbol, CancellationToken cancellationToken = default)
    {
        var quote = await GetQuoteAsync(symbol, cancellationToken);
        return quote?.Price;
    }

    public async Task<MarketQuote?> GetQuoteAsync(string symbol, CancellationToken cancellationToken = default)
    {
        try
        {
            // Check cache first
            var cacheKey = $"market_quote_{symbol}";
            if (_cache != null)
            {
                var cached = await _cache.GetAsync<MarketQuote>(cacheKey, cancellationToken);
                if (cached != null)
                {
                    _logger.LogDebug("Returning cached quote for {Symbol}", symbol);
                    return cached;
                }
            }

            // Fetch from API
            var url = $"?function=GLOBAL_QUOTE&symbol={symbol}&apikey={_options.ApiKey}";
            var response = await _httpClient.GetAsync(url, cancellationToken);
            
            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("Failed to fetch quote for {Symbol}. Status: {Status}", symbol, response.StatusCode);
                return null;
            }

            var content = await response.Content.ReadAsStringAsync(cancellationToken);
            var result = JsonSerializer.Deserialize<AlphaVantageQuoteResponse>(content, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });

            if (result?.GlobalQuote == null)
            {
                _logger.LogWarning("No quote data returned for {Symbol}", symbol);
                return null;
            }

            var quote = MapToMarketQuote(result.GlobalQuote, symbol);
            
            // Calculate simple volatility from daily change
            if (quote.ChangePercent.HasValue)
            {
                // Rough estimate: annualized volatility from daily change
                quote.Volatility = Math.Abs(quote.ChangePercent.Value / 100m) * (decimal)Math.Sqrt(252); // 252 trading days
            }

            // Cache the result
            if (_cache != null && quote != null)
            {
                await _cache.SetAsync(cacheKey, quote, TimeSpan.FromMinutes(_options.CacheDurationMinutes), cancellationToken);
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
        
        // Alpha Vantage free tier doesn't support batch requests, so we fetch sequentially
        // Add small delay between requests to respect rate limits (5 calls per minute for free tier)
        foreach (var symbol in symbols)
        {
            var quote = await GetQuoteAsync(symbol, cancellationToken);
            if (quote != null)
            {
                result[symbol] = quote;
            }
            
            // Rate limiting: wait 12 seconds between calls (5 calls per minute)
            await Task.Delay(TimeSpan.FromSeconds(12), cancellationToken);
        }

        return result;
    }

    public async Task<List<SymbolSearchResult>> SearchSymbolsAsync(string keyword, CancellationToken cancellationToken = default)
    {
        try
        {
            if (string.IsNullOrWhiteSpace(keyword))
            {
                return new List<SymbolSearchResult>();
            }

            // Check cache first
            var cacheKey = $"symbol_search_{keyword.ToLower()}";
            if (_cache != null)
            {
                var cached = await _cache.GetAsync<List<SymbolSearchResult>>(cacheKey, cancellationToken);
                if (cached != null)
                {
                    _logger.LogDebug("Returning cached search results for {Keyword}", keyword);
                    return cached;
                }
            }

            // Fetch from API
            var url = $"?function=SYMBOL_SEARCH&keywords={Uri.EscapeDataString(keyword)}&apikey={_options.ApiKey}";
            var response = await _httpClient.GetAsync(url, cancellationToken);
            
            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("Failed to search symbols for {Keyword}. Status: {Status}", keyword, response.StatusCode);
                return new List<SymbolSearchResult>();
            }

            var content = await response.Content.ReadAsStringAsync(cancellationToken);
            var result = JsonSerializer.Deserialize<AlphaVantageSearchResponse>(content, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });

            if (result?.BestMatches == null || !result.BestMatches.Any())
            {
                _logger.LogInformation("No matches found for {Keyword}", keyword);
                return new List<SymbolSearchResult>();
            }

            var searchResults = result.BestMatches
                .Select(match => new SymbolSearchResult
                {
                    Symbol = match.Symbol ?? string.Empty,
                    Name = match.Name ?? string.Empty,
                    Type = match.Type ?? string.Empty,
                    Region = match.Region ?? string.Empty,
                    Currency = match.Currency ?? string.Empty,
                    MarketOpen = match.MarketOpen,
                    MarketClose = match.MarketClose,
                    Timezone = match.Timezone
                })
                .ToList();

            // Cache the results
            if (_cache != null)
            {
                await _cache.SetAsync(cacheKey, searchResults, TimeSpan.FromMinutes(_options.CacheDurationMinutes), cancellationToken);
            }

            _logger.LogInformation("Found {Count} matches for {Keyword}", searchResults.Count, keyword);
            return searchResults;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error searching symbols for keyword: {Keyword}", keyword);
            return new List<SymbolSearchResult>();
        }
    }

    private MarketQuote MapToMarketQuote(AlphaVantageGlobalQuote quote, string symbol)
    {
        return new MarketQuote
        {
            Symbol = symbol,
            Price = ParseDecimal(quote.Price),
            Change = ParseDecimal(quote.Change),
            ChangePercent = ParseDecimal(quote.ChangePercent?.Replace("%", "")),
            Volume = ParseDecimal(quote.Volume),
            High = ParseDecimal(quote.High),
            Low = ParseDecimal(quote.Low),
            Open = ParseDecimal(quote.Open),
            PreviousClose = ParseDecimal(quote.PreviousClose),
            LastUpdated = DateTime.UtcNow
        };
    }

    private decimal ParseDecimal(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
            return 0;
        
        if (decimal.TryParse(value, out var result))
            return result;
        
        return 0;
    }

    // Response models for Alpha Vantage API
    private class AlphaVantageQuoteResponse
    {
        [JsonPropertyName("Global Quote")]
        public AlphaVantageGlobalQuote? GlobalQuote { get; set; }
    }

    private class AlphaVantageGlobalQuote
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

    private class AlphaVantageSearchResponse
    {
        [JsonPropertyName("bestMatches")]
        public List<AlphaVantageSearchMatch>? BestMatches { get; set; }
    }

    private class AlphaVantageSearchMatch
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
}
