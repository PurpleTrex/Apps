# Seed Portfolio Data Script
# Creates 10 realistic portfolios with positions via the API

$apiUrl = "http://localhost:5012/api/portfolios"

# Define 10 portfolios with realistic data
$portfolios = @(
    @{
        name = "Tech Growth Fund"
        owner = "Sarah Johnson"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "AAPL"; quantity = 500; averagePrice = 145.50; currentPrice = 178.25; volatility = 0.28 }
            @{ symbol = "MSFT"; quantity = 350; averagePrice = 285.00; currentPrice = 372.45; volatility = 0.24 }
            @{ symbol = "GOOGL"; quantity = 200; averagePrice = 125.30; currentPrice = 141.80; volatility = 0.26 }
            @{ symbol = "NVDA"; quantity = 300; averagePrice = 425.00; currentPrice = 495.20; volatility = 0.42 }
            @{ symbol = "META"; quantity = 250; averagePrice = 310.50; currentPrice = 485.75; volatility = 0.35 }
        )
    },
    @{
        name = "Dividend Income Portfolio"
        owner = "Michael Chen"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "JNJ"; quantity = 400; averagePrice = 158.20; currentPrice = 162.35; volatility = 0.15 }
            @{ symbol = "PG"; quantity = 350; averagePrice = 142.80; currentPrice = 155.90; volatility = 0.14 }
            @{ symbol = "KO"; quantity = 600; averagePrice = 58.50; currentPrice = 61.25; volatility = 0.16 }
            @{ symbol = "PEP"; quantity = 300; averagePrice = 168.40; currentPrice = 175.85; volatility = 0.14 }
            @{ symbol = "T"; quantity = 800; averagePrice = 18.75; currentPrice = 17.90; volatility = 0.22 }
            @{ symbol = "VZ"; quantity = 500; averagePrice = 38.20; currentPrice = 41.15; volatility = 0.19 }
        )
    },
    @{
        name = "Crypto Ventures"
        owner = "Alex Rivera"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "BTC"; quantity = 2.5; averagePrice = 42000.00; currentPrice = 67850.00; volatility = 0.68 }
            @{ symbol = "ETH"; quantity = 15; averagePrice = 2800.00; currentPrice = 3625.50; volatility = 0.72 }
            @{ symbol = "COIN"; quantity = 400; averagePrice = 185.00; currentPrice = 215.75; volatility = 0.58 }
            @{ symbol = "MSTR"; quantity = 150; averagePrice = 420.00; currentPrice = 1285.40; volatility = 0.85 }
        )
    },
    @{
        name = "European Blue Chips"
        owner = "Emma Schmidt"
        baseCurrency = "EUR"
        positions = @(
            @{ symbol = "SAP"; quantity = 300; averagePrice = 118.50; currentPrice = 145.25; volatility = 0.25 }
            @{ symbol = "ASML"; quantity = 150; averagePrice = 625.00; currentPrice = 895.30; volatility = 0.32 }
            @{ symbol = "LVMH"; quantity = 100; averagePrice = 720.00; currentPrice = 825.50; volatility = 0.22 }
            @{ symbol = "TTE"; quantity = 500; averagePrice = 58.30; currentPrice = 62.85; volatility = 0.28 }
            @{ symbol = "SIE"; quantity = 250; averagePrice = 142.00; currentPrice = 168.90; volatility = 0.26 }
        )
    },
    @{
        name = "ESG Sustainable Fund"
        owner = "James Wilson"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "TSLA"; quantity = 200; averagePrice = 220.00; currentPrice = 248.50; volatility = 0.52 }
            @{ symbol = "ENPH"; quantity = 300; averagePrice = 145.00; currentPrice = 128.75; volatility = 0.48 }
            @{ symbol = "NEE"; quantity = 400; averagePrice = 72.50; currentPrice = 68.90; volatility = 0.21 }
            @{ symbol = "ICLN"; quantity = 1000; averagePrice = 22.30; currentPrice = 21.45; volatility = 0.35 }
        )
    },
    @{
        name = "Emerging Markets"
        owner = "Priya Patel"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "BABA"; quantity = 500; averagePrice = 95.00; currentPrice = 78.35; volatility = 0.45 }
            @{ symbol = "TSM"; quantity = 350; averagePrice = 92.50; currentPrice = 145.80; volatility = 0.35 }
            @{ symbol = "VALE"; quantity = 600; averagePrice = 14.20; currentPrice = 12.85; volatility = 0.38 }
            @{ symbol = "ITUB"; quantity = 1200; averagePrice = 5.80; currentPrice = 6.45; volatility = 0.32 }
            @{ symbol = "EEM"; quantity = 800; averagePrice = 42.00; currentPrice = 44.25; volatility = 0.28 }
        )
    },
    @{
        name = "Healthcare Innovation"
        owner = "Dr. Robert Martinez"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "UNH"; quantity = 150; averagePrice = 485.00; currentPrice = 558.75; volatility = 0.19 }
            @{ symbol = "LLY"; quantity = 200; averagePrice = 565.00; currentPrice = 785.40; volatility = 0.26 }
            @{ symbol = "ABBV"; quantity = 300; averagePrice = 142.50; currentPrice = 178.25; volatility = 0.22 }
            @{ symbol = "TMO"; quantity = 180; averagePrice = 520.00; currentPrice = 585.90; volatility = 0.20 }
            @{ symbol = "GILD"; quantity = 400; averagePrice = 72.00; currentPrice = 85.60; volatility = 0.24 }
        )
    },
    @{
        name = "Real Estate Holdings"
        owner = "Linda Thompson"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "AMT"; quantity = 250; averagePrice = 205.00; currentPrice = 218.45; volatility = 0.18 }
            @{ symbol = "PLD"; quantity = 300; averagePrice = 128.50; currentPrice = 145.90; volatility = 0.22 }
            @{ symbol = "EQIX"; quantity = 100; averagePrice = 745.00; currentPrice = 825.30; volatility = 0.24 }
            @{ symbol = "SPG"; quantity = 400; averagePrice = 115.00; currentPrice = 142.75; volatility = 0.28 }
            @{ symbol = "O"; quantity = 600; averagePrice = 58.00; currentPrice = 62.15; volatility = 0.19 }
        )
    },
    @{
        name = "Conservative Balanced"
        owner = "Richard Anderson"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "BND"; quantity = 2000; averagePrice = 78.50; currentPrice = 75.90; volatility = 0.08 }
            @{ symbol = "SPY"; quantity = 500; averagePrice = 425.00; currentPrice = 562.85; volatility = 0.18 }
            @{ symbol = "GLD"; quantity = 300; averagePrice = 182.00; currentPrice = 195.40; volatility = 0.14 }
            @{ symbol = "IEF"; quantity = 1000; averagePrice = 102.50; currentPrice = 98.75; volatility = 0.09 }
            @{ symbol = "VNQ"; quantity = 400; averagePrice = 88.00; currentPrice = 95.20; volatility = 0.21 }
        )
    },
    @{
        name = "Aggressive Growth"
        owner = "Jessica Kim"
        baseCurrency = "USD"
        positions = @(
            @{ symbol = "SHOP"; quantity = 250; averagePrice = 68.00; currentPrice = 95.85; volatility = 0.55 }
            @{ symbol = "SQ"; quantity = 400; averagePrice = 85.00; currentPrice = 78.30; volatility = 0.62 }
            @{ symbol = "ARKK"; quantity = 800; averagePrice = 52.00; currentPrice = 48.75; volatility = 0.48 }
            @{ symbol = "PLTR"; quantity = 1000; averagePrice = 18.50; currentPrice = 35.40; volatility = 0.68 }
            @{ symbol = "SNOW"; quantity = 200; averagePrice = 185.00; currentPrice = 162.90; volatility = 0.58 }
            @{ symbol = "CRWD"; quantity = 150; averagePrice = 220.00; currentPrice = 315.75; volatility = 0.45 }
        )
    }
)

