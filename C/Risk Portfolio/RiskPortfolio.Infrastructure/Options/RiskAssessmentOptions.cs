namespace RiskPortfolio.Infrastructure.Options;

public class RiskAssessmentOptions
{
    public const string SectionName = "RiskAssessment";

    /// <summary>
    /// Time-to-live for cached risk metrics expressed in minutes.
    /// </summary>
    public int CacheMinutes { get; set; } = 30;

    /// <summary>
    /// Multiplier applied on volatility to compute value at risk (default 99% confidence level).
    /// </summary>
    public decimal StressMultiplier { get; set; } = 2.33m;
}
