using FluentValidation;
using Hangfire;
using Hangfire.SqlServer;
using Microsoft.AspNetCore.Mvc;
using Microsoft.OpenApi.Models;
using Prometheus;
using RiskPortfolio.Api.Models.Requests;
using RiskPortfolio.Api.Validators;
using RiskPortfolio.Infrastructure.Extensions;
using RiskPortfolio.Infrastructure.Jobs;
using RiskPortfolio.Infrastructure.Options;
using Serilog;
using Serilog.Events;
using Serilog.Sinks.MSSqlServer;

var builder = WebApplication.CreateBuilder(args);

ConfigureSerilog(builder);

builder.Services.AddControllers(options =>
{
    options.RespectBrowserAcceptHeader = true;
}).ConfigureApiBehaviorOptions(options =>
{
    options.InvalidModelStateResponseFactory = context =>
        new BadRequestObjectResult(context.ModelState);
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Risk Portfolio API",
        Version = "v1",
        Description = "API for managing investment portfolios and assessing risk."
    });
});

builder.Services.AddInfrastructure(builder.Configuration);

builder.Services.AddScoped<IValidator<CreatePortfolioRequest>, CreatePortfolioRequestValidator>();
builder.Services.AddScoped<IValidator<AddOrUpdatePositionRequest>, AddOrUpdatePositionRequestValidator>();
builder.Services.AddScoped<IValidator<UpdatePortfolioOwnerRequest>, UpdatePortfolioOwnerRequestValidator>();

builder.Services.AddHangfire((serviceProvider, configuration) =>
{
    var dbOptions = serviceProvider.GetRequiredService<Microsoft.Extensions.Options.IOptions<DatabaseOptions>>().Value;
    configuration
        .SetDataCompatibilityLevel(CompatibilityLevel.Version_180)
        .UseSimpleAssemblyNameTypeSerializer()
        .UseRecommendedSerializerSettings()
        .UseSqlServerStorage(dbOptions.HangfireConnectionString, new SqlServerStorageOptions
        {
            SchemaName = "hangfire",
            CommandBatchMaxTimeout = TimeSpan.FromMinutes(5),
            SlidingInvisibilityTimeout = TimeSpan.FromMinutes(5),
            QueuePollInterval = TimeSpan.FromSeconds(15),
            UseRecommendedIsolationLevel = true,
            DisableGlobalLocks = false
        });
});

builder.Services.AddHangfireServer();

builder.Services.AddHealthChecks();

var app = builder.Build();

app.UseSerilogRequestLogging();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseRouting();

app.UseAuthorization();

app.MapControllers();

app.MapHangfireDashboard("/hangfire");

app.MapHealthChecks("/health");

app.MapGet("/metrics", async context =>
{
    context.Response.ContentType = "text/plain; version=0.0.4";
    await Metrics.DefaultRegistry.CollectAndExportAsTextAsync(context.Response.Body, context.RequestAborted);
});

using (var scope = app.Services.CreateScope())
{
    RecurringJob.AddOrUpdate<RiskRecalculationJob>(
        "risk-portfolio-hourly",
        job => job.ExecuteAsync(CancellationToken.None),
        Cron.Hourly);

    BackgroundJob.Enqueue<RiskRecalculationJob>(job => job.ExecuteAsync(CancellationToken.None));
}

app.Run();

static void ConfigureSerilog(WebApplicationBuilder builder)
{
    var databaseOptions = builder.Configuration.GetSection(DatabaseOptions.SectionName).Get<DatabaseOptions>() ?? new DatabaseOptions();

    var loggerConfiguration = new LoggerConfiguration()
        .Enrich.FromLogContext()
        .MinimumLevel.Override("Microsoft", LogEventLevel.Information)
        .MinimumLevel.Override("Hangfire", LogEventLevel.Warning)
        .MinimumLevel.Information()
        .WriteTo.Console()
        .WriteTo.File(Path.Combine("Logs", "risk-portfolio-.log"), rollingInterval: RollingInterval.Day);

    if (!string.IsNullOrWhiteSpace(databaseOptions.LoggingConnectionString))
    {
        loggerConfiguration.WriteTo.MSSqlServer(
            connectionString: databaseOptions.LoggingConnectionString,
            sinkOptions: new MSSqlServerSinkOptions
            {
                AutoCreateSqlTable = true,
                TableName = "Logs"
            });
    }

    Log.Logger = loggerConfiguration.CreateLogger();
    builder.Host.UseSerilog(Log.Logger, dispose: true);
}
