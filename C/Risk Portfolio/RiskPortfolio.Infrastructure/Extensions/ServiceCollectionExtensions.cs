using System;
using FluentMigrator.Runner;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using Polly;
using Polly.Registry;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Infrastructure.Caching;
using RiskPortfolio.Infrastructure.Data;
using RiskPortfolio.Infrastructure.Hosting;
using RiskPortfolio.Infrastructure.Jobs;
using RiskPortfolio.Infrastructure.Migrations;
using RiskPortfolio.Infrastructure.Options;
using RiskPortfolio.Infrastructure.Repositories;
using RiskPortfolio.Infrastructure.Services;

namespace RiskPortfolio.Infrastructure.Extensions;

public static class ServiceCollectionExtensions
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddMemoryCache();

        services.Configure<DatabaseOptions>(configuration.GetSection(DatabaseOptions.SectionName));
        services.Configure<CachingOptions>(configuration.GetSection(CachingOptions.SectionName));
        services.Configure<RiskAssessmentOptions>(configuration.GetSection(RiskAssessmentOptions.SectionName));

        services.AddDbContext<RiskPortfolioDbContext>((provider, options) =>
        {
            var dbOptions = provider.GetRequiredService<IOptions<DatabaseOptions>>().Value;
            options.UseSqlServer(dbOptions.ConnectionString, sqlOptions =>
            {
                sqlOptions.EnableRetryOnFailure();
            });
        });

        services.AddScoped<IPortfolioRepository, PortfolioRepository>();
        services.AddScoped<IRiskAssessmentService, RiskAssessmentService>();
        services.AddSingleton<IRiskMetricCache, PortfolioRiskCache>();
        services.AddSingleton<IRedisConnectionFactory, RedisConnectionFactory>();

        services.AddTransient<RiskRecalculationJob>();

        services.AddSingleton<IPolicyRegistry<string>>(provider =>
        {
            var registry = new PolicyRegistry
            {
                { PolicyNames.SqlRetryPolicy, Policy.Handle<Exception>().WaitAndRetryAsync(3, attempt => TimeSpan.FromMilliseconds(200 * attempt)) }
            };
            return registry;
        });

        var databaseOptions = configuration.GetSection(DatabaseOptions.SectionName).Get<DatabaseOptions>() ?? new DatabaseOptions();

        services.AddFluentMigratorCore()
            .ConfigureRunner(rb =>
            {
                rb.AddSqlServer()
                  .WithGlobalConnectionString(databaseOptions.ConnectionString)
                  .ScanIn(typeof(Migration_20241017_CreatePortfolioSchema).Assembly).For.Migrations();
            })
            .AddLogging(lb => lb.AddFluentMigratorConsole());

        services.AddHostedService<MigrationHostedService>();

        return services;
    }
}
