using RiskPortfolio.Core.Entities;

namespace RiskPortfolio.Core.Interfaces;

public interface IWidgetRepository
{
    Task<DashboardWidget?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<DashboardWidget>> GetByUserIdAsync(string userId, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<DashboardWidget>> GetVisibleByUserIdAsync(string userId, CancellationToken cancellationToken = default);
    Task AddAsync(DashboardWidget widget, CancellationToken cancellationToken = default);
    Task UpdateAsync(DashboardWidget widget, CancellationToken cancellationToken = default);
    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);
    Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default);
}
