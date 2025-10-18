# Alpha Vantage API Integration - Setup Guide

## Overview
This document describes the integration of Alpha Vantage API for real-time market data in the Risk Portfolio application. The system includes automatic hourly background refresh of all portfolio positions.

## Features

### Automatic Market Data Refresh
- **Background Job**: Runs every hour to refresh all portfolio positions with live market data
- **Configurable**: Can be enabled/disabled and customized via `appsettings.json`
- **Rate Limiting**: Respects Alpha Vantage free tier limits (5 calls per minute)
- **Smart Updates**: Only fetches data for active portfolio positions
- **Risk Recalculation**: Automatically recalculates portfolio risk metrics after price updates

## Backend Changes

### 1. New Files Created

#### Core Layer
- `RiskPortfolio.Core/Interfaces/IMarketDataService.cs`
  - Interface for market data operations
  - MarketQuote model for quote data

#### Infrastructure Layer
- `RiskPortfolio.Infrastructure/Options/AlphaVantageOptions.cs`
  - Configuration options for Alpha Vantage API

- `RiskPortfolio.Infrastructure/Options/MarketDataRefreshOptions.cs`
  - Configuration options for automatic market data refresh
  
- `RiskPortfolio.Infrastructure/Services/AlphaVantageMarketDataService.cs`
  - Implementation of IMarketDataService
  - Integrates with Alpha Vantage API
  - Includes caching support
  - Rate limiting (5 calls per minute for free tier)

- `RiskPortfolio.Infrastructure/Jobs/MarketDataRefreshJob.cs`
  - Background service that runs every hour
  - Automatically refreshes all portfolio positions
  - Fetches live prices from Alpha Vantage
  - Recalculates portfolio risk metrics
  - Configurable refresh interval and rate limiting

#### API Layer
- `RiskPortfolio.Api/Controllers/MarketDataController.cs`
  - REST endpoints for market data
  - GET `/api/market-data/{symbol}/price` - Get current price
  - GET `/api/market-data/{symbol}/quote` - Get detailed quote
  - POST `/api/market-data/quotes` - Batch quotes
  - POST `/api/market-data/refresh-positions` - Refresh position prices

### 2. Configuration Updates

#### appsettings.json
```json
{
  "AlphaVantage": {
    "ApiKey": "5687GYS0UKPVU3OZ",
    "BaseUrl": "https://www.alphavantage.co/query",
    "RequestTimeoutSeconds": 30,
    "CacheDurationMinutes": 5
  },
  "MarketDataRefresh": {
    "Enabled": true,
    "RefreshIntervalMinutes": 60,
    "StartupDelayMinutes": 5,
    "RateLimitDelaySeconds": 12
  }
}
```

**MarketDataRefresh Configuration:**
- `Enabled`: Enable/disable automatic refresh (default: `true`)
- `RefreshIntervalMinutes`: How often to refresh data (default: `60` minutes)
- `StartupDelayMinutes`: Delay before first refresh after startup (default: `5` minutes)
- `RateLimitDelaySeconds`: Delay between API calls to respect rate limits (default: `12` seconds = 5 calls/minute)

#### ServiceCollectionExtensions.cs
- Added AlphaVantage configuration
- Added MarketDataRefresh configuration
- Registered IMarketDataService with HttpClient
- Added HttpClient factory for Alpha Vantage service
- Registered MarketDataRefreshJob as hosted service

## Frontend Changes

### 1. New Files Created

#### Services
- `src/services/marketData.ts`
  - MarketQuote interface
  - Service methods for market data API calls
  - `getPrice()`, `getQuote()`, `getBatchQuotes()`, `refreshPositions()`

#### Hooks
- `src/hooks/useMarketData.ts`
  - React Query hooks for market data
  - `useMarketQuote()` - Single symbol quote
  - `useBatchQuotes()` - Multiple symbols
  - `useRefreshPositions()` - Refresh all positions
  - `useMarketPrice()` - Simple price fetch
  - Auto-refresh every 5 minutes

### 2. Seed Scripts

#### seed-portfolios-live.ps1
- Enhanced PowerShell script with Alpha Vantage integration
- Fetches LIVE prices for stocks during seeding
- Automatic rate limiting (12 seconds between calls)
- Calculates volatility estimates based on price movement
- Creates 3 portfolios with real market data:
  - Tech Growth Fund (AAPL, MSFT, GOOGL, NVDA, META)
  - Dividend Income Portfolio (JNJ, PG, KO, PEP, VZ)
  - Healthcare Innovation (UNH, LLY, ABBV, TMO, GILD)

## API Endpoints

### Market Data Controller

#### Get Current Price
```
GET /api/market-data/{symbol}/price
Response: { "symbol": "AAPL", "price": 178.25 }
```

#### Get Detailed Quote
```
GET /api/market-data/{symbol}/quote
Response: MarketQuote object with price, change, volume, etc.
```

#### Get Batch Quotes
```
POST /api/market-data/quotes
Body: { "symbols": ["AAPL", "MSFT", "GOOGL"] }
Response: Dictionary<string, MarketQuote>
```

#### Refresh Position Prices
```
POST /api/market-data/refresh-positions
Body: { "symbols": ["AAPL", "MSFT"] }
Response: Dictionary<string, decimal>
```

## Usage Examples

