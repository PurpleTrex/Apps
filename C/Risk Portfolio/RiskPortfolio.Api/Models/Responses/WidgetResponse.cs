namespace RiskPortfolio.Api.Models.Responses;

public sealed record WidgetResponse(
    Guid Id,
    string Title,
    string Type,
    string Configuration,
    int PositionX,
    int PositionY,
    int Width,
    int Height,
    bool IsVisible,
    int DisplayOrder,
    DateTime CreatedAt,
    DateTime UpdatedAt
);
