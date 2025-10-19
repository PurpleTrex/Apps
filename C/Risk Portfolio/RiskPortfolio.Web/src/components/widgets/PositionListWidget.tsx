import { usePortfolios } from '@/hooks/usePortfolios';
import { formatCurrency, formatNumber } from '@/lib/utils';
import WidgetContainer from './WidgetContainer';
import type { Widget } from '@/types/widgets';

interface PositionListWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function PositionListWidget({ widget, onEdit, onDelete }: PositionListWidgetProps) {
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

  // Get all positions from all portfolios
  const positions = portfolios?.flatMap(p =>
    p.positions.map(pos => ({
      symbol: pos.symbol,
      quantity: pos.quantity,
      currentPrice: pos.currentPrice,
      averagePrice: pos.averagePrice,
      marketValue: pos.quantity * pos.currentPrice,
      gainLoss: (pos.currentPrice - pos.averagePrice) * pos.quantity,
      gainLossPercent: ((pos.currentPrice - pos.averagePrice) / pos.averagePrice) * 100
    }))
  ) || [];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', fontSize: '0.75rem', borderCollapse: 'separate', borderSpacing: '0' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.1)' }}>
              <th style={{ textAlign: 'left', padding: '0.5rem', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Symbol</th>
              <th style={{ textAlign: 'right', padding: '0.5rem', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Qty</th>
              <th style={{ textAlign: 'right', padding: '0.5rem', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Price</th>
              <th style={{ textAlign: 'right', padding: '0.5rem', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Value</th>
              <th style={{ textAlign: 'right', padding: '0.5rem', color: '#64748b', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Gain/Loss</th>
            </tr>
          </thead>
          <tbody>
            {positions.map((position, index) => (
              <tr key={`${position.symbol}-${index}`} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.05)' }}>
                <td style={{ padding: '0.75rem 0.5rem', color: 'white', fontWeight: '600' }}>{position.symbol}</td>
                <td style={{ padding: '0.75rem 0.5rem', textAlign: 'right', color: '#cbd5e1' }}>{formatNumber(position.quantity, 0)}</td>
                <td style={{ padding: '0.75rem 0.5rem', textAlign: 'right', color: '#cbd5e1' }}>{formatCurrency(position.currentPrice, 'USD', 2)}</td>
                <td style={{ padding: '0.75rem 0.5rem', textAlign: 'right', color: 'white', fontWeight: '500' }}>{formatCurrency(position.marketValue, 'USD', 0)}</td>
                <td style={{ padding: '0.75rem 0.5rem', textAlign: 'right', fontWeight: '600', color: position.gainLoss >= 0 ? '#34d399' : '#f87171' }}>
                  {position.gainLoss >= 0 ? '+' : ''}{formatCurrency(position.gainLoss, 'USD', 0)}
                  <div style={{ fontSize: '0.7rem', marginTop: '0.125rem' }}>
                    {position.gainLoss >= 0 ? '+' : ''}{formatNumber(position.gainLossPercent, 2)}%
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </WidgetContainer>
  );
}
