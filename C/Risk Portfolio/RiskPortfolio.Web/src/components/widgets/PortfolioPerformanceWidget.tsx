import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface PortfolioPerformanceWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function PortfolioPerformanceWidget({ widget, onEdit, onDelete }: PortfolioPerformanceWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <h3 style={{ fontSize: '0.875rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>
          Portfolio Performance
        </h3>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>1D</span>
          <span style={{ fontSize: '0.875rem', color: '#10b981', fontWeight: '600' }}>+2.14%</span>
        </div>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>1W</span>
          <span style={{ fontSize: '0.875rem', color: '#10b981', fontWeight: '600' }}>+5.67%</span>
        </div>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>1M</span>
          <span style={{ fontSize: '0.875rem', color: '#10b981', fontWeight: '600' }}>+12.34%</span>
        </div>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>YTD</span>
          <span style={{ fontSize: '0.875rem', color: '#10b981', fontWeight: '600' }}>+28.91%</span>
        </div>
      </div>
    </WidgetContainer>
  );
}