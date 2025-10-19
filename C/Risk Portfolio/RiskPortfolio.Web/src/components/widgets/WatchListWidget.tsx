import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface WatchListWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function WatchListWidget({ widget, onEdit, onDelete }: WatchListWidgetProps) {
  const watchlist = [
    { symbol: 'TSLA', price: 248.50, change: -2.15 },
    { symbol: 'AMZN', price: 142.80, change: 1.45 },
    { symbol: 'NFLX', price: 425.30, change: 8.72 },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {watchlist.map((item) => (
          <div key={item.symbol} style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            marginBottom: '0.75rem'
          }}>
            <span style={{ color: 'white', fontWeight: '600' }}>{item.symbol}</span>
            <div style={{ textAlign: 'right' }}>
              <div style={{ color: 'white', fontSize: '0.875rem' }}>
                ${item.price.toFixed(2)}
              </div>
              <div style={{ 
                color: item.change >= 0 ? '#10b981' : '#ef4444',
                fontSize: '0.75rem'
              }}>
                {item.change >= 0 ? '+' : ''}${item.change.toFixed(2)}
              </div>
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}