using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Prometheus;
using RiskPortfolio.Core.Interfaces;

namespace RiskPortfolio.Infrastructure.Jobs;

public class RiskRecalculationJob
{
    private static readonly Counter RecalculationCounter = Metrics.CreateCounter(
        "risk_portfolio_recalculations_total",
        "Number of portfolio risk recalculations that have been executed.");

    private readonly IPortfolioRepository _portfolioRepository;
    private readonly IRiskAssessmentService _riskAssessmentService;
    private readonly ILogger<RiskRecalculationJob> _logger;

    public RiskRecalculationJob(
        IPortfolioRepository portfolioRepository,
        IRiskAssessmentService riskAssessmentService,
        ILogger<RiskRecalculationJob> logger)
    {
        _portfolioRepository = portfolioRepository;
        _riskAssessmentService = riskAssessmentService;
        _logger = logger;
    }

    public async Task ExecuteAsync(CancellationToken cancellationToken = default)
    {
        var portfolios = await _portfolioRepository.GetAllAsync(cancellationToken);
        foreach (var portfolio in portfolios)
        {
            try
            {
                await _riskAssessmentService.RecalculateAsync(portfolio.Id, cancellationToken);
                RecalculationCounter.Inc();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to recalculate risk metrics for portfolio {PortfolioId}", portfolio.Id);
            }
        }
    }
}
