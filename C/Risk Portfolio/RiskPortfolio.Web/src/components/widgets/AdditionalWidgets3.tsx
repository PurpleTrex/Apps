import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface SimpleWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function RebalancingNeedsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const recommendations = [
    { action: 'Sell AAPL', amount: '$5,200', reason: 'Overweight' },
    { action: 'Buy Bonds', amount: '$3,800', reason: 'Underweight' },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {recommendations.map((rec, i) => (
          <div key={i} style={{ marginBottom: '0.75rem', padding: '0.5rem', backgroundColor: 'rgba(59, 130, 246, 0.1)', borderRadius: '0.375rem' }}>
            <div style={{ color: 'white', fontSize: '0.75rem', fontWeight: '600', marginBottom: '0.25rem' }}>
              {rec.action}
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#3b82f6', fontSize: '0.75rem' }}>{rec.amount}</span>
              <span style={{ color: '#64748b', fontSize: '0.7rem' }}>{rec.reason}</span>
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}

export function TaxLossHarvestingWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1rem' }}>
          <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#ef4444' }}>$2,340</div>
          <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Potential Tax Savings</div>
        </div>
        <div style={{ marginBottom: '0.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ color: '#94a3b8', fontSize: '0.75rem' }}>ROKU</span>
            <span style={{ color: '#ef4444', fontSize: '0.75rem' }}>-$1,200</span>
          </div>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function ReturnsCalendarWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(7, 1fr)', 
          gap: '2px',
          marginBottom: '0.5rem'
        }}>
          {Array.from({ length: 21 }, (_, i) => {
            const colors = ['#ef4444', '#f59e0b', '#10b981'];
            return (
              <div key={i} style={{
                width: '16px',
                height: '16px',
                backgroundColor: colors[i % 3],
                borderRadius: '2px',
                opacity: 0.3 + (i % 3) * 0.3
              }}></div>
            );
          })}
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Returns Heatmap</div>
      </div>
    </WidgetContainer>
  );
}

export function CustomChartWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ height: '100px', backgroundColor: 'rgba(148, 163, 184, 0.1)', borderRadius: '0.375rem', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '0.5rem' }}>
          <span style={{ color: '#64748b' }}>📊 Custom Chart</span>
        </div>
        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Configurable chart widget</div>
      </div>
    </WidgetContainer>
  );
}

export function NotesWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ 
          backgroundColor: 'rgba(148, 163, 184, 0.05)', 
          borderRadius: '0.375rem', 
          padding: '0.75rem',
          border: '1px solid rgba(148, 163, 184, 0.1)'
        }}>
          <div style={{ color: '#94a3b8', fontSize: '0.75rem', lineHeight: '1.4' }}>
            Review quarterly earnings for tech holdings. Consider rebalancing if AAPL exceeds 15% allocation.
          </div>
        </div>
        <div style={{ textAlign: 'center', marginTop: '0.75rem' }}>
          <button style={{ 
            fontSize: '0.75rem', 
            color: '#3b82f6', 
            background: 'none', 
            border: 'none',
            cursor: 'pointer'
          }}>
            + Add Note
          </button>
        </div>
      </div>
    </WidgetContainer>
  );
}

export function CalculatorWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        <div style={{ marginBottom: '1rem' }}>
          <input 
            style={{ 
              width: '100%', 
              padding: '0.5rem', 
              backgroundColor: 'rgba(148, 163, 184, 0.1)', 
              border: '1px solid rgba(148, 163, 184, 0.2)',
              borderRadius: '0.375rem',
              color: 'white',
              fontSize: '0.875rem'
            }}
            placeholder="Enter calculation..."
          />
        </div>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(4, 1fr)', 
          gap: '0.25rem' 
        }}>
          {['7', '8', '9', '/', '4', '5', '6', '*', '1', '2', '3', '-', '0', '.', '=', '+'].map((btn) => (
            <button key={btn} style={{
              padding: '0.5rem',
              backgroundColor: 'rgba(148, 163, 184, 0.1)',
              border: '1px solid rgba(148, 163, 184, 0.2)',
              borderRadius: '0.25rem',
              color: '#94a3b8',
              fontSize: '0.75rem',
              cursor: 'pointer'
            }}>
              {btn}
            </button>
          ))}
        </div>
      </div>
    </WidgetContainer>
  );
}

export function AlertsWidget({ widget, onEdit, onDelete }: SimpleWidgetProps) {
  const alerts = [
    { message: 'AAPL above $180', type: 'price', active: true },
    { message: 'Portfolio risk > 30', type: 'risk', active: false },
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem' }}>
        {alerts.map((alert, i) => (
          <div key={i} style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            marginBottom: '0.75rem',
            padding: '0.5rem',
            backgroundColor: alert.active ? 'rgba(16, 185, 129, 0.1)' : 'rgba(148, 163, 184, 0.1)',
            borderRadius: '0.375rem'
          }}>
            <span style={{ color: alert.active ? '#10b981' : '#64748b', fontSize: '0.75rem' }}>
              {alert.message}
            </span>
            <div style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: alert.active ? '#10b981' : '#64748b'
            }}></div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}