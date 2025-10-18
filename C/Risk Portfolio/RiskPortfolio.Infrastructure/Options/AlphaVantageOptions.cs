namespace RiskPortfolio.Infrastructure.Options;

public class AlphaVantageOptions
{
    public const string SectionName = "AlphaVantage";
    
    public string ApiKey { get; set; } = string.Empty;
    public string BaseUrl { get; set; } = "https://www.alphavantage.co/query";
    public int RequestTimeoutSeconds { get; set; } = 30;
    public int CacheDurationMinutes { get; set; } = 5;
}
