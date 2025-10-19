using FluentMigrator;

namespace RiskPortfolio.Infrastructure.Migrations;

[Migration(202410190001, "Add PortfolioSnapshots and Transactions tables")]
public class Migration_20241019_AddSnapshotsAndTransactions : Migration
{
    public override void Up()
    {
        // Create PortfolioSnapshots table
        Create.Table("PortfolioSnapshots")
            .WithColumn("Id").AsGuid().PrimaryKey("PK_PortfolioSnapshots")
            .WithColumn("PortfolioId").AsGuid().NotNullable()
            .WithColumn("SnapshotDate").AsDateTime().NotNullable()
            .WithColumn("TotalValue").AsDecimal(18, 2).NotNullable()
            .WithColumn("RiskScore").AsDecimal(18, 2).NotNullable()
            .WithColumn("ValueAtRisk").AsDecimal(18, 2).NotNullable()
            .WithColumn("PositionCount").AsInt32().NotNullable()
            .WithColumn("DailyReturn").AsDecimal(18, 4).NotNullable().WithDefaultValue(0)
            .WithColumn("CreatedAt").AsDateTime().NotNullable().WithDefault(SystemMethods.CurrentDateTime);

        Create.ForeignKey("FK_PortfolioSnapshots_Portfolios")
            .FromTable("PortfolioSnapshots").ForeignColumn("PortfolioId")
            .ToTable("Portfolios").PrimaryColumn("Id")
            .OnDelete(System.Data.Rule.Cascade);

        Create.Index("IX_PortfolioSnapshots_Portfolio_Date")
            .OnTable("PortfolioSnapshots")
            .OnColumn("PortfolioId").Ascending()
            .OnColumn("SnapshotDate").Ascending()
            .WithOptions().Unique();

        Create.Index("IX_PortfolioSnapshots_Date")
            .OnTable("PortfolioSnapshots")
            .OnColumn("SnapshotDate").Ascending();

        // Create Transactions table
        Create.Table("Transactions")
            .WithColumn("Id").AsGuid().PrimaryKey("PK_Transactions")
            .WithColumn("PortfolioId").AsGuid().NotNullable()
            .WithColumn("Symbol").AsString(10).NotNullable()
            .WithColumn("Type").AsInt32().NotNullable()
            .WithColumn("Quantity").AsDecimal(18, 8).NotNullable()
            .WithColumn("Price").AsDecimal(18, 2).NotNullable()
            .WithColumn("TotalAmount").AsDecimal(18, 2).NotNullable()
            .WithColumn("Commission").AsDecimal(18, 2).NotNullable().WithDefaultValue(0)
            .WithColumn("TransactionDate").AsDateTime().NotNullable()
            .WithColumn("Notes").AsString(500).Nullable()
            .WithColumn("CreatedAt").AsDateTime().NotNullable().WithDefault(SystemMethods.CurrentDateTime);

        Create.ForeignKey("FK_Transactions_Portfolios")
            .FromTable("Transactions").ForeignColumn("PortfolioId")
            .ToTable("Portfolios").PrimaryColumn("Id")
            .OnDelete(System.Data.Rule.Cascade);

        Create.Index("IX_Transactions_Portfolio")
            .OnTable("Transactions")
            .OnColumn("PortfolioId").Ascending();

        Create.Index("IX_Transactions_Symbol")
            .OnTable("Transactions")
            .OnColumn("Symbol").Ascending();

        Create.Index("IX_Transactions_Date")
            .OnTable("Transactions")
            .OnColumn("TransactionDate").Ascending();
    }

    public override void Down()
    {
        Delete.Table("Transactions");
        Delete.Table("PortfolioSnapshots");
    }
}
