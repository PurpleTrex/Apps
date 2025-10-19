import { usePortfolios } from '@/hooks/usePortfolios';
import { formatNumber } from '@/lib/utils';
import WidgetContainer from './WidgetContainer';
import type { Widget } from '@/types/widgets';

interface RiskMetricsWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function RiskMetricsWidget({ widget, onEdit, onDelete }: RiskMetricsWidgetProps) {
  const { data: portfolios, isLoading } = usePortfolios();

  if (isLoading) {
    return (
      <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
          <div style={{ color: '#64748b', fontSize: '0.875rem' }}>Loading...</div>
        </div>
      </WidgetContainer>
    );
  }

  const avgRiskScore = portfolios && portfolios.length > 0
    ? portfolios.reduce((sum, p) => sum + p.riskScore, 0) / portfolios.length
    : 0;

  const avgVolatility = portfolios && portfolios.length > 0
    ? portfolios.reduce((sum, p) => {
        const portfolioVolatility = p.positions.reduce((pSum, pos) => pSum + pos.volatility, 0) / (p.positions.length || 1);
        return sum + portfolioVolatility;
      }, 0) / portfolios.length
    : 0;

  const totalVaR = portfolios?.reduce((sum, p) => sum + p.valueAtRisk, 0) || 0;

  const getRiskColor = (score: number) => {
    if (score < 30) return '#10b981';
    if (score < 60) return '#f59e0b';
    return '#ef4444';
  };

  const getRiskLabel = (score: number) => {
    if (score < 30) return 'Low Risk';
    if (score < 60) return 'Moderate Risk';
    return 'High Risk';
  };

  const riskColor = getRiskColor(avgRiskScore);

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', height: '100%', justifyContent: 'center' }}>
        {/* Risk Score Gauge */}
        <div style={{ textAlign: 'center' }}>
          <div style={{ position: 'relative', display: 'inline-block' }}>
            <svg width="120" height="120" viewBox="0 0 120 120">
              {/* Background circle */}
              <circle
                cx="60"
                cy="60"
                r="50"
                fill="none"
                stroke="rgba(148, 163, 184, 0.1)"
                strokeWidth="10"
              />
              {/* Progress circle */}
              <circle
                cx="60"
                cy="60"
                r="50"
                fill="none"
                stroke={riskColor}
                strokeWidth="10"
                strokeDasharray={`${(avgRiskScore / 100) * 314} 314`}
                strokeLinecap="round"
                transform="rotate(-90 60 60)"
              />
            </svg>
            <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
              <div style={{ fontSize: '1.75rem', fontWeight: 'bold', color: riskColor }}>
                {formatNumber(avgRiskScore, 0)}
              </div>
              <div style={{ fontSize: '0.7rem', color: '#64748b' }}>RISK</div>
            </div>
          </div>
          <div style={{ marginTop: '0.5rem', fontSize: '0.875rem', fontWeight: '600', color: riskColor }}>
            {getRiskLabel(avgRiskScore)}
          </div>
        </div>

        {/* Risk Metrics */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem', borderRadius: '0.25rem', backgroundColor: 'rgba(148, 163, 184, 0.05)' }}>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Avg Volatility</span>
            <span style={{ fontSize: '0.875rem', fontWeight: '600', color: 'white' }}>
              {formatNumber(avgVolatility * 100, 1)}%
            </span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem', borderRadius: '0.25rem', backgroundColor: 'rgba(148, 163, 184, 0.05)' }}>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Value at Risk</span>
            <span style={{ fontSize: '0.875rem', fontWeight: '600', color: '#ef4444' }}>
              ${formatNumber(totalVaR, 0)}
            </span>
          </div>
        </div>
      </div>
    </WidgetContainer>
  );
}
