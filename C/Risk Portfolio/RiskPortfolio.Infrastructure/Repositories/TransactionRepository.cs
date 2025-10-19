using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Data;

namespace RiskPortfolio.Infrastructure.Repositories;

public class TransactionRepository : ITransactionRepository
{
    private readonly RiskPortfolioDbContext _context;
    private readonly ILogger<TransactionRepository> _logger;

    public TransactionRepository(
        RiskPortfolioDbContext context,
        ILogger<TransactionRepository> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<Transaction?> GetAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            return await _context.Transactions
                .Include(t => t.Portfolio)
                .FirstOrDefaultAsync(t => t.Id == id, cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving transaction {TransactionId}", id);
            throw;
        }
    }

    public async Task<IReadOnlyList<Transaction>> GetByPortfolioAsync(
        Guid portfolioId, 
        CancellationToken cancellationToken = default)
    {
        try
        {
            return await _context.Transactions
                .Where(t => t.PortfolioId == portfolioId)
                .OrderByDescending(t => t.TransactionDate)
                .ThenByDescending(t => t.CreatedAt)
                .ToListAsync(cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving transactions for portfolio {PortfolioId}", portfolioId);
            throw;
        }
    }

    public async Task<IReadOnlyList<Transaction>> GetBySymbolAsync(
        Guid portfolioId, 
        string symbol, 
        CancellationToken cancellationToken = default)
    {
        try
        {
            return await _context.Transactions
                .Where(t => t.PortfolioId == portfolioId && t.Symbol == symbol)
                .OrderByDescending(t => t.TransactionDate)
                .ThenByDescending(t => t.CreatedAt)
                .ToListAsync(cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving transactions for portfolio {PortfolioId} and symbol {Symbol}", 
                portfolioId, symbol);
            throw;
        }
    }

    public async Task<IReadOnlyList<Transaction>> GetByDateRangeAsync(
        Guid portfolioId, 
        DateTime startDate, 
        DateTime endDate, 
        CancellationToken cancellationToken = default)
    {
        try
        {
            return await _context.Transactions
                .Where(t => t.PortfolioId == portfolioId 
                    && t.TransactionDate >= startDate 
                    && t.TransactionDate <= endDate)
                .OrderByDescending(t => t.TransactionDate)
                .ThenByDescending(t => t.CreatedAt)
                .ToListAsync(cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving transactions for portfolio {PortfolioId} between {StartDate} and {EndDate}", 
                portfolioId, startDate, endDate);
            throw;
        }
    }

    public async Task AddAsync(Transaction transaction, CancellationToken cancellationToken = default)
    {
        try
        {
            await _context.Transactions.AddAsync(transaction, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);
            
            _logger.LogInformation("Added transaction {TransactionId} for portfolio {PortfolioId}: {Type} {Quantity} {Symbol}", 
                transaction.Id, transaction.PortfolioId, transaction.Type, transaction.Quantity, transaction.Symbol);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error adding transaction for portfolio {PortfolioId}", transaction.PortfolioId);
            throw;
        }
    }

    public async Task UpdateAsync(Transaction transaction, CancellationToken cancellationToken = default)
    {
        try
        {
            _context.Transactions.Update(transaction);
            await _context.SaveChangesAsync(cancellationToken);
            
            _logger.LogInformation("Updated transaction {TransactionId}", transaction.Id);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error updating transaction {TransactionId}", transaction.Id);
            throw;
        }
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            var transaction = await _context.Transactions.FindAsync(new object[] { id }, cancellationToken);
            if (transaction != null)
            {
                _context.Transactions.Remove(transaction);
                await _context.SaveChangesAsync(cancellationToken);
                
                _logger.LogInformation("Deleted transaction {TransactionId}", id);
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error deleting transaction {TransactionId}", id);
            throw;
        }
    }

    public async Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            return await _context.Transactions.AnyAsync(t => t.Id == id, cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error checking if transaction {TransactionId} exists", id);
            throw;
        }
    }
}
