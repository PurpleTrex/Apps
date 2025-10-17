using System;
using System.Threading;
using System.Threading.Tasks;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Core.Interfaces;

public interface IRiskAssessmentService
{
    Task<RiskAssessmentResult> RecalculateAsync(Guid portfolioId, CancellationToken cancellationToken = default);
    Task<RiskAssessmentResult> CalculateAsync(Portfolio portfolio, CancellationToken cancellationToken = default);
}
