using Microsoft.AspNetCore.Mvc;
using RiskPortfolio.Core.Interfaces;

namespace RiskPortfolio.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class MarketDataController : ControllerBase
{
    private readonly IMarketDataService _marketDataService;
    private readonly ILogger<MarketDataController> _logger;

    public MarketDataController(
        IMarketDataService marketDataService,
        ILogger<MarketDataController> logger)
    {
        _marketDataService = marketDataService;
        _logger = logger;
    }

    /// <summary>
    /// Get current price for a stock symbol
    /// </summary>
    [HttpGet("{symbol}/price")]
    [ProducesResponseType(typeof(decimal), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetPrice(string symbol, CancellationToken cancellationToken)
    {
        var price = await _marketDataService.GetCurrentPriceAsync(symbol, cancellationToken);
        
        if (price == null)
        {
            return NotFound(new { message = $"Price data not found for symbol {symbol}" });
        }

        return Ok(new { symbol, price });
    }

    /// <summary>
    /// Get detailed quote information for a stock symbol
    /// </summary>
    [HttpGet("{symbol}/quote")]
    [ProducesResponseType(typeof(MarketQuote), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetQuote(string symbol, CancellationToken cancellationToken)
    {
        var quote = await _marketDataService.GetQuoteAsync(symbol, cancellationToken);
        
        if (quote == null)
        {
            return NotFound(new { message = $"Quote data not found for symbol {symbol}" });
        }

        return Ok(quote);
    }

    /// <summary>
    /// Get quotes for multiple symbols
    /// </summary>
    [HttpPost("quotes")]
    [ProducesResponseType(typeof(Dictionary<string, MarketQuote>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetBatchQuotes(
        [FromBody] BatchQuoteRequest request,
        CancellationToken cancellationToken)
    {
        if (request.Symbols == null || !request.Symbols.Any())
        {
            return BadRequest(new { message = "At least one symbol is required" });
        }

        var quotes = await _marketDataService.GetBatchQuotesAsync(request.Symbols, cancellationToken);
        return Ok(quotes);
    }

    /// <summary>
    /// Update position prices with live market data
    /// </summary>
    [HttpPost("refresh-positions")]
    [ProducesResponseType(typeof(Dictionary<string, decimal>), StatusCodes.Status200OK)]
    public async Task<IActionResult> RefreshPositions(
        [FromBody] BatchQuoteRequest request,
        CancellationToken cancellationToken)
    {
        if (request.Symbols == null || !request.Symbols.Any())
        {
            return BadRequest(new { message = "At least one symbol is required" });
        }

        var result = new Dictionary<string, decimal>();
        
        foreach (var symbol in request.Symbols)
        {
            var price = await _marketDataService.GetCurrentPriceAsync(symbol, cancellationToken);
            if (price.HasValue)
            {
                result[symbol] = price.Value;
            }
            
            // Small delay to respect API rate limits
            await Task.Delay(100, cancellationToken);
        }

        return Ok(result);
    }

    /// <summary>
    /// Search for stock symbols by keyword
    /// </summary>
    [HttpGet("search")]
    [ProducesResponseType(typeof(List<SymbolSearchResult>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> SearchSymbols(
        [FromQuery] string keyword,
        CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(keyword))
        {
            return BadRequest(new { message = "Search keyword is required" });
        }

        var results = await _marketDataService.SearchSymbolsAsync(keyword, cancellationToken);
        return Ok(results);
    }
}

public class BatchQuoteRequest
{
    public List<string> Symbols { get; set; } = new();
}