Write-Host "🚀 Starting Portfolio Seed Script..." -ForegroundColor Cyan
Write-Host "API Endpoint: $apiUrl`n" -ForegroundColor Gray

$successCount = 0
$failCount = 0

foreach ($portfolio in $portfolios) {
    Write-Host "📊 Creating portfolio: $($portfolio.name)" -ForegroundColor Yellow
    Write-Host "   Owner: $($portfolio.owner)" -ForegroundColor Gray
    Write-Host "   Positions: $($portfolio.positions.Count)" -ForegroundColor Gray
    
    try {
        $body = $portfolio | ConvertTo-Json -Depth 10
        
        $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        
        Write-Host "   ✅ Created successfully (ID: $($response.id))" -ForegroundColor Green
        Write-Host "   💰 Total Value: $([math]::Round($response.totalValue, 2))" -ForegroundColor Cyan
        Write-Host "   ⚠️  Risk Score: $([math]::Round($response.riskScore, 1))" -ForegroundColor Magenta
        Write-Host ""
        
        $successCount++
    }
    catch {
        Write-Host "   ❌ Failed to create: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        $failCount++
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host "`n" + "="*60 -ForegroundColor Gray
Write-Host "📈 Seed Summary:" -ForegroundColor Cyan
Write-Host "   ✅ Successfully created: $successCount portfolios" -ForegroundColor Green
Write-Host "   ❌ Failed: $failCount portfolios" -ForegroundColor Red
Write-Host "="*60 -ForegroundColor Gray

if ($successCount -gt 0) {
    Write-Host "`n🎉 Portfolios seeded! Open http://localhost:3000 to view them." -ForegroundColor Green
}
