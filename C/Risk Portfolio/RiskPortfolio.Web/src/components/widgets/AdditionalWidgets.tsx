import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

const simpleWidgets = {
  PerformanceChart: () => (
    <div style={{ padding: '1rem', textAlign: 'center' }}>
      <div style={{ height: '120px', backgroundColor: 'rgba(59, 130, 246, 0.1)', borderRadius: '0.375rem', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '0.5rem' }}>
        <span style={{ color: '#64748b' }}>📈 Performance Chart</span>
      </div>
      <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Historical performance visualization</div>
    </div>
  ),
  
  MarketOverview: () => (
    <div style={{ padding: '1rem' }}>
      {[
        { name: 'S&P 500', value: '4,327.78', change: '+0.85%' },
        { name: 'NASDAQ', value: '13,431.34', change: '+1.12%' },
        { name: 'DOW', value: '33,745.69', change: '+0.43%' },
      ].map((index) => (
        <div key={index.name} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
          <span style={{ color: '#94a3b8', fontSize: '0.75rem' }}>{index.name}</span>
          <div style={{ textAlign: 'right' }}>
            <div style={{ color: 'white', fontSize: '0.75rem' }}>{index.value}</div>
            <div style={{ color: '#10b981', fontSize: '0.7rem' }}>{index.change}</div>
          </div>
        </div>
      ))}
    </div>
  ),

  MarketNews: () => (
    <div style={{ padding: '1rem' }}>
      {[
        'Fed signals potential rate cuts',
        'Tech stocks rally continues',
        'Energy sector shows strength'
      ].map((headline, i) => (
        <div key={i} style={{ marginBottom: '0.75rem', paddingBottom: '0.75rem', borderBottom: i < 2 ? '1px solid rgba(148, 163, 184, 0.1)' : 'none' }}>
          <div style={{ color: 'white', fontSize: '0.75rem', lineHeight: '1.4' }}>{headline}</div>
          <div style={{ color: '#64748b', fontSize: '0.7rem', marginTop: '0.25rem' }}>2 hours ago</div>
        </div>
      ))}
    </div>
  ),

  AssetAllocation: () => (
    <div style={{ padding: '1rem', textAlign: 'center' }}>
      <div style={{ marginBottom: '1rem' }}>
        {[
          { label: 'Stocks', percent: 70, color: '#3b82f6' },
          { label: 'Bonds', percent: 20, color: '#10b981' },
          { label: 'Cash', percent: 10, color: '#f59e0b' }
        ].map((item) => (
          <div key={item.label} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: item.color }}></div>
              <span style={{ color: '#94a3b8', fontSize: '0.75rem' }}>{item.label}</span>
            </div>
            <span style={{ color: 'white', fontSize: '0.75rem', fontWeight: '600' }}>{item.percent}%</span>
          </div>
        ))}
      </div>
    </div>
  ),

  Dividends: () => (
    <div style={{ padding: '1rem' }}>
      <div style={{ textAlign: 'center', marginBottom: '1rem' }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#10b981' }}>$1,247</div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>This Quarter</div>
      </div>
      {[
        { symbol: 'AAPL', amount: '$0.24', date: 'Nov 15' },
        { symbol: 'MSFT', amount: '$0.75', date: 'Dec 12' },
      ].map((div) => (
        <div key={div.symbol} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
          <span style={{ color: 'white', fontSize: '0.75rem' }}>{div.symbol}</span>
          <span style={{ color: '#10b981', fontSize: '0.75rem' }}>{div.amount}</span>
          <span style={{ color: '#64748b', fontSize: '0.7rem' }}>{div.date}</span>
        </div>
      ))}
    </div>
  )
};

interface SimpleWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function PerformanceChartWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      {simpleWidgets.PerformanceChart()}
    </WidgetContainer>
  );
}

export function MarketOverviewWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      {simpleWidgets.MarketOverview()}
    </WidgetContainer>
  );
}

export function MarketNewsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      {simpleWidgets.MarketNews()}
    </WidgetContainer>
  );
}

export function AssetAllocationWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      {simpleWidgets.AssetAllocation()}
    </WidgetContainer>
  );
}

export function DividendsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      {simpleWidgets.Dividends()}
    </WidgetContainer>
  );
}