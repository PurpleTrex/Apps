namespace RiskPortfolio.Core.Entities;

/// <summary>
/// Represents a customizable widget on the user's dashboard
/// </summary>
public class DashboardWidget
{
    public Guid Id { get; private set; }
    public string UserId { get; private set; } = string.Empty;
    public string Title { get; private set; } = string.Empty;
    public WidgetType Type { get; private set; }
    public string Configuration { get; private set; } = "{}"; // JSON configuration
    public int PositionX { get; private set; }
    public int PositionY { get; private set; }
    public int Width { get; private set; }
    public int Height { get; private set; }
    public bool IsVisible { get; private set; }
    public int DisplayOrder { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime UpdatedAt { get; private set; }

    private DashboardWidget() { } // EF Core

    public DashboardWidget(
        string userId,
        string title,
        WidgetType type,
        string configuration,
        int positionX,
        int positionY,
        int width,
        int height,
        int displayOrder = 0)
    {
        if (string.IsNullOrWhiteSpace(userId))
            throw new ArgumentException("User ID cannot be empty", nameof(userId));

        if (string.IsNullOrWhiteSpace(title))
            throw new ArgumentException("Title cannot be empty", nameof(title));

        if (width <= 0 || width > 12)
            throw new ArgumentException("Width must be between 1 and 12", nameof(width));

        if (height <= 0)
            throw new ArgumentException("Height must be positive", nameof(height));

        Id = Guid.NewGuid();
        UserId = userId;
        Title = title;
        Type = type;
        Configuration = configuration ?? "{}";
        PositionX = positionX;
        PositionY = positionY;
        Width = width;
        Height = height;
        IsVisible = true;
        DisplayOrder = displayOrder;
        CreatedAt = DateTime.UtcNow;
        UpdatedAt = DateTime.UtcNow;
    }

    public void UpdatePosition(int x, int y)
    {
        PositionX = x;
        PositionY = y;
        UpdatedAt = DateTime.UtcNow;
    }

    public void UpdateSize(int width, int height)
    {
        if (width <= 0 || width > 12)
            throw new ArgumentException("Width must be between 1 and 12", nameof(width));

        if (height <= 0)
            throw new ArgumentException("Height must be positive", nameof(height));

        Width = width;
        Height = height;
        UpdatedAt = DateTime.UtcNow;
    }

    public void UpdateConfiguration(string configuration)
    {
        Configuration = configuration ?? "{}";
        UpdatedAt = DateTime.UtcNow;
    }

    public void UpdateTitle(string title)
    {
        if (string.IsNullOrWhiteSpace(title))
            throw new ArgumentException("Title cannot be empty", nameof(title));

        Title = title;
        UpdatedAt = DateTime.UtcNow;
    }

    public void SetVisibility(bool isVisible)
    {
        IsVisible = isVisible;
        UpdatedAt = DateTime.UtcNow;
    }

    public void UpdateDisplayOrder(int order)
    {
        DisplayOrder = order;
        UpdatedAt = DateTime.UtcNow;
    }
}

public enum WidgetType
{
    // Portfolio Widgets
    PortfolioValue = 1,
    PortfolioAllocation = 2,
    PortfolioPerformance = 3,
    PortfolioRiskGauge = 4,

    // Position Widgets
    PositionList = 10,
    PositionCard = 11,
    WatchList = 12,

    // Performance Widgets
    TopGainers = 20,
    TopLosers = 21,
    DailyPnL = 22,
    PerformanceChart = 23,

    // Market Widgets
    MarketOverview = 30,
    SectorPerformance = 31,
    MarketNews = 32,
    EconomicCalendar = 33,

    // Risk Widgets
    RiskMetrics = 40,
    ValueAtRisk = 41,
    CorrelationMatrix = 42,
    VolatilityChart = 43,

    // Analytics Widgets
    AssetAllocation = 50,
    DiversificationScore = 51,
    RebalancingNeeds = 52,
    TaxLossHarvesting = 53,

    // Statistics Widgets
    QuickStats = 60,
    ReturnsCalendar = 61,
    Dividends = 62,
    Transactions = 63,

    // Custom Widgets
    CustomChart = 70,
    Notes = 71,
    Calculator = 72,
    Alerts = 73
}
