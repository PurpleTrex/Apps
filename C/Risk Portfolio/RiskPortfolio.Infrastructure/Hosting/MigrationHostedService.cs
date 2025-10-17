using System.Threading;
using System.Threading.Tasks;
using FluentMigrator.Runner;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace RiskPortfolio.Infrastructure.Hosting;

public class MigrationHostedService : IHostedService
{
    private readonly IMigrationRunner _migrationRunner;
    private readonly ILogger<MigrationHostedService> _logger;

    public MigrationHostedService(IMigrationRunner migrationRunner, ILogger<MigrationHostedService> logger)
    {
        _migrationRunner = migrationRunner;
        _logger = logger;
    }

    public Task StartAsync(CancellationToken cancellationToken)
    {
        _logger.LogInformation("Applying database migrations...");
        _migrationRunner.MigrateUp();
        _logger.LogInformation("Database migrations completed");
        return Task.CompletedTask;
    }

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;
}
