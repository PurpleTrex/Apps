using System;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Logging;
using RiskPortfolio.Core.Interfaces;
using RiskPortfolio.Core.Models;

namespace RiskPortfolio.Infrastructure.Caching;

public class PortfolioRiskCache : IRiskMetricCache
{
    private static readonly JsonSerializerOptions SerializerOptions = new(JsonSerializerDefaults.Web);

    private readonly IMemoryCache _memoryCache;
    private readonly ILogger<PortfolioRiskCache> _logger;
    private readonly Lazy<Task<StackExchange.Redis.IDatabase?>> _redis;

    public PortfolioRiskCache(
        IMemoryCache memoryCache,
        IRedisConnectionFactory redisConnectionFactory,
        ILogger<PortfolioRiskCache> logger)
    {
        _memoryCache = memoryCache;
        _logger = logger;
        _redis = new Lazy<Task<StackExchange.Redis.IDatabase?>>(async () =>
        {
            var connection = await redisConnectionFactory.GetConnectionAsync();
            return connection?.GetDatabase();
        });
    }

    public async Task<RiskAssessmentResult?> GetAsync(Guid portfolioId, CancellationToken cancellationToken = default)
    {
        if (_memoryCache.TryGetValue<RiskAssessmentResult>(portfolioId, out var result))
        {
            return result;
        }

        var redisDb = await _redis.Value;
        if (redisDb == null)
        {
            return null;
        }

        var payload = await redisDb.StringGetAsync(GetRedisKey(portfolioId));
        if (!payload.HasValue)
        {
            return null;
        }

        try
        {
            result = JsonSerializer.Deserialize<RiskAssessmentResult>(payload!, SerializerOptions);
            if (result != null)
            {
                _memoryCache.Set(portfolioId, result, TimeSpan.FromMinutes(5));
            }

            return result;
        }
        catch (JsonException ex)
        {
            _logger.LogWarning(ex, "Failed to deserialize cached risk result for portfolio {PortfolioId}", portfolioId);
            await redisDb.KeyDeleteAsync(GetRedisKey(portfolioId));
            return null;
        }
    }

    public async Task SetAsync(RiskAssessmentResult result, TimeSpan timeToLive, CancellationToken cancellationToken = default)
    {
        _memoryCache.Set(result.PortfolioId, result, timeToLive);

        var redisDb = await _redis.Value;
        if (redisDb == null)
        {
            return;
        }

        var payload = JsonSerializer.Serialize(result, SerializerOptions);
        await redisDb.StringSetAsync(GetRedisKey(result.PortfolioId), payload, timeToLive);
    }

    public async Task EvictAsync(Guid portfolioId, CancellationToken cancellationToken = default)
    {
        _memoryCache.Remove(portfolioId);

        var redisDb = await _redis.Value;
        if (redisDb == null)
        {
            return;
        }

        await redisDb.KeyDeleteAsync(GetRedisKey(portfolioId));
    }

    private static string GetRedisKey(Guid portfolioId) => $"risk-portfolio:{portfolioId}";
}
