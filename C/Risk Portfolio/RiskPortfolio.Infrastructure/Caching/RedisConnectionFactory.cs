using System;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using StackExchange.Redis;
using RiskPortfolio.Infrastructure.Options;

namespace RiskPortfolio.Infrastructure.Caching;

public interface IRedisConnectionFactory
{
    ValueTask<IConnectionMultiplexer?> GetConnectionAsync();
}

public class RedisConnectionFactory : IRedisConnectionFactory, IAsyncDisposable
{
    private readonly ILogger<RedisConnectionFactory> _logger;
    private readonly CachingOptions _options;
    private IConnectionMultiplexer? _connection;

    public RedisConnectionFactory(IOptions<CachingOptions> options, ILogger<RedisConnectionFactory> logger)
    {
        _options = options.Value;
        _logger = logger;
    }

    public async ValueTask<IConnectionMultiplexer?> GetConnectionAsync()
    {
        if (!_options.EnableRedis)
        {
            return null;
        }

        if (_connection != null && _connection.IsConnected)
        {
            return _connection;
        }

        try
        {
            _connection = await ConnectionMultiplexer.ConnectAsync(_options.RedisConnectionString);
            _logger.LogInformation("Connected to Redis at {Endpoint}", _options.RedisConnectionString);
            return _connection;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to establish Redis connection using {ConnectionString}. Falling back to memory cache only.", _options.RedisConnectionString);
            _options.EnableRedis = false;
            return null;
        }
    }

    public async ValueTask DisposeAsync()
    {
        if (_connection != null)
        {
            await _connection.CloseAsync();
            _connection.Dispose();
        }
    }
}