### Frontend - React Component
```typescript
import { useMarketQuote, useRefreshPositions } from '@/hooks/useMarketData';

function PositionCard({ symbol }) {
  const { data: quote, isLoading } = useMarketQuote(symbol);
  const refreshMutation = useRefreshPositions();
  
  const handleRefresh = () => {
    refreshMutation.mutate([symbol]);
  };
  
  return (
    <div>
      <h3>{symbol}</h3>
      <p>Price: ${quote?.price}</p>
      <p>Change: {quote?.changePercent}%</p>
      <button onClick={handleRefresh}>Refresh</button>
    </div>
  );
}
```

### Backend - C# Service
```csharp
public class PortfolioService
{
    private readonly IMarketDataService _marketData;
    
    public async Task UpdatePortfolioPrices(Portfolio portfolio)
    {
        var symbols = portfolio.Positions.Select(p => p.Symbol);
        var quotes = await _marketData.GetBatchQuotesAsync(symbols);
        
        foreach (var position in portfolio.Positions)
        {
            if (quotes.TryGetValue(position.Symbol, out var quote))
            {
                position.UpdatePrice(quote.Price);
            }
        }
    }
}
```

## Running the Application

### 1. Start Backend
```powershell
cd "RiskPortfolio.Api"
dotnet run
```

**What happens on startup:**
- Database migrations run automatically
- **MarketDataRefreshJob starts** and waits 5 minutes before first refresh
- After 5 minutes, the job fetches live prices for all portfolio positions
- Background refresh runs every hour automatically

### 2. Seed with Live Data
```powershell
# Run the enhanced seed script
.\seed-portfolios-live.ps1
```
This will:
- Fetch live prices from Alpha Vantage
- Create 3 portfolios with real market data
- Display progress and results

### 3. Start Frontend
```powershell
cd "RiskPortfolio.Web"
npm run dev
```

### 4. Monitor Background Job
Check the console logs to see the automatic refresh in action:
```
info: RiskPortfolio.Infrastructure.Jobs.MarketDataRefreshJob[0]
      Market Data Refresh Job started. Refresh interval: 01:00:00, Startup delay: 00:05:00
info: RiskPortfolio.Infrastructure.Jobs.MarketDataRefreshJob[0]
      Starting market data refresh for all portfolios
info: RiskPortfolio.Infrastructure.Jobs.MarketDataRefreshJob[0]
      Found 3 portfolios to refresh
info: RiskPortfolio.Infrastructure.Jobs.MarketDataRefreshJob[0]
      Refreshing portfolio: Tech Growth Portfolio (ID: 1)
info: RiskPortfolio.Infrastructure.Jobs.MarketDataRefreshJob[0]
      Portfolio Tech Growth Portfolio updated: 5 positions refreshed, new total value: $125,432.50
```

## Features

### ✅ Automatic Background Refresh (NEW!)
- Runs every hour by default
- Fetches live prices for all portfolio positions
- Automatically recalculates risk metrics
- Configurable refresh interval
- Can be enabled/disabled via config

### ✅ Real-Time Market Data
- Live stock prices from Alpha Vantage
- 15-minute delayed data (free tier)
- Automatic caching (5 minutes)

### ✅ Rate Limiting
- Respects API limits (5 calls/minute)
- Automatic delays between requests
- Queue management for batch operations

### ✅ Error Handling
- Graceful fallbacks for API failures
- Comprehensive logging
- User-friendly error messages

### ✅ Caching Strategy
- Redis/Memory cache support
- 5-minute cache duration
- Reduces API calls
- Improves performance

## Next Steps

1. **Add More Portfolios**: Modify `seed-portfolios-live.ps1` to add more portfolios
2. **Real-Time Updates**: Implement WebSocket for live price streaming
3. **Historical Data**: Add endpoints for historical price charts
4. **Premium Features**: Consider Alpha Vantage premium for:
   - Real-time data (no delay)
   - Higher rate limits
   - More advanced indicators

## Troubleshooting

### Issue: Background Job Not Running
**Solution**: 
- Check `MarketDataRefresh.Enabled` is `true` in `appsettings.json`
- Look for startup log: "Market Data Refresh Job started"
- If disabled, you'll see: "Market Data Refresh Job is disabled"

### Issue: Background Job Timing
**Question**: When does the first refresh happen?
**Answer**: 
- Default: 5 minutes after startup
- Configurable via `MarketDataRefresh.StartupDelayMinutes`
- Subsequent refreshes: Every 60 minutes (configurable)

### Issue: Too Many API Calls
**Solution**: 
- Adjust `MarketDataRefresh.RefreshIntervalMinutes` (increase to reduce calls)
- Adjust `MarketDataRefresh.RateLimitDelaySeconds` (increase to slow down)
- Free tier: 5 calls per minute max
- Default config respects this limit with 12-second delays

### Issue: API Rate Limit Exceeded
**Solution**: The free tier allows 5 calls per minute. The seed script automatically handles this with 12-second delays.

### Issue: No Price Data Returned
**Solution**: 
- Check API key is correct in `appsettings.json`
- Verify symbol exists (Alpha Vantage uses US market symbols)
- Check Alpha Vantage status: https://www.alphavantage.co/

### Issue: Cached Data Not Updating
**Solution**: 
- Cache duration is 5 minutes
- Clear cache or wait for expiration
- Use `/refresh-positions` endpoint to force update

## Resources

- Alpha Vantage Documentation: https://www.alphavantage.co/documentation/
- API Key Management: https://www.alphavantage.co/support/#api-key
- Stock Symbol Lookup: https://www.alphavantage.co/query?function=SYMBOL_SEARCH
