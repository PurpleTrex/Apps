using Microsoft.AspNetCore.Mvc;
using RiskPortfolio.Api.Models.Requests;
using RiskPortfolio.Api.Models.Responses;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;

namespace RiskPortfolio.Api.Controllers;

[ApiController]
[Route("api/widgets")]
public class WidgetsController : ControllerBase
{
    private readonly IWidgetRepository _widgetRepository;
    private readonly ILogger<WidgetsController> _logger;
    private const string DefaultUserId = "default-user"; // TODO: Replace with actual auth

    public WidgetsController(
        IWidgetRepository widgetRepository,
        ILogger<WidgetsController> logger)
    {
        _widgetRepository = widgetRepository;
        _logger = logger;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<WidgetResponse>>> GetAll(
        [FromQuery] bool visibleOnly = false,
        CancellationToken cancellationToken = default)
    {
        var widgets = visibleOnly
            ? await _widgetRepository.GetVisibleByUserIdAsync(DefaultUserId, cancellationToken)
            : await _widgetRepository.GetByUserIdAsync(DefaultUserId, cancellationToken);

        var response = widgets.Select(w => new WidgetResponse(
            w.Id,
            w.Title,
            w.Type.ToString(),
            w.Configuration,
            w.PositionX,
            w.PositionY,
            w.Width,
            w.Height,
            w.IsVisible,
            w.DisplayOrder,
            w.CreatedAt,
            w.UpdatedAt
        ));

        return Ok(response);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<WidgetResponse>> GetById(
        Guid id,
        CancellationToken cancellationToken = default)
    {
        var widget = await _widgetRepository.GetByIdAsync(id, cancellationToken);
        if (widget == null)
            return NotFound($"Widget with ID {id} not found");

        var response = new WidgetResponse(
            widget.Id,
            widget.Title,
            widget.Type.ToString(),
            widget.Configuration,
            widget.PositionX,
            widget.PositionY,
            widget.Width,
            widget.Height,
            widget.IsVisible,
            widget.DisplayOrder,
            widget.CreatedAt,
            widget.UpdatedAt
        );

        return Ok(response);
    }

    [HttpPost]
    public async Task<ActionResult<WidgetResponse>> Create(
        [FromBody] CreateWidgetRequest request,
        CancellationToken cancellationToken = default)
    {
        if (!Enum.TryParse<WidgetType>(request.Type, ignoreCase: true, out var widgetType))
            return BadRequest($"Invalid widget type: {request.Type}");

        var widget = new DashboardWidget(
            DefaultUserId,
            request.Title,
            widgetType,
            request.Configuration,
            request.PositionX,
            request.PositionY,
            request.Width,
            request.Height,
            request.DisplayOrder
        );

        await _widgetRepository.AddAsync(widget, cancellationToken);

        _logger.LogInformation("Created widget {WidgetId} of type {Type}", widget.Id, widget.Type);

        var response = new WidgetResponse(
            widget.Id,
            widget.Title,
            widget.Type.ToString(),
            widget.Configuration,
            widget.PositionX,
            widget.PositionY,
            widget.Width,
            widget.Height,
            widget.IsVisible,
            widget.DisplayOrder,
            widget.CreatedAt,
            widget.UpdatedAt
        );

        return CreatedAtAction(nameof(GetById), new { id = widget.Id }, response);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<WidgetResponse>> Update(
        Guid id,
        [FromBody] UpdateWidgetRequest request,
        CancellationToken cancellationToken = default)
    {
        var widget = await _widgetRepository.GetByIdAsync(id, cancellationToken);
        if (widget == null)
            return NotFound($"Widget with ID {id} not found");

        if (request.Title != null)
            widget.UpdateTitle(request.Title);

        if (request.Configuration != null)
            widget.UpdateConfiguration(request.Configuration);

        if (request.PositionX.HasValue && request.PositionY.HasValue)
            widget.UpdatePosition(request.PositionX.Value, request.PositionY.Value);

        if (request.Width.HasValue && request.Height.HasValue)
            widget.UpdateSize(request.Width.Value, request.Height.Value);

        if (request.IsVisible.HasValue)
            widget.SetVisibility(request.IsVisible.Value);

        if (request.DisplayOrder.HasValue)
            widget.UpdateDisplayOrder(request.DisplayOrder.Value);

        await _widgetRepository.UpdateAsync(widget, cancellationToken);

        _logger.LogInformation("Updated widget {WidgetId}", widget.Id);

        var response = new WidgetResponse(
            widget.Id,
            widget.Title,
            widget.Type.ToString(),
            widget.Configuration,
            widget.PositionX,
            widget.PositionY,
            widget.Width,
            widget.Height,
            widget.IsVisible,
            widget.DisplayOrder,
            widget.CreatedAt,
            widget.UpdatedAt
        );

        return Ok(response);
    }

    [HttpPost("batch-update-positions")]
    public async Task<ActionResult> UpdatePositions(
        [FromBody] UpdateWidgetPositionsRequest request,
        CancellationToken cancellationToken = default)
    {
        foreach (var update in request.Widgets)
        {
            var widget = await _widgetRepository.GetByIdAsync(update.Id, cancellationToken);
            if (widget != null)
            {
                widget.UpdatePosition(update.PositionX, update.PositionY);
                widget.UpdateDisplayOrder(update.DisplayOrder);
                await _widgetRepository.UpdateAsync(widget, cancellationToken);
            }
        }

        _logger.LogInformation("Updated positions for {Count} widgets", request.Widgets.Count);

        return Ok();
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult> Delete(
        Guid id,
        CancellationToken cancellationToken = default)
    {
        if (!await _widgetRepository.ExistsAsync(id, cancellationToken))
            return NotFound($"Widget with ID {id} not found");

        await _widgetRepository.DeleteAsync(id, cancellationToken);

        _logger.LogInformation("Deleted widget {WidgetId}", id);

        return NoContent();
    }

    [HttpGet("types")]
    public ActionResult<IEnumerable<WidgetTypeInfo>> GetWidgetTypes()
    {
        var types = Enum.GetValues<WidgetType>()
            .Select(t => new WidgetTypeInfo(
                t.ToString(),
                GetWidgetCategory(t),
                GetWidgetDescription(t),
                GetDefaultWidth(t),
                GetDefaultHeight(t)
            ));

        return Ok(types);
    }

    private static string GetWidgetCategory(WidgetType type) => type switch
    {
        WidgetType.PortfolioValue or WidgetType.PortfolioAllocation or WidgetType.PortfolioPerformance or WidgetType.PortfolioRiskGauge => "Portfolio",
        WidgetType.PositionList or WidgetType.PositionCard or WidgetType.WatchList => "Positions",
        WidgetType.TopGainers or WidgetType.TopLosers or WidgetType.DailyPnL or WidgetType.PerformanceChart => "Performance",
        WidgetType.MarketOverview or WidgetType.SectorPerformance or WidgetType.MarketNews or WidgetType.EconomicCalendar => "Market",
        WidgetType.RiskMetrics or WidgetType.ValueAtRisk or WidgetType.CorrelationMatrix or WidgetType.VolatilityChart => "Risk",
        WidgetType.AssetAllocation or WidgetType.DiversificationScore or WidgetType.RebalancingNeeds or WidgetType.TaxLossHarvesting => "Analytics",
        WidgetType.QuickStats or WidgetType.ReturnsCalendar or WidgetType.Dividends or WidgetType.Transactions => "Statistics",
        _ => "Custom"
    };

    private static string GetWidgetDescription(WidgetType type) => type switch
    {
        WidgetType.PortfolioValue => "Display total portfolio value and period performance",
        WidgetType.PortfolioAllocation => "Show portfolio allocation breakdown by asset",
        WidgetType.PortfolioPerformance => "Track portfolio performance over time",
        WidgetType.PortfolioRiskGauge => "Display current risk score and classification",
        WidgetType.PositionList => "List all positions in a table format",
        WidgetType.PositionCard => "Display a specific position's details",
        WidgetType.WatchList => "Track watchlist symbols",
        WidgetType.TopGainers => "Show best performing positions",
        WidgetType.TopLosers => "Show worst performing positions",
        WidgetType.DailyPnL => "Today's profit and loss",
        WidgetType.PerformanceChart => "Historical performance chart",
        WidgetType.MarketOverview => "Major market indices overview",
        WidgetType.SectorPerformance => "Sector performance comparison",
        WidgetType.MarketNews => "Latest market news and updates",
        WidgetType.EconomicCalendar => "Upcoming economic events",
        WidgetType.RiskMetrics => "Comprehensive risk metrics display",
        WidgetType.ValueAtRisk => "Value at Risk calculation and history",
        WidgetType.CorrelationMatrix => "Asset correlation heatmap",
        WidgetType.VolatilityChart => "Portfolio volatility over time",
        WidgetType.AssetAllocation => "Asset allocation pie chart",
        WidgetType.DiversificationScore => "Portfolio diversification metrics",
        WidgetType.RebalancingNeeds => "Recommended rebalancing actions",
        WidgetType.TaxLossHarvesting => "Tax loss harvesting opportunities",
        WidgetType.QuickStats => "Quick portfolio statistics",
        WidgetType.ReturnsCalendar => "Calendar heatmap of returns",
        WidgetType.Dividends => "Dividend income tracking",
        WidgetType.Transactions => "Recent transactions list",
        WidgetType.CustomChart => "Custom chart builder",
        WidgetType.Notes => "Personal notes widget",
        WidgetType.Calculator => "Financial calculator",
        WidgetType.Alerts => "Price and portfolio alerts",
        _ => "Widget"
    };

    private static int GetDefaultWidth(WidgetType type) => type switch
    {
        WidgetType.QuickStats or WidgetType.DailyPnL or WidgetType.PortfolioValue => 3,
        WidgetType.PositionCard or WidgetType.Notes or WidgetType.Calculator => 4,
        WidgetType.TopGainers or WidgetType.TopLosers or WidgetType.Dividends => 4,
        WidgetType.PortfolioAllocation or WidgetType.AssetAllocation => 6,
        WidgetType.PerformanceChart or WidgetType.VolatilityChart => 8,
        WidgetType.PositionList or WidgetType.Transactions => 12,
        _ => 6
    };

    private static int GetDefaultHeight(WidgetType type) => type switch
    {
        WidgetType.QuickStats or WidgetType.DailyPnL => 1,
        WidgetType.PortfolioValue or WidgetType.PortfolioRiskGauge => 2,
        WidgetType.TopGainers or WidgetType.TopLosers => 3,
        WidgetType.PortfolioAllocation or WidgetType.AssetAllocation => 3,
        WidgetType.PerformanceChart or WidgetType.VolatilityChart => 3,
        WidgetType.PositionList or WidgetType.Transactions => 4,
        _ => 2
    };
}

public sealed record WidgetTypeInfo(
    string Type,
    string Category,
    string Description,
    int DefaultWidth,
    int DefaultHeight
);
