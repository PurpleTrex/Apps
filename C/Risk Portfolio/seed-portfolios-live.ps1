# Enhanced Seed Portfolio Data Script with Live Market Prices
# Fetches real prices from Alpha Vantage API and creates portfolios

$apiUrl = "http://localhost:5012/api/portfolios"
$alphaVantageKey = "5687GYS0UKPVU3OZ"
$alphaVantageUrl = "https://www.alphavantage.co/query"

# Function to get current price from Alpha Vantage
function Get-LivePrice {
    param (
        [string]$Symbol
    )
    
    try {
        $url = "$alphaVantageUrl`?function=GLOBAL_QUOTE&symbol=$Symbol&apikey=$alphaVantageKey"
        Write-Host "Fetching live price for $Symbol..." -ForegroundColor Cyan
        
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 30
        
        if ($response.'Global Quote'.'05. price') {
            $price = [decimal]$response.'Global Quote'.'05. price'
            Write-Host "  ✓ $Symbol`: $$$price" -ForegroundColor Green
            Start-Sleep -Milliseconds 12000  # Rate limit: 5 calls per minute (12 seconds between calls)
            return $price
        }
        else {
            Write-Host "  ✗ No price data for $Symbol" -ForegroundColor Yellow
            return $null
        }
    }
    catch {
        Write-Host "  ✗ Error fetching $Symbol`: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Function to calculate volatility estimate (simplified)
function Get-VolatilityEstimate {
    param (
        [string]$Symbol,
        [decimal]$CurrentPrice,
        [decimal]$AveragePrice
    )
    
    # Simple volatility estimate based on price movement
    $priceChange = [Math]::Abs(($CurrentPrice - $AveragePrice) / $AveragePrice)
    $volatility = $priceChange * 0.5  # Rough estimate
    
    # Tech stocks: higher volatility
    if ($Symbol -match "NVDA|TSLA|META|COIN|MSTR") {
        $volatility = [Math]::Min($volatility * 1.5, 0.85)
    }
    # Blue chips: lower volatility
    elseif ($Symbol -match "JNJ|PG|KO|PEP|UNH") {
        $volatility = [Math]::Max($volatility * 0.6, 0.15)
    }
    
    # Clamp between 0.12 and 0.85
    return [Math]::Max([Math]::Min($volatility, 0.85), 0.12)
}

Write-Host "============================================" -ForegroundColor Magenta
Write-Host "  Risk Portfolio Seeder with Live Prices" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""

# Define portfolios with real stock symbols
$portfolios = @(
    @{
        name = "Tech Growth Fund"
        owner = "Sarah Johnson"
        baseCurrency = "USD"
        symbols = @("AAPL", "MSFT", "GOOGL", "NVDA", "META")
        quantities = @(500, 350, 200, 300, 250)
        averagePrices = @(145.50, 285.00, 125.30, 425.00, 310.50)
    },
    @{
        name = "Dividend Income Portfolio"
        owner = "Michael Chen"
        baseCurrency = "USD"
        symbols = @("JNJ", "PG", "KO", "PEP", "VZ")
        quantities = @(400, 350, 600, 300, 500)
        averagePrices = @(158.20, 142.80, 58.50, 168.40, 38.20)
    },
    @{
        name = "Healthcare Innovation"
        owner = "Dr. Robert Martinez"
        baseCurrency = "USD"
        symbols = @("UNH", "LLY", "ABBV", "TMO", "GILD")
        quantities = @(150, 200, 300, 180, 400)
        averagePrices = @(485.00, 565.00, 142.50, 520.00, 72.00)
    }
)

$createdCount = 0
$failedCount = 0

foreach ($portfolio in $portfolios) {
    Write-Host ""
    Write-Host "Creating portfolio: $($portfolio.name)" -ForegroundColor Yellow
    Write-Host "Owner: $($portfolio.owner)" -ForegroundColor Gray
    Write-Host "Fetching live market data..." -ForegroundColor Gray
    Write-Host ""
    
    $positions = @()
    
    for ($i = 0; $i -lt $portfolio.symbols.Length; $i++) {
        $symbol = $portfolio.symbols[$i]
        $quantity = $portfolio.quantities[$i]
        $avgPrice = $portfolio.averagePrices[$i]
        
        # Get live price from Alpha Vantage
        $livePrice = Get-LivePrice -Symbol $symbol
        
        if ($livePrice) {
            $currentPrice = $livePrice
        }
        else {
            # Fallback: simulate a price change
            $randomChange = (Get-Random -Minimum -10 -Maximum 20) / 100.0
            $currentPrice = [Math]::Round($avgPrice * (1 + $randomChange), 2)
            Write-Host "  Using fallback price for $symbol`: $$$currentPrice" -ForegroundColor DarkYellow
        }
        
        # Calculate estimated volatility
        $volatility = Get-VolatilityEstimate -Symbol $symbol -CurrentPrice $currentPrice -AveragePrice $avgPrice
        
        $positions += @{
            symbol = $symbol
            quantity = $quantity
            averagePrice = $avgPrice
            currentPrice = $currentPrice
            volatility = $volatility
        }
    }
    
    # Create portfolio payload
    $portfolioData = @{
        name = $portfolio.name
        owner = $portfolio.owner
        baseCurrency = $portfolio.baseCurrency
        positions = $positions
    } | ConvertTo-Json -Depth 10
    
    # Send to API
    try {
        $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $portfolioData -ContentType "application/json" -TimeoutSec 30
        Write-Host ""
        Write-Host "✓ Portfolio created successfully!" -ForegroundColor Green
        Write-Host "  ID: $($response.id)" -ForegroundColor Gray
        Write-Host "  Total Value: $$$([Math]::Round($response.totalValue, 2))" -ForegroundColor Gray
        Write-Host "  Risk Score: $([Math]::Round($response.riskScore, 2))" -ForegroundColor Gray
        $createdCount++
    }
    catch {
        Write-Host ""
        Write-Host "✗ Failed to create portfolio: $($_.Exception.Message)" -ForegroundColor Red
        $failedCount++
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "  Summary" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "Portfolios created: $createdCount" -ForegroundColor Green
Write-Host "Portfolios failed: $failedCount" -ForegroundColor $(if ($failedCount -gt 0) { "Red" } else { "Gray" })
Write-Host ""
