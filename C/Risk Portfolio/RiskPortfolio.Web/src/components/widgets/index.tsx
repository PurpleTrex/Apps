import type { Widget } from '@/types/widgets';
import { WidgetType } from '@/types/widgets';
import PortfolioValueWidget from './PortfolioValueWidget';
import PortfolioAllocationWidget from './PortfolioAllocationWidget';
import TopGainersWidget from './TopGainersWidget';
import TopLosersWidget from './TopLosersWidget';
import PositionListWidget from './PositionListWidget';
import QuickStatsWidget from './QuickStatsWidget';
import RiskMetricsWidget from './RiskMetricsWidget';
import { 
  PortfolioPerformanceWidget,
  PortfolioRiskGaugeWidget,
  PositionCardWidget,
  DailyPnLWidget,
  TransactionsWidget,
  ValueAtRiskWidget,
  WatchListWidget,
  NewsWidget
} from './MissingWidgets';
import {
  SectorPerformanceWidget,
  EconomicCalendarWidget,
  CorrelationMatrixWidget,
  VolatilityChartWidget,
  DiversificationScoreWidget
} from './AdditionalWidgets2';
import {
  MarketSummaryWidget,
  AssetClassBreakdownWidget,
  DrawdownChartWidget,
  MonthlyReturnsWidget
} from './AdditionalWidgets4';
import {
  RebalancingNeedsWidget,
  TaxLossHarvestingWidget,
  ReturnsCalendarWidget,
  CustomChartWidget,
  NotesWidget,
  CalculatorWidget,
  AlertsWidget
} from './AdditionalWidgets3';

interface WidgetRendererProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function WidgetRenderer({ widget, onEdit, onDelete }: WidgetRendererProps) {
  const props = { widget, onEdit, onDelete };

  switch (widget.type) {
    // Original widgets
    case WidgetType.PortfolioValue:
      return <PortfolioValueWidget {...props} />;
    case WidgetType.PortfolioAllocation:
      return <PortfolioAllocationWidget {...props} />;
    case WidgetType.TopGainers:
      return <TopGainersWidget {...props} />;
    case WidgetType.TopLosers:
      return <TopLosersWidget {...props} />;
    case WidgetType.PositionList:
      return <PositionListWidget {...props} />;
    case WidgetType.QuickStats:
      return <QuickStatsWidget {...props} />;
    case WidgetType.RiskMetrics:
      return <RiskMetricsWidget {...props} />;

    // Additional widgets from AdditionalWidgets.tsx
    case WidgetType.PortfolioPerformance:
      return <PortfolioPerformanceWidget {...props} />;
    case WidgetType.PortfolioRiskGauge:
      return <PortfolioRiskGaugeWidget {...props} />;
    case WidgetType.PositionCard:
      return <PositionCardWidget {...props} />;
    case WidgetType.DailyPnL:
      return <DailyPnLWidget {...props} />;
    case WidgetType.Transactions:
      return <TransactionsWidget {...props} />;
    case WidgetType.ValueAtRisk:
      return <ValueAtRiskWidget {...props} />;
    case WidgetType.WatchList:
      return <WatchListWidget {...props} />;
    case WidgetType.MarketNews:
      return <NewsWidget {...props} />;

    // Additional widgets from AdditionalWidgets2.tsx
    case WidgetType.MarketOverview:
      return <MarketSummaryWidget {...props} />;
    case WidgetType.AssetAllocation:
      return <AssetClassBreakdownWidget {...props} />;
    case WidgetType.ReturnsCalendar:
      return <ReturnsCalendarWidget {...props} />;
    case WidgetType.PerformanceChart:
      return <DrawdownChartWidget {...props} />;
    case WidgetType.SectorPerformance:
      return <SectorPerformanceWidget {...props} />;
    case WidgetType.EconomicCalendar:
      return <EconomicCalendarWidget {...props} />;
    case WidgetType.CorrelationMatrix:
      return <CorrelationMatrixWidget {...props} />;
    case WidgetType.VolatilityChart:
      return <VolatilityChartWidget {...props} />;
    case WidgetType.DiversificationScore:
      return <DiversificationScoreWidget {...props} />;

    // Additional widgets from AdditionalWidgets3.tsx  
    case WidgetType.RebalancingNeeds:
      return <RebalancingNeedsWidget {...props} />;
    case WidgetType.TaxLossHarvesting:
      return <TaxLossHarvestingWidget {...props} />;
    case WidgetType.CustomChart:
      return <CustomChartWidget {...props} />;
    case WidgetType.Notes:
      return <NotesWidget {...props} />;
    case WidgetType.Calculator:
      return <CalculatorWidget {...props} />;
    case WidgetType.Alerts:
      return <AlertsWidget {...props} />;
    case WidgetType.Dividends:
      return <MonthlyReturnsWidget {...props} />;

    // Note: PerformanceAttributionWidget available but no matching WidgetType enum yet

    default:
      return (
        <div style={{
          padding: '2rem',
          textAlign: 'center',
          color: '#64748b',
          fontSize: '0.875rem',
          border: '1px dashed rgba(148, 163, 184, 0.2)',
          borderRadius: '0.5rem'
        }}>
          Widget type "{widget.type}" not yet implemented
        </div>
      );
  }
}

export {
  PortfolioValueWidget,
  PortfolioAllocationWidget,
  TopGainersWidget,
  TopLosersWidget,
  PositionListWidget,
  QuickStatsWidget,
  RiskMetricsWidget
};
