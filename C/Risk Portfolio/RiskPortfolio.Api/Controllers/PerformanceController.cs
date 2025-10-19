using Microsoft.AspNetCore.Mvc;
using RiskPortfolio.Api.Models.Responses;
using RiskPortfolio.Core.Interfaces;

namespace RiskPortfolio.Api.Controllers;

[ApiController]
[Route("api/portfolios/{portfolioId}/performance")]
public class PerformanceController : ControllerBase
{
    private readonly IPerformanceService _performanceService;
    private readonly IPortfolioRepository _portfolioRepository;
    private readonly ILogger<PerformanceController> _logger;

    public PerformanceController(
        IPerformanceService performanceService,
        IPortfolioRepository portfolioRepository,
        ILogger<PerformanceController> logger)
    {
        _performanceService = performanceService;
        _portfolioRepository = portfolioRepository;
        _logger = logger;
    }

    [HttpGet("historical")]
    public async Task<ActionResult<IEnumerable<PerformanceDataPoint>>> GetHistoricalPerformance(
        Guid portfolioId,
        [FromQuery] int days = 30,
        CancellationToken cancellationToken = default)
    {
        var portfolio = await _portfolioRepository.GetAsync(portfolioId, cancellationToken);
        if (portfolio == null)
            return NotFound($"Portfolio with ID {portfolioId} not found.");

        if (days <= 0 || days > 365)
            return BadRequest("Days parameter must be between 1 and 365.");

        var snapshots = await _performanceService.GetHistoricalPerformanceAsync(portfolioId, days, cancellationToken);
        
        var response = snapshots.Select(s => new PerformanceDataPoint(
            s.SnapshotDate,
            s.TotalValue,
            s.RiskScore,
            s.ValueAtRisk,
            s.DailyReturn
        ));

        return Ok(response);
    }

    [HttpGet("metrics")]
    public async Task<ActionResult<PerformanceMetrics>> GetPerformanceMetrics(
        Guid portfolioId,
        [FromQuery] int days = 30,
        CancellationToken cancellationToken = default)
    {
        var portfolio = await _portfolioRepository.GetAsync(portfolioId, cancellationToken);
        if (portfolio == null)
            return NotFound($"Portfolio with ID {portfolioId} not found.");

        if (days <= 0 || days > 365)
            return BadRequest("Days parameter must be between 1 and 365.");

        var metrics = await _performanceService.GetPerformanceMetricsAsync(portfolioId, days, cancellationToken);
        
        if (metrics == null)
            return NotFound("No performance data available for the specified period.");

        return Ok(metrics);
    }
}
