import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface SimpleWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function PortfolioPerformanceWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981', marginBottom: '0.5rem' }}>+12.4%</div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>YTD Performance</div>
        <div style={{ height: '60px', backgroundColor: 'rgba(16, 185, 129, 0.1)', borderRadius: '0.375rem', marginTop: '0.75rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ color: '#64748b', fontSize: '0.875rem' }}>📈 Performance Chart</span>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function PortfolioRiskGaugeWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#f59e0b', marginBottom: '0.25rem' }}>Medium</div>
        <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '1rem' }}>Risk Level</div>
        <div style={{ 
          width: '80px', 
          height: '40px', 
          backgroundColor: 'rgba(245, 158, 11, 0.2)', 
          borderRadius: '40px 40px 0 0', 
          margin: '0 auto',
          position: 'relative'
        }}>
          <div style={{
            position: 'absolute',
            bottom: '0',
            left: '50%',
            width: '2px',
            height: '25px',
            backgroundColor: '#f59e0b',
            transform: 'translateX(-50%) rotate(15deg)',
            transformOrigin: 'bottom'
          }}></div>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function PositionCardWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
          <div style={{ fontSize: '0.875rem', fontWeight: '600', color: 'white' }}>AAPL</div>
          <div style={{ fontSize: '0.875rem', color: '#10b981' }}>+2.4%</div>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
          <span style={{ color: '#64748b', fontSize: '0.75rem' }}>100 shares</span>
          <span style={{ color: 'white', fontSize: '0.75rem' }}>$18,450</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ color: '#64748b', fontSize: '0.75rem' }}>Avg Cost: $175.20</span>
          <span style={{ color: '#10b981', fontSize: '0.75rem' }}>+$430</span>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function DailyPnLWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#10b981', marginBottom: '0.25rem' }}>+$1,247</div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Today's P&L</div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.75rem', fontSize: '0.75rem' }}>
          <div>
            <div style={{ color: '#64748b' }}>Realized</div>
            <div style={{ color: '#10b981' }}>+$320</div>
          </div>
          <div>
            <div style={{ color: '#64748b' }}>Unrealized</div>
            <div style={{ color: '#10b981' }}>+$927</div>
          </div>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function TransactionsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const transactions = [
    { symbol: 'AAPL', action: 'BUY', shares: 10, price: '$182.50' },
    { symbol: 'MSFT', action: 'SELL', shares: 5, price: '$378.20' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {transactions.map((tx, i) => (
          <div key={i} style={{ 
            display: 'flex', 
            justifyContent: 'space-between',
            marginBottom: '0.75rem',
            padding: '0.5rem',
            backgroundColor: 'rgba(148, 163, 184, 0.05)',
            borderRadius: '0.375rem'
          }}>
            <div>
              <div style={{ color: 'white', fontSize: '0.75rem', fontWeight: '600' }}>{tx.symbol}</div>
              <div style={{ color: tx.action === 'BUY' ? '#10b981' : '#ef4444', fontSize: '0.7rem' }}>
                {tx.action} {tx.shares}
              </div>
            </div>
            <div style={{ color: '#94a3b8', fontSize: '0.75rem' }}>{tx.price}</div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function ValueAtRiskWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#ef4444', marginBottom: '0.25rem' }}>$2,340</div>
        <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.75rem' }}>95% VaR (1 day)</div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem' }}>
          <div>
            <div style={{ color: '#64748b' }}>Confidence</div>
            <div style={{ color: 'white' }}>95%</div>
          </div>
          <div>
            <div style={{ color: '#64748b' }}>Horizon</div>
            <div style={{ color: 'white' }}>1 day</div>
          </div>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function WatchListWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const watchlist = [
    { symbol: 'NVDA', price: '$442.50', change: '+1.8%' },
    { symbol: 'AMD', price: '$108.75', change: '-0.5%' },
    { symbol: 'INTC', price: '$36.20', change: '+2.1%' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {watchlist.map((stock, i) => (
          <div key={i} style={{ 
            display: 'flex', 
            justifyContent: 'space-between',
            marginBottom: '0.5rem'
          }}>
            <div style={{ color: 'white', fontSize: '0.75rem', fontWeight: '600' }}>{stock.symbol}</div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ color: 'white', fontSize: '0.75rem' }}>{stock.price}</div>
              <div style={{ 
                color: stock.change.startsWith('+') ? '#10b981' : '#ef4444', 
                fontSize: '0.7rem' 
              }}>
                {stock.change}
              </div>
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function NewsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const news = [
    { headline: 'Fed signals potential rate cuts', time: '2h ago' },
    { headline: 'Tech stocks rally continues', time: '4h ago' },
    { headline: 'Energy sector shows strength', time: '6h ago' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {news.map((item, i) => (
          <div key={i} style={{ 
            marginBottom: '0.75rem',
            paddingBottom: '0.75rem',
            borderBottom: i < news.length - 1 ? '1px solid rgba(148, 163, 184, 0.1)' : 'none'
          }}>
            <div style={{ color: 'white', fontSize: '0.75rem', lineHeight: '1.4', marginBottom: '0.25rem' }}>
              {item.headline}
            </div>
            <div style={{ color: '#64748b', fontSize: '0.7rem' }}>{item.time}</div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}