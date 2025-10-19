namespace RiskPortfolio.Api.Models.Requests;

public sealed record CreateWidgetRequest(
    string Title,
    string Type,
    string Configuration,
    int PositionX,
    int PositionY,
    int Width,
    int Height,
    int DisplayOrder = 0
);

public sealed record UpdateWidgetRequest(
    string? Title = null,
    string? Configuration = null,
    int? PositionX = null,
    int? PositionY = null,
    int? Width = null,
    int? Height = null,
    bool? IsVisible = null,
    int? DisplayOrder = null
);

public sealed record UpdateWidgetPositionsRequest(
    List<WidgetPositionUpdate> Widgets
);

public sealed record WidgetPositionUpdate(
    Guid Id,
    int PositionX,
    int PositionY,
    int DisplayOrder
);
