using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Polly;
using Polly.Registry;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Data;

namespace RiskPortfolio.Infrastructure.Repositories;

public class PortfolioRepository : IPortfolioRepository
{
    private readonly RiskPortfolioDbContext _dbContext;
    private readonly IAsyncPolicy _retryPolicy;
    private readonly ILogger<PortfolioRepository> _logger;

    public PortfolioRepository(
        RiskPortfolioDbContext dbContext,
        IPolicyRegistry<string> policyRegistry,
        ILogger<PortfolioRepository> logger)
    {
        _dbContext = dbContext;
        _logger = logger;
        _retryPolicy = policyRegistry.Get<IAsyncPolicy>(PolicyNames.SqlRetryPolicy);
    }

    public Task<Portfolio?> GetAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return _retryPolicy.ExecuteAsync(async ct =>
                await _dbContext.Portfolios
                    .Include(p => p.Positions)
                    .SingleOrDefaultAsync(p => p.Id == id, ct),
            cancellationToken);
    }

    public Task<IReadOnlyList<Portfolio>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return _retryPolicy.ExecuteAsync(async ct =>
            (IReadOnlyList<Portfolio>)await _dbContext.Portfolios
                .Include(p => p.Positions)
                .OrderBy(p => p.Name)
                .AsNoTracking()
                .ToListAsync(ct),
            cancellationToken);
    }

    public Task AddAsync(Portfolio portfolio, CancellationToken cancellationToken = default)
    {
        return _retryPolicy.ExecuteAsync(async ct =>
        {
            await _dbContext.Portfolios.AddAsync(portfolio, ct);
            await _dbContext.SaveChangesAsync(ct);
            _logger.LogInformation("Persisted new portfolio {PortfolioId} ({Name})", portfolio.Id, portfolio.Name);
        }, cancellationToken);
    }

    public Task UpdateAsync(Portfolio portfolio, CancellationToken cancellationToken = default)
    {
        return _retryPolicy.ExecuteAsync(async ct =>
        {
            _dbContext.Portfolios.Update(portfolio);
            await _dbContext.SaveChangesAsync(ct);
            _logger.LogInformation("Updated portfolio {PortfolioId}", portfolio.Id);
        }, cancellationToken);
    }

    public Task RemoveAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return _retryPolicy.ExecuteAsync(async ct =>
        {
            var entity = await _dbContext.Portfolios.FindAsync([id], ct);
            if (entity is null)
            {
                return;
            }

            _dbContext.Portfolios.Remove(entity);
            await _dbContext.SaveChangesAsync(ct);
            _logger.LogInformation("Removed portfolio {PortfolioId}", id);
        }, cancellationToken);
    }

    public Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return _retryPolicy.ExecuteAsync(ct => _dbContext.Portfolios.AnyAsync(p => p.Id == id, ct), cancellationToken);
    }
}

public static class PolicyNames
{
    public const string SqlRetryPolicy = "SqlRetryPolicy";
}
