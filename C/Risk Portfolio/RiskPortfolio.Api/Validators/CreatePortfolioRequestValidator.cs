using System.Linq;
using FluentValidation;
using RiskPortfolio.Api.Models.Requests;

namespace RiskPortfolio.Api.Validators;

public sealed class CreatePortfolioRequestValidator : AbstractValidator<CreatePortfolioRequest>
{
    public CreatePortfolioRequestValidator()
    {
        RuleFor(r => r.Name)
            .NotEmpty()
            .MaximumLength(200);

        RuleFor(r => r.Owner)
            .NotEmpty()
            .MaximumLength(200);

        RuleFor(r => r.BaseCurrency)
            .NotEmpty()
            .Length(3)
            .Matches("^[A-Z]{3}$").WithMessage("Base currency must be an ISO 4217 code in upper case.");

        RuleForEach(r => r.Positions)
            .SetValidator(new CreatePortfolioPositionRequestValidator());

        RuleFor(r => r)
            .Must(r => r.Positions
                .Select(p => p.Symbol.ToUpperInvariant())
                .Distinct()
                .Count() == r.Positions.Count)
            .When(r => r.Positions.Any())
            .WithMessage("Duplicate symbols are not allowed within the same portfolio.");
    }

    private sealed class CreatePortfolioPositionRequestValidator : AbstractValidator<CreatePortfolioPositionRequest>
    {
        public CreatePortfolioPositionRequestValidator()
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
}
