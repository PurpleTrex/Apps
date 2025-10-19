import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface TransactionsWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function TransactionsWidget({ widget, onEdit, onDelete }: TransactionsWidgetProps) {
  const transactions = [
    { id: 1, symbol: 'AAPL', type: 'BUY', quantity: 100, price: 175.50, date: '2024-10-18' },
    { id: 2, symbol: 'GOOGL', type: 'SELL', quantity: 50, price: 141.80, date: '2024-10-17' },
    { id: 3, symbol: 'MSFT', type: 'BUY', quantity: 200, price: 372.45, date: '2024-10-16' },
    { id: 4, symbol: 'NVDA', type: 'BUY', quantity: 75, price: 495.20, date: '2024-10-15' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: '1fr 1fr 1fr 1fr 1fr',
          gap: '0.5rem',
          marginBottom: '0.75rem',
          fontSize: '0.75rem',
          fontWeight: '600',
          color: '#64748b',
          textTransform: 'uppercase',
          letterSpacing: '0.025em'
        }}>
          <div>Symbol</div>
          <div>Type</div>
          <div>Qty</div>
          <div>Price</div>
          <div>Date</div>
        </div>
        
        {transactions.map((transaction) => (
          <div key={transaction.id} style={{ 
            display: 'grid', 
            gridTemplateColumns: '1fr 1fr 1fr 1fr 1fr',
            gap: '0.5rem',
            marginBottom: '0.5rem',
            fontSize: '0.75rem',
            alignItems: 'center'
          }}>
            <div style={{ color: 'white', fontWeight: '600' }}>
              {transaction.symbol}
            </div>
            <div style={{ 
              color: transaction.type === 'BUY' ? '#10b981' : '#ef4444',
              fontWeight: '600'
            }}>
              {transaction.type}
            </div>
            <div style={{ color: '#94a3b8' }}>
              {transaction.quantity}
            </div>
            <div style={{ color: '#94a3b8' }}>
              ${transaction.price.toFixed(2)}
            </div>
            <div style={{ color: '#64748b' }}>
              {new Date(transaction.date).toLocaleDateString()}
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}