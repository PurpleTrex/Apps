using System;

namespace RiskPortfolio.Core.Models;

public sealed record RiskAssessmentResult(
    Guid PortfolioId,
    decimal RiskScore,
    decimal TotalValue,
    decimal ValueAtRisk,
    RiskClassification Classification,
    DateTimeOffset CalculatedAt
);

public enum RiskClassification
{
    Unspecified = 0,
    Low = 1,
    Moderate = 2,
    Elevated = 3,
    Critical = 4
}
