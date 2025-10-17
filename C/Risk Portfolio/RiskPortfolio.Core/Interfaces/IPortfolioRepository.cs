using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Core.Interfaces;

public interface IPortfolioRepository
{
    Task<Portfolio?> GetAsync(Guid id, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<Portfolio>> GetAllAsync(CancellationToken cancellationToken = default);
    Task AddAsync(Portfolio portfolio, CancellationToken cancellationToken = default);
    Task UpdateAsync(Portfolio portfolio, CancellationToken cancellationToken = default);
    Task RemoveAsync(Guid id, CancellationToken cancellationToken = default);
    Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default);
}
