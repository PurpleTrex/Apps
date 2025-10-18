import type { PositionResponse } from '@/types/api';
import { formatCurrency, formatNumber } from '@/lib/utils';
import Button from '@/components/ui/Button';

interface PositionsTableProps {
  positions: PositionResponse[];
  baseCurrency: string;
  onRemove: (positionId: string, symbol: string) => void;
}

export default function PositionsTable({
  positions,
  baseCurrency,
  onRemove,
}: PositionsTableProps) {
  if (positions.length === 0) {
    return (
      <div style={{ 
        textAlign: 'center', 
        padding: '2rem', 
        color: '#64748b' 
      }}>
        No positions in this portfolio yet
      </div>
    );
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', fontSize: '0.875rem', color: '#e2e8f0' }}>
        <thead style={{ backgroundColor: 'rgba(15, 23, 42, 0.8)', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.3em', color: '#94a3b8' }}>
          <tr>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'left' }}>Symbol</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Quantity</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Avg Price</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Current Price</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Market Value</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Volatility</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Risk Contribution</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Gain/Loss</th>
            <th style={{ padding: '0.75rem 1rem' }}></th>
          </tr>
        </thead>
        <tbody>
          {positions.map((position) => {
            const gainLoss = (position.currentPrice - position.averagePrice) * position.quantity;
            const gainLossPercent =
              ((position.currentPrice - position.averagePrice) / position.averagePrice) * 100;
            const isProfit = gainLoss >= 0;

            return (
              <tr 
                key={position.id} 
                style={{ 
                  borderBottom: '1px solid rgba(148, 163, 184, 0.6)', 
                  backgroundColor: 'rgba(2, 6, 23, 0.4)', 
                  fontSize: '0.875rem',
                  transition: 'background-color 200ms'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(15, 23, 42, 0.7)'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'rgba(2, 6, 23, 0.4)'}
              >
                <td style={{ padding: '0.75rem 1rem' }}>
                  <span style={{ fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.2em', color: '#f1f5f9' }}>
                    {position.symbol}
                  </span>
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>
                  {formatNumber(position.quantity, 4)}
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>
                  {formatCurrency(position.averagePrice, baseCurrency)}
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>
                  {formatCurrency(position.currentPrice, baseCurrency)}
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right', fontWeight: '600', color: '#f1f5f9' }}>
                  {formatCurrency(position.marketValue, baseCurrency)}
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>
                  {formatNumber(position.volatility * 100, 2)}%
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right', fontWeight: '600', color: '#fbbf24' }}>
                  {formatCurrency(position.riskContribution, baseCurrency)}
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>
                  <div style={{ color: isProfit ? '#34d399' : '#fb7185' }}>
                    <div style={{ fontWeight: '600' }}>
                      {isProfit ? '+' : ''}
                      {formatCurrency(gainLoss, baseCurrency)}
                    </div>
                    <div style={{ fontSize: '0.75rem' }}>
                      ({isProfit ? '+' : ''}
                      {formatNumber(gainLossPercent, 2)}%)
                    </div>
                  </div>
                </td>
                <td style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => onRemove(position.id, position.symbol)}
                  >
                    Remove
                  </Button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}