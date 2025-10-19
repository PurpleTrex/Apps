using Microsoft.EntityFrameworkCore;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Data;

namespace RiskPortfolio.Infrastructure.Repositories;

public class WidgetRepository : IWidgetRepository
{
    private readonly RiskPortfolioDbContext _context;

    public WidgetRepository(RiskPortfolioDbContext context)
    {
        _context = context;
    }

    public async Task<DashboardWidget?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return await _context.DashboardWidgets
            .FirstOrDefaultAsync(w => w.Id == id, cancellationToken);
    }

    public async Task<IReadOnlyList<DashboardWidget>> GetByUserIdAsync(string userId, CancellationToken cancellationToken = default)
    {
        return await _context.DashboardWidgets
            .Where(w => w.UserId == userId)
            .OrderBy(w => w.DisplayOrder)
            .ToListAsync(cancellationToken);
    }

    public async Task<IReadOnlyList<DashboardWidget>> GetVisibleByUserIdAsync(string userId, CancellationToken cancellationToken = default)
    {
        return await _context.DashboardWidgets
            .Where(w => w.UserId == userId && w.IsVisible)
            .OrderBy(w => w.DisplayOrder)
            .ToListAsync(cancellationToken);
    }

    public async Task AddAsync(DashboardWidget widget, CancellationToken cancellationToken = default)
    {
        await _context.DashboardWidgets.AddAsync(widget, cancellationToken);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task UpdateAsync(DashboardWidget widget, CancellationToken cancellationToken = default)
    {
        _context.DashboardWidgets.Update(widget);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var widget = await GetByIdAsync(id, cancellationToken);
        if (widget != null)
        {
            _context.DashboardWidgets.Remove(widget);
            await _context.SaveChangesAsync(cancellationToken);
        }
    }

    public async Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return await _context.DashboardWidgets
            .AnyAsync(w => w.Id == id, cancellationToken);
    }
}
