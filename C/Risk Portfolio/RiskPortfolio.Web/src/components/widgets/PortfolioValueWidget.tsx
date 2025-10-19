import { usePortfolios } from '@/hooks/usePortfolios';
import { formatCurrency, formatNumber } from '@/lib/utils';
import WidgetContainer from './WidgetContainer';
import type { Widget } from '@/types/widgets';

interface PortfolioValueWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function PortfolioValueWidget({ widget, onEdit, onDelete }: PortfolioValueWidgetProps) {
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
  const periodGain = totalValue * 0.152;
  const periodGainPercent = 15.2;

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', height: '100%', justifyContent: 'center' }}>
        <div>
          <p style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Total Portfolio Value
          </p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: 'white' }}>
            {formatCurrency(totalValue, 'USD', 0)}
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.375rem',
            padding: '0.375rem 0.75rem',
            borderRadius: '0.375rem',
            backgroundColor: 'rgba(16, 185, 129, 0.1)'
          }}>
            <svg style={{ width: '1rem', height: '1rem', color: '#34d399' }} fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
            </svg>
            <span style={{ fontSize: '0.875rem', fontWeight: '600', color: '#34d399' }}>
              +{formatNumber(periodGainPercent, 2)}%
            </span>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#64748b' }}>14 Days</p>
            <p style={{ fontSize: '0.875rem', fontWeight: '600', color: '#34d399' }}>
              +{formatCurrency(periodGain, 'USD', 0)}
            </p>
          </div>
        </div>
      </div>
    </WidgetContainer>
  );
}
