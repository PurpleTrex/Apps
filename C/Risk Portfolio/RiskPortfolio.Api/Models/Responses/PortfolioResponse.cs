using System;
using System.Collections.Generic;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Api.Models.Responses;

public sealed record PortfolioResponse(
    Guid Id,
    string Name,
    string Owner,
    string BaseCurrency,
    decimal TotalValue,
    decimal RiskScore,
    decimal ValueAtRisk,
    RiskClassification RiskClassification,
    DateTime CreatedAt,
    DateTime? LastEvaluatedAt,
    IReadOnlyCollection<PositionResponse> Positions
);

public sealed record PositionResponse(
    Guid Id,
    string Symbol,
    decimal Quantity,
    decimal AveragePrice,
    decimal CurrentPrice,
    decimal Volatility,
    decimal MarketValue,
    decimal RiskContribution
);
