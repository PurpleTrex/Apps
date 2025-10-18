using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Core.Models;
using RiskPortfolio.Infrastructure.Options;

namespace RiskPortfolio.Infrastructure.Services;

public class RiskAssessmentService : IRiskAssessmentService
{
    private readonly IPortfolioRepository _repository;
    private readonly IRiskMetricCache _cache;
    private readonly ILogger<RiskAssessmentService> _logger;
    private readonly RiskAssessmentOptions _options;

    public RiskAssessmentService(
        IPortfolioRepository repository,
        IRiskMetricCache cache,
        IOptions<RiskAssessmentOptions> options,
        ILogger<RiskAssessmentService> logger)
    {
        _repository = repository;
        _cache = cache;
        _logger = logger;
        _options = options.Value;
    }

    public async Task<RiskAssessmentResult> RecalculateAsync(Guid portfolioId, CancellationToken cancellationToken = default)
    {
        var portfolio = await _repository.GetAsync(portfolioId, cancellationToken)
            ?? throw new InvalidOperationException($"Portfolio {portfolioId} was not found.");

        var result = await CalculateInternalAsync(portfolio, cancellationToken);
        await _repository.UpdateAsync(portfolio, cancellationToken);
        await _cache.SetAsync(result, TimeSpan.FromMinutes(_options.CacheMinutes), cancellationToken);

        return result;
    }

    public async Task<RiskAssessmentResult> CalculateAsync(Portfolio portfolio, CancellationToken cancellationToken = default)
    {
        var cached = await _cache.GetAsync(portfolio.Id, cancellationToken);
        if (cached != null && cached.CalculatedAt > DateTime.UtcNow.AddMinutes(-_options.CacheMinutes))
        {
            return cached;
        }

        var result = await CalculateInternalAsync(portfolio, cancellationToken);
        await _cache.SetAsync(result, TimeSpan.FromMinutes(_options.CacheMinutes), cancellationToken);
        return result;
    }

    private Task<RiskAssessmentResult> CalculateInternalAsync(Portfolio portfolio, CancellationToken cancellationToken)
    {
        cancellationToken.ThrowIfCancellationRequested();

        var totalValue = portfolio.TotalValue;
        if (totalValue == 0)
        {
            var zeroResult = new RiskAssessmentResult(portfolio.Id, 0, 0, 0, RiskClassification.Unspecified, DateTime.UtcNow);
            portfolio.UpdateRiskMetrics(0, 0, zeroResult.CalculatedAt);
            return Task.FromResult(zeroResult);
        }

        var riskContribution = portfolio.Positions.Sum(p => p.RiskContribution);
        var riskScore = totalValue <= 0 ? 0 : Math.Clamp((riskContribution / totalValue) * 100, 0, 200);

        var weightedVolatility = CalculateWeightedVolatility(portfolio, totalValue);
        var valueAtRisk = totalValue * weightedVolatility * _options.StressMultiplier;
        var classification = ClassifyRisk(riskScore);
        var calculatedAt = DateTime.UtcNow;

        portfolio.UpdateRiskMetrics(riskScore, valueAtRisk, calculatedAt);

        var result = new RiskAssessmentResult(
            portfolio.Id,
            riskScore,
            totalValue,
            valueAtRisk,
            classification,
            calculatedAt);

        _logger.LogInformation(
            "Calculated risk for portfolio {PortfolioId} - Score: {RiskScore:F2}, VAR: {VaR:F2}, Classification: {Classification}",
            portfolio.Id,
            riskScore,
            valueAtRisk,
            classification);

        return Task.FromResult(result);
    }

    private static decimal CalculateWeightedVolatility(Portfolio portfolio, decimal totalValue)
    {
        if (!portfolio.Positions.Any() || totalValue <= 0)
        {
            return 0m;
        }

        var variance = portfolio.Positions.Sum(position =>
        {
            var weight = position.MarketValue / totalValue;
            var weightSquared = Math.Pow((double)weight, 2);
            var volatilitySquared = Math.Pow((double)position.Volatility, 2);
            return (decimal)(weightSquared * volatilitySquared);
        });

        var volatility = Math.Sqrt(Math.Max((double)variance, 0));
        return (decimal)volatility;
    }

    private static RiskClassification ClassifyRisk(decimal riskScore)
    {
        return riskScore switch
        {
            < 15 => RiskClassification.Low,
            < 35 => RiskClassification.Moderate,
            < 60 => RiskClassification.Elevated,
            _ => RiskClassification.Critical
        };
    }
}
