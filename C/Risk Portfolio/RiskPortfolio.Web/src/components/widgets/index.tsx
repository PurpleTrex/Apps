import type { Widget } from '@/types/widgets';
import { WidgetType } from '@/types/widgets';
import PortfolioValueWidget from './PortfolioValueWidget';
import PortfolioAllocationWidget from './PortfolioAllocationWidget';
import TopGainersWidget from './TopGainersWidget';
import TopLosersWidget from './TopLosersWidget';
import PositionListWidget from './PositionListWidget';
import QuickStatsWidget from './QuickStatsWidget';
import RiskMetricsWidget from './RiskMetricsWidget';

interface WidgetRendererProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function WidgetRenderer({ widget, onEdit, onDelete }: WidgetRendererProps) {
  const props = { widget, onEdit, onDelete };

  switch (widget.type) {
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
