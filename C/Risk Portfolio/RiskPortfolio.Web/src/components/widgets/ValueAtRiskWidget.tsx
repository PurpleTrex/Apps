import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface ValueAtRiskWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function ValueAtRiskWidget({ widget, onEdit, onDelete }: ValueAtRiskWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444', marginBottom: '0.5rem' }}>
          $24,850
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '1rem' }}>
          95% Confidence, 1-day VaR
        </div>
        <div style={{ fontSize: '0.875rem', color: '#94a3b8' }}>
          Maximum expected loss over 1 day with 95% probability
        </div>
      </div>
    </WidgetContainer>
  );
}