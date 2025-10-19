using FluentMigrator;

namespace RiskPortfolio.Infrastructure.Migrations;

[Migration(202410180001, "Add DashboardWidgets table")]
public class Migration_20241018_AddWidgetsTable : Migration
{
    public override void Up()
    {
        Create.Table("DashboardWidgets")
            .WithColumn("Id").AsGuid().PrimaryKey("PK_DashboardWidgets")
            .WithColumn("UserId").AsString(100).NotNullable()
            .WithColumn("Title").AsString(200).NotNullable()
            .WithColumn("Type").AsInt32().NotNullable()
            .WithColumn("Configuration").AsString(int.MaxValue).NotNullable()
            .WithColumn("PositionX").AsInt32().NotNullable().WithDefaultValue(0)
            .WithColumn("PositionY").AsInt32().NotNullable().WithDefaultValue(0)
            .WithColumn("Width").AsInt32().NotNullable().WithDefaultValue(4)
            .WithColumn("Height").AsInt32().NotNullable().WithDefaultValue(2)
            .WithColumn("IsVisible").AsBoolean().NotNullable().WithDefaultValue(true)
            .WithColumn("DisplayOrder").AsInt32().NotNullable().WithDefaultValue(0)
            .WithColumn("CreatedAt").AsDateTime().NotNullable().WithDefault(SystemMethods.CurrentDateTime)
            .WithColumn("UpdatedAt").AsDateTime().Nullable();

        Create.Index("IX_DashboardWidgets_UserId")
            .OnTable("DashboardWidgets")
            .OnColumn("UserId").Ascending();

        Create.Index("IX_DashboardWidgets_UserId_DisplayOrder")
            .OnTable("DashboardWidgets")
            .OnColumn("UserId").Ascending()
            .OnColumn("DisplayOrder").Ascending();
    }

    public override void Down()
    {
        Delete.Table("DashboardWidgets");
    }
}
