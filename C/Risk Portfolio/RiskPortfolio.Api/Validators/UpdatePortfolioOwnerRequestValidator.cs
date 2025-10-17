using FluentValidation;
using RiskPortfolio.Api.Models.Requests;

namespace RiskPortfolio.Api.Validators;

public sealed class UpdatePortfolioOwnerRequestValidator : AbstractValidator<UpdatePortfolioOwnerRequest>
{
    public UpdatePortfolioOwnerRequestValidator()
    {
        RuleFor(r => r.Owner)
            .NotEmpty()
            .MaximumLength(200);
    }
}
