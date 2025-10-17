namespace RiskPortfolio.Infrastructure.Options;

public class CachingOptions
{
    public const string SectionName = "Caching";

    public string RedisConnectionString { get; set; } = "localhost:6379";
    public bool EnableRedis { get; set; } = true;
}
