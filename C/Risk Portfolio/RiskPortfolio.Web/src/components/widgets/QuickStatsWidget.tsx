import { usePortfolios } from '@/hooks/usePortfolios';
import { formatCurrency, formatNumber } from '@/lib/utils';
import WidgetContainer from './WidgetContainer';
import type { Widget } from '@/types/widgets';

interface QuickStatsWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function QuickStatsWidget({ widget, onEdit, onDelete }: QuickStatsWidgetProps) {
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

  const totalValue = portfolios?.reduce((sum, p) => sum + p.totalValue, 0) || 0;
  const totalPositions = portfolios?.reduce((sum, p) => sum + p.positions.length, 0) || 0;
  const avgRiskScore = portfolios && portfolios.length > 0
    ? portfolios.reduce((sum, p) => sum + p.riskScore, 0) / portfolios.length
    : 0;

  // Calculate total gain/loss
  const totalGainLoss = portfolios?.reduce((sum, p) => {
    const portfolioGainLoss = p.positions.reduce((pSum, pos) => {
      return pSum + ((pos.currentPrice - pos.averagePrice) * pos.quantity);
    }, 0);
    return sum + portfolioGainLoss;
  }, 0) || 0;

  const stats = [
    { label: 'Total Value', value: formatCurrency(totalValue, 'USD', 0), color: '#3b82f6' },
    { label: 'Total Positions', value: totalPositions.toString(), color: '#10b981' },
    { label: 'Avg Risk Score', value: formatNumber(avgRiskScore, 1), color: '#f59e0b' },
    { label: 'Total P&L', value: formatCurrency(totalGainLoss, 'USD', 0), color: totalGainLoss >= 0 ? '#34d399' : '#f87171' }
  ];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', height: '100%' }}>
        {stats.map((stat, index) => (
          <div
            key={index}
            style={{
              padding: '1rem',
              borderRadius: '0.375rem',
              backgroundColor: 'rgba(148, 163, 184, 0.05)',
              border: '1px solid rgba(148, 163, 184, 0.1)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center'
            }}
          >
            <div style={{ fontSize: '0.7rem', color: '#64748b', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              {stat.label}
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: stat.color }}>
              {stat.value}
            </div>
          </div>
        ))}
      </div>
    </WidgetContainer>
  );
}
