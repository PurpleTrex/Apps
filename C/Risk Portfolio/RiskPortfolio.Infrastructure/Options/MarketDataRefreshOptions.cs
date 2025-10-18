namespace RiskPortfolio.Infrastructure.Options;

public class MarketDataRefreshOptions
{
    public const string SectionName = "MarketDataRefresh";

    /// <summary>
    /// Whether automatic market data refresh is enabled
    /// </summary>
    public bool Enabled { get; set; } = true;

    /// <summary>
    /// Refresh interval in minutes (default: 60 minutes = 1 hour)
    /// </summary>
    public int RefreshIntervalMinutes { get; set; } = 60;

    /// <summary>
    /// Delay in minutes before first refresh after startup (default: 5 minutes)
    /// </summary>
    public int StartupDelayMinutes { get; set; } = 5;

    /// <summary>
    /// Delay in seconds between individual symbol API calls to respect rate limits (default: 12 seconds = 5 calls per minute)
    /// </summary>
    public int RateLimitDelaySeconds { get; set; } = 12;
}
