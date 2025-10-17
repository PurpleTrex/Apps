using System.Collections.Generic;
using System.Linq;
using RiskPortfolio.Api.Models.Responses;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Api.Mappers;

public static class PortfolioMappers
{
    public static PortfolioResponse ToResponse(this Portfolio portfolio, RiskAssessmentResult? risk = null)
    {
        var appliedRisk = risk ?? new RiskAssessmentResult(
            portfolio.Id,
            portfolio.RiskScore,
            portfolio.TotalValue,
            portfolio.ValueAtRisk,
            DeriveClassification(portfolio.RiskScore),
            portfolio.LastEvaluatedAt ?? portfolio.CreatedAt);

        return new PortfolioResponse(
            portfolio.Id,
            portfolio.Name,
            portfolio.Owner,
            portfolio.BaseCurrency,
            portfolio.TotalValue,
            appliedRisk.RiskScore,
            appliedRisk.ValueAtRisk,
            appliedRisk.Classification,
            portfolio.CreatedAt,
            portfolio.LastEvaluatedAt,
            portfolio.Positions.Select(ToResponse).ToList());
    }

    public static PositionResponse ToResponse(this AssetPosition position)
    {
        return new PositionResponse(
            position.Id,
            position.Symbol,
            position.Quantity,
            position.AveragePrice,
            position.CurrentPrice,
            position.Volatility,
            position.MarketValue,
            position.RiskContribution);
    }

    public static RiskAssessmentResponse ToResponse(this RiskAssessmentResult result)
    {
        return new RiskAssessmentResponse(
            result.PortfolioId,
            result.RiskScore,
            result.TotalValue,
            result.ValueAtRisk,
            result.Classification,
            result.CalculatedAt);
    }

    private static RiskClassification DeriveClassification(decimal riskScore)
    {
        return riskScore switch
        {
            < 15 => RiskClassification.Low,
            < 35 => RiskClassification.Moderate,
            < 60 => RiskClassification.Elevated,
            _ when riskScore > 0 => RiskClassification.Critical,
            _ => RiskClassification.Unspecified
        };
    }
}
