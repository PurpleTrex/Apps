using Microsoft.AspNetCore.Mvc;
using RiskPortfolio.Api.Models.Requests;
using RiskPortfolio.Api.Models.Responses;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;

namespace RiskPortfolio.Api.Controllers;

[ApiController]
[Route("api/portfolios/{portfolioId}/transactions")]
public class TransactionsController : ControllerBase
{
    private readonly ITransactionRepository _transactionRepository;
    private readonly IPortfolioRepository _portfolioRepository;
    private readonly ILogger<TransactionsController> _logger;

    public TransactionsController(
        ITransactionRepository transactionRepository,
        IPortfolioRepository portfolioRepository,
        ILogger<TransactionsController> logger)
    {
        _transactionRepository = transactionRepository;
        _portfolioRepository = portfolioRepository;
        _logger = logger;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<TransactionResponse>>> GetTransactions(
        Guid portfolioId,
        CancellationToken cancellationToken)
    {
        var portfolio = await _portfolioRepository.GetAsync(portfolioId, cancellationToken);
        if (portfolio == null)
            return NotFound($"Portfolio with ID {portfolioId} not found.");

        var transactions = await _transactionRepository.GetByPortfolioAsync(portfolioId, cancellationToken);
        
        var response = transactions.Select(t => new TransactionResponse(
            t.Id,
            t.PortfolioId,
            t.Symbol,
            t.Type.ToString(),
            t.Quantity,
            t.Price,
            t.TotalAmount,
            t.Commission,
            t.TransactionDate,
            t.Notes,
            t.CreatedAt
        ));

        return Ok(response);
    }

    [HttpGet("{transactionId}", Name = "GetTransaction")]
    public async Task<ActionResult<TransactionResponse>> GetTransaction(
        Guid portfolioId,
        Guid transactionId,
        CancellationToken cancellationToken)
    {
        var transaction = await _transactionRepository.GetAsync(transactionId, cancellationToken);
        
        if (transaction == null)
            return NotFound($"Transaction with ID {transactionId} not found.");

        if (transaction.PortfolioId != portfolioId)
            return BadRequest("Transaction does not belong to the specified portfolio.");

        var response = new TransactionResponse(
            transaction.Id,
            transaction.PortfolioId,
            transaction.Symbol,
            transaction.Type.ToString(),
            transaction.Quantity,
            transaction.Price,
            transaction.TotalAmount,
            transaction.Commission,
            transaction.TransactionDate,
            transaction.Notes,
            transaction.CreatedAt
        );

        return Ok(response);
    }

    [HttpPost]
    public async Task<ActionResult<TransactionResponse>> CreateTransaction(
        Guid portfolioId,
        [FromBody] CreateTransactionRequest request,
        CancellationToken cancellationToken)
    {
        var portfolio = await _portfolioRepository.GetAsync(portfolioId, cancellationToken);
        if (portfolio == null)
            return NotFound($"Portfolio with ID {portfolioId} not found.");

        if (!Enum.TryParse<TransactionType>(request.Type, ignoreCase: true, out var transactionType))
            return BadRequest($"Invalid transaction type. Must be 'Buy' or 'Sell'.");

        var transaction = new Transaction(
            portfolioId,
            request.Symbol,
            transactionType,
            request.Quantity,
            request.Price,
            request.TransactionDate,
            request.Commission,
            request.Notes
        );

        await _transactionRepository.AddAsync(transaction, cancellationToken);

        _logger.LogInformation("Created transaction {TransactionId} for portfolio {PortfolioId}: {Type} {Quantity} {Symbol} @ {Price}",
            transaction.Id, portfolioId, transaction.Type, transaction.Quantity, transaction.Symbol, transaction.Price);

        var response = new TransactionResponse(
            transaction.Id,
            transaction.PortfolioId,
            transaction.Symbol,
            transaction.Type.ToString(),
            transaction.Quantity,
            transaction.Price,
            transaction.TotalAmount,
            transaction.Commission,
            transaction.TransactionDate,
            transaction.Notes,
            transaction.CreatedAt
        );

        return CreatedAtRoute("GetTransaction", new { portfolioId, transactionId = transaction.Id }, response);
    }

    [HttpPut("{transactionId}")]
    public async Task<ActionResult<TransactionResponse>> UpdateTransactionNotes(
        Guid portfolioId,
        Guid transactionId,
        [FromBody] UpdateTransactionNotesRequest request,
        CancellationToken cancellationToken)
    {
        var transaction = await _transactionRepository.GetAsync(transactionId, cancellationToken);
        
        if (transaction == null)
            return NotFound($"Transaction with ID {transactionId} not found.");

        if (transaction.PortfolioId != portfolioId)
            return BadRequest("Transaction does not belong to the specified portfolio.");

        transaction.UpdateNotes(request.Notes);
        await _transactionRepository.UpdateAsync(transaction, cancellationToken);

        _logger.LogInformation("Updated notes for transaction {TransactionId}", transactionId);

        var response = new TransactionResponse(
            transaction.Id,
            transaction.PortfolioId,
            transaction.Symbol,
            transaction.Type.ToString(),
            transaction.Quantity,
            transaction.Price,
            transaction.TotalAmount,
            transaction.Commission,
            transaction.TransactionDate,
            transaction.Notes,
            transaction.CreatedAt
        );

        return Ok(response);
    }

    [HttpDelete("{transactionId}")]
    public async Task<ActionResult> DeleteTransaction(
        Guid portfolioId,
        Guid transactionId,
        CancellationToken cancellationToken)
    {
        var transaction = await _transactionRepository.GetAsync(transactionId, cancellationToken);
        
        if (transaction == null)
            return NotFound($"Transaction with ID {transactionId} not found.");

        if (transaction.PortfolioId != portfolioId)
            return BadRequest("Transaction does not belong to the specified portfolio.");

        await _transactionRepository.DeleteAsync(transactionId, cancellationToken);

        _logger.LogInformation("Deleted transaction {TransactionId} from portfolio {PortfolioId}", transactionId, portfolioId);

        return NoContent();
    }
}
