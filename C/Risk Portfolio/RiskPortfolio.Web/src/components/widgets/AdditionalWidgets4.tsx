import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface SimpleWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function MarketSummaryWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const marketData = [
    { index: 'S&P 500', value: '4,378.38', change: '+0.85%', color: '#10b981' },
    { index: 'NASDAQ', value: '13,567.98', change: '+1.24%', color: '#10b981' },
    { index: 'DOW', value: '34,258.32', change: '+0.43%', color: '#10b981' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {marketData.map((market, i) => (
          <div key={i} style={{ 
            display: 'flex', 
            justifyContent: 'space-between',
            marginBottom: '0.75rem',
            alignItems: 'center'
          }}>
            <div>
              <div style={{ color: 'white', fontSize: '0.875rem', fontWeight: '500' }}>
                {market.index}
              </div>
              <div style={{ color: '#94a3b8', fontSize: '0.75rem' }}>
                {market.value}
              </div>
            </div>
            <div style={{ color: market.color, fontSize: '0.875rem', fontWeight: '600' }}>
              {market.change}
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function AssetClassBreakdownWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const assetClasses = [
    { name: 'Stocks', percentage: 65, color: '#3b82f6' },
    { name: 'Bonds', percentage: 25, color: '#10b981' },
    { name: 'REITs', percentage: 7, color: '#f59e0b' },
    { name: 'Cash', percentage: 3, color: '#64748b' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {assetClasses.map((asset, i) => (
          <div key={i} style={{ marginBottom: '0.75rem' }}>
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              marginBottom: '0.25rem'
            }}>
              <span style={{ color: '#94a3b8', fontSize: '0.75rem' }}>{asset.name}</span>
              <span style={{ color: 'white', fontSize: '0.75rem' }}>{asset.percentage}%</span>
            </div>
            <div style={{ 
              width: '100%',
              height: '4px',
              backgroundColor: 'rgba(148, 163, 184, 0.2)',
              borderRadius: '2px'
            }}>
              <div style={{
                width: `${asset.percentage}%`,
                height: '100%',
                backgroundColor: asset.color,
                borderRadius: '2px'
              }}></div>
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function DrawdownChartWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ 
          height: '80px', 
          backgroundColor: 'rgba(148, 163, 184, 0.1)', 
          borderRadius: '0.375rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '0.75rem'
        }}>
          <span style={{ color: '#64748b', fontSize: '0.875rem' }}>📉 Drawdown Chart</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem' }}>
          <span style={{ color: '#64748b' }}>Max Drawdown:</span>
          <span style={{ color: '#ef4444' }}>-8.5%</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginTop: '0.25rem' }}>
          <span style={{ color: '#64748b' }}>Current:</span>
          <span style={{ color: '#f59e0b' }}>-2.1%</span>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function MonthlyReturnsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(4, 1fr)', 
          gap: '4px',
          marginBottom: '0.5rem'
        }}>
          {Array.from({ length: 12 }, (_, i) => {
            const colors = ['#ef4444', '#f59e0b', '#10b981'];
            const returns = ['-2.1%', '+1.4%', '+3.2%', '-0.8%', '+2.7%', '+1.9%', '-1.3%', '+2.1%', '+0.5%', '+1.8%', '-0.9%', '+2.4%'];
            return (
              <div key={i} style={{
                padding: '4px',
                fontSize: '0.6rem',
                textAlign: 'center',
                backgroundColor: colors[i % 3],
                borderRadius: '2px',
                opacity: 0.3 + (i % 3) * 0.3,
                color: 'white'
              }}>
                {returns[i]}
              </div>
            );
          })}
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b', textAlign: 'center' }}>Monthly Returns Heatmap</div>
      </div>
    </WidgetContainer>
  );
}

export function PerformanceAttributionWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const attributions = [
    { factor: 'Stock Selection', contribution: '+1.2%' },
    { factor: 'Asset Allocation', contribution: '+0.8%' },
    { factor: 'Market Timing', contribution: '-0.3%' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {attributions.map((attr, i) => (
          <div key={i} style={{ 
            display: 'flex', 
            justifyContent: 'space-between',
            marginBottom: '0.5rem'
          }}>
            <span style={{ color: '#94a3b8', fontSize: '0.75rem' }}>{attr.factor}</span>
            <span style={{ 
              color: attr.contribution.startsWith('+') ? '#10b981' : '#ef4444', 
              fontSize: '0.75rem' 
            }}>
              {attr.contribution}
            </span>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}