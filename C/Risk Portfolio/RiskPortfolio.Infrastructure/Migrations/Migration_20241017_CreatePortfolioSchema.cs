using FluentMigrator;

namespace RiskPortfolio.Infrastructure.Migrations;

[Migration(202410170001)]
public class Migration_20241017_CreatePortfolioSchema : Migration
{
    public override void Up()
    {
        Create.Table("Portfolios")
            .WithColumn("Id").AsGuid().PrimaryKey()
            .WithColumn("Name").AsString(200).NotNullable()
            .WithColumn("Owner").AsString(200).NotNullable()
            .WithColumn("BaseCurrency").AsFixedLengthString(3).NotNullable()
            .WithColumn("RiskScore").AsDecimal(9, 4).WithDefaultValue(0)
            .WithColumn("ValueAtRisk").AsDecimal(18, 2).WithDefaultValue(0)
            .WithColumn("CreatedAt").AsDateTime().WithDefault(SystemMethods.CurrentUTCDateTime)
            .WithColumn("LastEvaluatedAt").AsDateTime().Nullable();

        Create.Table("AssetPositions")
            .WithColumn("Id").AsGuid().PrimaryKey()
            .WithColumn("PortfolioId").AsGuid().NotNullable().Indexed("IX_AssetPositions_Portfolio")
            .WithColumn("Symbol").AsString(32).NotNullable()
            .WithColumn("Quantity").AsDecimal(18, 4).NotNullable()
            .WithColumn("AveragePrice").AsDecimal(18, 4).NotNullable()
            .WithColumn("CurrentPrice").AsDecimal(18, 4).NotNullable()
            .WithColumn("Volatility").AsDecimal(5, 4).NotNullable();

        Create.Index("IX_AssetPositions_Symbol")
            .OnTable("AssetPositions")
            .OnColumn("Symbol").Ascending();
    }

    public override void Down()
    {
        Delete.Index("IX_AssetPositions_Symbol").OnTable("AssetPositions");
        Delete.Table("AssetPositions");
        Delete.Table("Portfolios");
    }
}
