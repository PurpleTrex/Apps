import type { Widget } from '@/types/widgets';
import WidgetContainer from './WidgetContainer';

interface PortfolioRiskGaugeWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function PortfolioRiskGaugeWidget({ widget, onEdit, onDelete }: PortfolioRiskGaugeWidgetProps) {
  const riskScore = 32.5;
  const riskLevel = riskScore < 20 ? 'Low' : riskScore < 50 ? 'Medium' : 'High';
  const riskColor = riskScore < 20 ? '#10b981' : riskScore < 50 ? '#f59e0b' : '#ef4444';

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ padding: '1rem', textAlign: 'center' }}>
        <div style={{ position: 'relative', width: '120px', height: '120px', margin: '0 auto 1rem' }}>
          <svg width="120" height="120" style={{ transform: 'rotate(-90deg)' }}>
            <circle
              cx="60"
              cy="60"
              r="50"
              fill="none"
              stroke="rgba(148, 163, 184, 0.2)"
              strokeWidth="8"
            />
            <circle
              cx="60"
              cy="60"
              r="50"
              fill="none"
              stroke={riskColor}
              strokeWidth="8"
              strokeDasharray={`${(riskScore / 100) * 314} 314`}
              strokeLinecap="round"
            />
          </svg>
          <div style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'white' }}>
              {riskScore}
            </div>
            <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Risk Score</div>
          </div>
        </div>
        <div style={{ fontSize: '0.875rem', fontWeight: '600', color: riskColor }}>
          {riskLevel} Risk
        </div>
      </div>
    </WidgetContainer>
  );
}