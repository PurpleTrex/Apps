using System;
using System.Threading;
using System.Threading.Tasks;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Core.Interfaces;

public interface IRiskMetricCache
{
    Task<RiskAssessmentResult?> GetAsync(Guid portfolioId, CancellationToken cancellationToken = default);
    Task SetAsync(RiskAssessmentResult result, TimeSpan timeToLive, CancellationToken cancellationToken = default);
    Task EvictAsync(Guid portfolioId, CancellationToken cancellationToken = default);
}
