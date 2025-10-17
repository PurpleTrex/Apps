namespace RiskPortfolio.Infrastructure.Options;

public class DatabaseOptions
{
    public const string SectionName = "Database";

    public string ConnectionString { get; set; } = string.Empty;
    public string HangfireConnectionString { get; set; } = string.Empty;
    public string LoggingConnectionString { get; set; } = string.Empty;
}
