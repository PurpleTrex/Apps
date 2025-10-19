import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface PositionCardWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function PositionCardWidget({ widget, onEdit, onDelete }: PositionCardWidgetProps) {
  const position = {
    symbol: 'AAPL',
    quantity: 500,
    currentPrice: 178.25,
    averagePrice: 145.50,
    gainLoss: 16375,
    gainLossPercent: 22.5
  };

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 'bold', color: 'white' }}>
            {position.symbol}
          </h3>
          <span style={{ fontSize: '0.875rem', color: '#64748b' }}>
            {position.quantity} shares
          </span>
        </div>
        
        <div style={{ marginBottom: '0.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Current Price</span>
            <span style={{ fontSize: '0.875rem', color: 'white', fontWeight: '600' }}>
              ${position.currentPrice.toFixed(2)}
            </span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Average Cost</span>
            <span style={{ fontSize: '0.875rem', color: 'white' }}>
              ${position.averagePrice.toFixed(2)}
            </span>
          </div>
        </div>
        
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          padding: '0.75rem',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          borderRadius: '0.375rem'
        }}>
          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Total Gain/Loss</span>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.875rem', color: '#10b981', fontWeight: '600' }}>
              +${position.gainLoss.toLocaleString()}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#10b981' }}>
              +{position.gainLossPercent}%
            </div>
          </div>
        </div>
      </div>
    </WidgetContainer>
  );
}