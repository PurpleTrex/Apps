using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using FluentValidation;
using FluentValidation.Results;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using RiskPortfolio.Api.Mappers;
using RiskPortfolio.Api.Models.Requests;
using RiskPortfolio.Api.Models.Responses;
using RiskPortfolio.Core.Entities;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public sealed class PortfoliosController : ControllerBase
{
    private readonly IPortfolioRepository _repository;
    private readonly IRiskAssessmentService _riskAssessmentService;
    private readonly IRiskMetricCache _riskCache;
    private readonly IValidator<CreatePortfolioRequest> _createValidator;
    private readonly IValidator<AddOrUpdatePositionRequest> _positionValidator;
    private readonly IValidator<UpdatePortfolioOwnerRequest> _ownerValidator;
    private readonly ILogger<PortfoliosController> _logger;

    public PortfoliosController(
        IPortfolioRepository repository,
        IRiskAssessmentService riskAssessmentService,
        IRiskMetricCache riskCache,
        IValidator<CreatePortfolioRequest> createValidator,
        IValidator<AddOrUpdatePositionRequest> positionValidator,
        IValidator<UpdatePortfolioOwnerRequest> ownerValidator,
        ILogger<PortfoliosController> logger)
    {
        _repository = repository;
        _riskAssessmentService = riskAssessmentService;
        _riskCache = riskCache;
        _createValidator = createValidator;
        _positionValidator = positionValidator;
        _ownerValidator = ownerValidator;
        _logger = logger;
    }

    [HttpGet]
    [ProducesResponseType(typeof(IEnumerable<PortfolioResponse>), 200)]
    public async Task<ActionResult<IEnumerable<PortfolioResponse>>> GetAllAsync(CancellationToken cancellationToken)
    {
        var portfolios = await _repository.GetAllAsync(cancellationToken);
        var responses = new List<PortfolioResponse>(portfolios.Count);

        foreach (var portfolio in portfolios)
        {
            var risk = await _riskCache.GetAsync(portfolio.Id, cancellationToken);
            responses.Add(portfolio.ToResponse(risk));
        }

        return Ok(responses);
    }

    [HttpGet("{id:guid}")]
    [ProducesResponseType(typeof(PortfolioResponse), 200)]
    [ProducesResponseType(404)]
    public async Task<ActionResult<PortfolioResponse>> GetByIdAsync(Guid id, CancellationToken cancellationToken)
    {
        var portfolio = await _repository.GetAsync(id, cancellationToken);
        if (portfolio is null)
        {
            return NotFound();
        }

        var risk = await _riskCache.GetAsync(id, cancellationToken);
        return Ok(portfolio.ToResponse(risk));
    }

    [HttpPost]
    [ProducesResponseType(typeof(PortfolioResponse), 201)]
    [ProducesResponseType(400)]
    public async Task<ActionResult<PortfolioResponse>> CreateAsync(
        [FromBody] CreatePortfolioRequest request,
        CancellationToken cancellationToken)
    {
        var validationResult = await _createValidator.ValidateAsync(request, cancellationToken);
        if (!validationResult.IsValid)
        {
            return ValidationProblem(CreateProblemDetails(validationResult));
        }

        var portfolio = new Portfolio(request.Name, request.Owner, request.BaseCurrency);
        if (request.Positions is { Count: > 0 })
        {
            foreach (var position in request.Positions)
            {
                portfolio.AddOrUpdatePosition(
                    position.Symbol,
                    position.Quantity,
                    position.AveragePrice,
                    position.CurrentPrice,
                    position.Volatility);
            }
        }

    var risk = await _riskAssessmentService.CalculateAsync(portfolio, cancellationToken);
    await _repository.AddAsync(portfolio, cancellationToken);

        _logger.LogInformation("Created portfolio {PortfolioId}", portfolio.Id);

        return CreatedAtAction(nameof(GetByIdAsync), new { id = portfolio.Id }, portfolio.ToResponse(risk));
    }

    [HttpPut("{id:guid}/owner")]
    [ProducesResponseType(typeof(PortfolioResponse), 200)]
    [ProducesResponseType(400)]
    [ProducesResponseType(404)]
    public async Task<ActionResult<PortfolioResponse>> UpdateOwnerAsync(
        Guid id,
        [FromBody] UpdatePortfolioOwnerRequest request,
        CancellationToken cancellationToken)
    {
        var validationResult = await _ownerValidator.ValidateAsync(request, cancellationToken);
        if (!validationResult.IsValid)
        {
            return ValidationProblem(CreateProblemDetails(validationResult));
        }

        var portfolio = await _repository.GetAsync(id, cancellationToken);
        if (portfolio is null)
        {
            return NotFound();
        }

        portfolio.ChangeOwner(request.Owner);
        await _repository.UpdateAsync(portfolio, cancellationToken);
        var risk = await _riskCache.GetAsync(id, cancellationToken);

        _logger.LogInformation("Updated owner for portfolio {PortfolioId}", id);

        return Ok(portfolio.ToResponse(risk));
    }

    [HttpPut("{id:guid}/positions")]
    [ProducesResponseType(typeof(PortfolioResponse), 200)]
    [ProducesResponseType(400)]
    [ProducesResponseType(404)]
    public async Task<ActionResult<PortfolioResponse>> AddOrUpdatePositionAsync(
        Guid id,
        [FromBody] AddOrUpdatePositionRequest request,
        CancellationToken cancellationToken)
    {
        var validationResult = await _positionValidator.ValidateAsync(request, cancellationToken);
        if (!validationResult.IsValid)
        {
            return ValidationProblem(CreateProblemDetails(validationResult));
        }

        var portfolio = await _repository.GetAsync(id, cancellationToken);
        if (portfolio is null)
        {
            return NotFound();
        }

        portfolio.AddOrUpdatePosition(
            request.Symbol,
            request.Quantity,
            request.AveragePrice,
            request.CurrentPrice,
            request.Volatility);

    var risk = await _riskAssessmentService.CalculateAsync(portfolio, cancellationToken);
    await _repository.UpdateAsync(portfolio, cancellationToken);

        _logger.LogInformation("Upserted position {Symbol} for portfolio {PortfolioId}", request.Symbol, id);

        return Ok(portfolio.ToResponse(risk));
    }

    [HttpDelete("{id:guid}/positions/{positionId:guid}")]
    [ProducesResponseType(typeof(PortfolioResponse), 200)]
    [ProducesResponseType(404)]
    public async Task<ActionResult<PortfolioResponse>> RemovePositionAsync(Guid id, Guid positionId, CancellationToken cancellationToken)
    {
        var portfolio = await _repository.GetAsync(id, cancellationToken);
        if (portfolio is null)
        {
            return NotFound();
        }

        try
        {
            portfolio.RemovePosition(positionId);
        }
        catch (InvalidOperationException)
        {
            return NotFound();
        }

    var risk = await _riskAssessmentService.CalculateAsync(portfolio, cancellationToken);
    await _repository.UpdateAsync(portfolio, cancellationToken);

        _logger.LogInformation("Removed position {PositionId} from portfolio {PortfolioId}", positionId, id);

        return Ok(portfolio.ToResponse(risk));
    }

    [HttpDelete("{id:guid}")]
    [ProducesResponseType(204)]
    [ProducesResponseType(404)]
    public async Task<IActionResult> DeleteAsync(Guid id, CancellationToken cancellationToken)
    {
        var exists = await _repository.ExistsAsync(id, cancellationToken);
        if (!exists)
        {
            return NotFound();
        }

        await _repository.RemoveAsync(id, cancellationToken);
        await _riskCache.EvictAsync(id, cancellationToken);

        _logger.LogInformation("Deleted portfolio {PortfolioId}", id);

        return NoContent();
    }

    [HttpPost("{id:guid}/recalculate-risk")]
    [ProducesResponseType(typeof(RiskAssessmentResponse), 200)]
    [ProducesResponseType(404)]
    public async Task<ActionResult<RiskAssessmentResponse>> RecalculateRiskAsync(Guid id, CancellationToken cancellationToken)
    {
        try
        {
            var result = await _riskAssessmentService.RecalculateAsync(id, cancellationToken);
            return Ok(result.ToResponse());
        }
        catch (InvalidOperationException)
        {
            return NotFound();
        }
    }

    private ValidationProblemDetails CreateProblemDetails(ValidationResult validationResult)
    {
        var problemDetails = new ValidationProblemDetails
        {
            Title = "One or more validation errors occurred.",
            Status = StatusCodes.Status400BadRequest
        };

        foreach (var failure in validationResult.Errors)
        {
            if (problemDetails.Errors.TryGetValue(failure.PropertyName, out var existing))
            {
                problemDetails.Errors[failure.PropertyName] = existing.Concat(new[] { failure.ErrorMessage }).ToArray();
            }
            else
            {
                problemDetails.Errors.Add(failure.PropertyName, new[] { failure.ErrorMessage });
            }
        }

        return problemDetails;
    }
}
