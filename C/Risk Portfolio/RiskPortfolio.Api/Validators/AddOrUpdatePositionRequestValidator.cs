using FluentValidation;
using RiskPortfolio.Api.Models.Requests;

namespace RiskPortfolio.Api.Validators;

public sealed class AddOrUpdatePositionRequestValidator : AbstractValidator<AddOrUpdatePositionRequest>
{
    public AddOrUpdatePositionRequestValidator()
    {
        RuleFor(p => p.Symbol)
            .NotEmpty()
            .MaximumLength(20);

        RuleFor(p => p.Quantity)
            .GreaterThan(0);

        RuleFor(p => p.AveragePrice)
            .GreaterThan(0);

        RuleFor(p => p.CurrentPrice)
            .GreaterThan(0);

        RuleFor(p => p.Volatility)
            .InclusiveBetween(0, 1);
    }
}
