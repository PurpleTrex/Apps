import { usePortfolios } from '@/hooks/usePortfolios';
import { formatCurrency, formatNumber } from '@/lib/utils';
import WidgetContainer from './WidgetContainer';
import type { Widget } from '@/types/widgets';

interface PortfolioAllocationWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function TopGainersWidget({ widget, onEdit, onDelete }: PortfolioAllocationWidgetProps) {
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

  // Get all positions and calculate gains
  const positions = portfolios?.flatMap(p =>
    p.positions.map(pos => ({
      symbol: pos.symbol,
      currentPrice: pos.currentPrice,
      averagePrice: pos.averagePrice,
      gainPercent: ((pos.currentPrice - pos.averagePrice) / pos.averagePrice) * 100,
      gainAmount: (pos.currentPrice - pos.averagePrice) * pos.quantity,
      quantity: pos.quantity
    }))
  ) || [];

  // Get top 5 gainers
  const topGainers = positions
    .filter(p => p.gainPercent > 0)
    .sort((a, b) => b.gainPercent - a.gainPercent)
    .slice(0, 5);

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {topGainers.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#64748b', fontSize: '0.875rem', paddingTop: '2rem' }}>
            No gainers found
          </div>
        ) : (
          topGainers.map((position, index) => (
            <div
              key={`${position.symbol}-${index}`}
              style={{
                padding: '0.75rem',
                borderRadius: '0.375rem',
                backgroundColor: 'rgba(16, 185, 129, 0.05)',
                border: '1px solid rgba(16, 185, 129, 0.1)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.375rem' }}>
                <span style={{ fontSize: '0.875rem', fontWeight: '600', color: 'white' }}>
                  {position.symbol}
                </span>
                <span style={{ fontSize: '0.875rem', fontWeight: '600', color: '#34d399' }}>
                  +{formatNumber(position.gainPercent, 2)}%
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                  {formatCurrency(position.currentPrice, 'USD', 2)}
                </span>
                <span style={{ fontSize: '0.75rem', fontWeight: '500', color: '#34d399' }}>
                  +{formatCurrency(position.gainAmount, 'USD', 0)}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </WidgetContainer>
  );
}
