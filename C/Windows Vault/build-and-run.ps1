# Windows Vault Build and Run Script
# This script builds and runs the Windows Vault application

Write-Host "🗂️ Windows Vault - Build and Run Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if .NET 8 is installed
Write-Host "Checking .NET 8 installation..." -ForegroundColor Yellow
try {
    $dotnetVersion = dotnet --version
    Write-Host "✅ .NET Version: $dotnetVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ .NET 8 is required but not found. Please install .NET 8 SDK." -ForegroundColor Red
    Write-Host "Download from: https://dotnet.microsoft.com/download/dotnet/8.0" -ForegroundColor Yellow
    exit 1
}

# Navigate to project directory
$projectPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectPath

Write-Host "📁 Project Directory: $projectPath" -ForegroundColor Blue

# Restore NuGet packages
Write-Host "📦 Restoring NuGet packages..." -ForegroundColor Yellow
dotnet restore
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to restore packages" -ForegroundColor Red
    exit 1
}

# Build the application
Write-Host "🔨 Building application..." -ForegroundColor Yellow
dotnet build --configuration Release
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build completed successfully!" -ForegroundColor Green

# Ask user if they want to run the application
$runApp = Read-Host "Would you like to run Windows Vault now? (Y/N)"
if ($runApp -eq "Y" -or $runApp -eq "y") {
    Write-Host "🚀 Starting Windows Vault..." -ForegroundColor Green
    dotnet run --configuration Release
} else {
    Write-Host "✅ Build complete. You can run the application later with 'dotnet run'" -ForegroundColor Green
}

Write-Host "`n🎉 Windows Vault is ready to organize your media!" -ForegroundColor Magenta