using System;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Api.Models.Responses;

public sealed record RiskAssessmentResponse(
    Guid PortfolioId,
    decimal RiskScore,
    decimal TotalValue,
    decimal ValueAtRisk,
    RiskClassification Classification,
    DateTimeOffset CalculatedAt
);
