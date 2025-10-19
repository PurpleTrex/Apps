FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copy project files
COPY ["RiskPortfolio.Api/RiskPortfolio.Api.csproj", "RiskPortfolio.Api/"]
COPY ["RiskPortfolio.Core/RiskPortfolio.Core.csproj", "RiskPortfolio.Core/"]
COPY ["RiskPortfolio.Infrastructure/RiskPortfolio.Infrastructure.csproj", "RiskPortfolio.Infrastructure/"]

# Restore dependencies
RUN dotnet restore "RiskPortfolio.Api/RiskPortfolio.Api.csproj"

# Copy everything else
COPY . .

# Build and publish
WORKDIR "/src/RiskPortfolio.Api"
RUN dotnet publish "RiskPortfolio.Api.csproj" -c Release -o /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY --from=build /app/publish .

# Create data directory for SQLite database
RUN mkdir -p /app/data

# Expose port
EXPOSE 8080

# Set environment variables
ENV ASPNETCORE_URLS=http://+:8080

ENTRYPOINT ["dotnet", "RiskPortfolio.Api.dll"]
