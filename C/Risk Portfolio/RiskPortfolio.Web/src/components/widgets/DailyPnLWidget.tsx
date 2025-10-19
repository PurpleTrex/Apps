import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface DailyPnLWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function DailyPnLWidget({ widget, onEdit, onDelete }: DailyPnLWidgetProps) {
  const dailyPnL = 8947;
  const dailyPnLPercent = 2.14;
  const isPositive = dailyPnL >= 0;

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ marginBottom: '0.5rem' }}>
          <div style={{ 
            fontSize: '1.5rem', 
            fontWeight: 'bold', 
            color: isPositive ? '#10b981' : '#ef4444' 
          }}>
            {isPositive ? '+' : ''}${Math.abs(dailyPnL).toLocaleString()}
          </div>
          <div style={{ 
            fontSize: '0.875rem', 
            color: isPositive ? '#10b981' : '#ef4444',
            fontWeight: '600'
          }}>
            {isPositive ? '+' : ''}{dailyPnLPercent}%
          </div>
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
          Today's P&L
        </div>
      </div>
    </WidgetContainer>
  );
}