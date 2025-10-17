using FluentMigrator;

namespace RiskPortfolio.Infrastructure.Migrations;

[Migration(202410170001)]
public class Migration_20241017_CreatePortfolioSchema : Migration
{
    public override void Up()
    {
        Create.Schema("portfolio");

        Create.Table("Portfolios").InSchema("portfolio")
            .WithColumn("Id").AsGuid().PrimaryKey()
            .WithColumn("Name").AsString(200).NotNullable()
            .WithColumn("Owner").AsString(200).NotNullable()
            .WithColumn("BaseCurrency").AsFixedLengthString(3).NotNullable()
            .WithColumn("RiskScore").AsDecimal(9, 4).WithDefaultValue(0)
            .WithColumn("ValueAtRisk").AsDecimal(18, 2).WithDefaultValue(0)
            .WithColumn("CreatedAt").AsDateTimeOffset().WithDefault(SystemMethods.CurrentUTCDateTime)
            .WithColumn("LastEvaluatedAt").AsDateTimeOffset().Nullable();

        Create.Table("AssetPositions").InSchema("portfolio")
            .WithColumn("Id").AsGuid().PrimaryKey()
            .WithColumn("PortfolioId").AsGuid().NotNullable().Indexed("IX_AssetPositions_Portfolio")
            .WithColumn("Symbol").AsString(32).NotNullable()
            .WithColumn("Quantity").AsDecimal(18, 4).NotNullable()
            .WithColumn("AveragePrice").AsDecimal(18, 4).NotNullable()
            .WithColumn("CurrentPrice").AsDecimal(18, 4).NotNullable()
            .WithColumn("Volatility").AsDecimal(5, 4).NotNullable();

        Create.ForeignKey("FK_AssetPositions_Portfolios")
            .FromTable("AssetPositions").InSchema("portfolio").ForeignColumn("PortfolioId")
            .ToTable("Portfolios").InSchema("portfolio").PrimaryColumn("Id")
            .OnDeleteOrUpdate(System.Data.Rule.Cascade);

        Create.Index("IX_AssetPositions_Symbol")
            .OnTable("AssetPositions").InSchema("portfolio")
            .OnColumn("Symbol").Ascending();
    }

    public override void Down()
    {
        Delete.Index("IX_AssetPositions_Symbol").OnTable("AssetPositions").InSchema("portfolio");
        Delete.ForeignKey("FK_AssetPositions_Portfolios").OnTable("AssetPositions").InSchema("portfolio");
        Delete.Table("AssetPositions").InSchema("portfolio");
        Delete.Table("Portfolios").InSchema("portfolio");
        Delete.Schema("portfolio");
    }
}
