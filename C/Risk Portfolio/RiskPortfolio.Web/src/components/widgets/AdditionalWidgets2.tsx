import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface SimpleWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function SectorPerformanceWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const sectors = [
    { name: 'Technology', performance: '+2.4%', color: '#10b981' },
    { name: 'Healthcare', performance: '+1.8%', color: '#10b981' },
    { name: 'Energy', performance: '-0.9%', color: '#ef4444' },
    { name: 'Finance', performance: '+0.7%', color: '#10b981' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {sectors.map((sector) => (
          <div key={sector.name} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
            <span style={{ color: '#94a3b8', fontSize: '0.75rem' }}>{sector.name}</span>
            <span style={{ color: sector.color, fontSize: '0.75rem', fontWeight: '600' }}>
              {sector.performance}
            </span>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function EconomicCalendarWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const events = [
    { event: 'Fed Meeting', date: 'Nov 2', impact: 'High' },
    { event: 'CPI Report', date: 'Nov 14', impact: 'Medium' },
    { event: 'GDP Data', date: 'Nov 28', impact: 'High' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {events.map((event, i) => (
          <div key={i} style={{ marginBottom: '0.75rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
              <span style={{ color: 'white', fontSize: '0.75rem' }}>{event.event}</span>
              <span style={{ color: '#64748b', fontSize: '0.7rem' }}>{event.date}</span>
            </div>
            <div style={{ 
              color: event.impact === 'High' ? '#ef4444' : '#f59e0b', 
              fontSize: '0.7rem',
              fontWeight: '600'
            }}>
              {event.impact} Impact
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function CorrelationMatrixWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(4, 1fr)', 
          gap: '2px',
          marginBottom: '0.5rem'
        }}>
          {Array.from({ length: 16 }, (_, i) => (
            <div key={i} style={{
              width: '20px',
              height: '20px',
              backgroundColor: `rgba(59, 130, 246, ${0.2 + (i % 5) * 0.2})`,
              borderRadius: '2px'
            }}></div>
          ))}
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Asset Correlation Heatmap</div>
      </div>
    </WidgetContainer>
  );
}

export function VolatilityChartWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#f59e0b', marginBottom: '0.5rem' }}>
          18.4%
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '1rem' }}>
          Current Volatility
        </div>
        <div style={{ height: '60px', backgroundColor: 'rgba(245, 158, 11, 0.1)', borderRadius: '0.375rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ color: '#64748b' }}>📊 Volatility Trend</span>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function DiversificationScoreWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#10b981', marginBottom: '0.5rem' }}>
          8.2
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem' }}>
          Diversification Score
        </div>
        <div style={{ fontSize: '0.875rem', color: '#10b981', fontWeight: '600' }}>
          Well Diversified
        </div>
      </div>
    </WidgetContainer>
  );
}