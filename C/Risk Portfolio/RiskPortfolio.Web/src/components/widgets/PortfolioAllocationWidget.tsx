import { usePortfolios } from '@/hooks/usePortfolios';
import WidgetContainer from './WidgetContainer';
import type { Widget } from '@/types/widgets';

interface PortfolioAllocationWidgetProps {
  widget: Widget;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function PortfolioAllocationWidget({ widget, onEdit, onDelete }: PortfolioAllocationWidgetProps) {
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

  // Calculate allocation by symbol (or could be grouped by portfolio)
  const allocation = portfolios?.reduce((acc, portfolio) => {
    portfolio.positions.forEach(position => {
      const type = position.symbol;
      const value = position.quantity * position.currentPrice;
      acc[type] = (acc[type] || 0) + value;
    });
    return acc;
  }, {} as Record<string, number>) || {};

  const total = Object.values(allocation).reduce((sum, val) => sum + val, 0);
  const allocationData = Object.entries(allocation).map(([type, value]) => ({
    type,
    value,
    percentage: total > 0 ? (value / total) * 100 : 0
  }));

  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

  return (
    <WidgetContainer title={widget.title} onEdit={onEdit} onDelete={onDelete}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', height: '100%' }}>
        {/* Pie Chart */}
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '200px' }}>
          <svg width="180" height="180" viewBox="0 0 200 200">
            {allocationData.reduce((acc, item, index) => {
              const startAngle = acc.totalAngle;
              const angle = (item.percentage / 100) * 360;
              const endAngle = startAngle + angle;

              const startRad = (startAngle - 90) * (Math.PI / 180);
              const endRad = (endAngle - 90) * (Math.PI / 180);

              const x1 = 100 + 80 * Math.cos(startRad);
              const y1 = 100 + 80 * Math.sin(startRad);
              const x2 = 100 + 80 * Math.cos(endRad);
              const y2 = 100 + 80 * Math.sin(endRad);

              const largeArc = angle > 180 ? 1 : 0;

              const path = `M 100 100 L ${x1} ${y1} A 80 80 0 ${largeArc} 1 ${x2} ${y2} Z`;

              acc.paths.push(
                <path
                  key={item.type}
                  d={path}
                  fill={colors[index % colors.length]}
                  opacity="0.9"
                />
              );

              acc.totalAngle = endAngle;
              return acc;
            }, { paths: [] as React.ReactElement[], totalAngle: 0 }).paths}
          </svg>
        </div>

        {/* Legend */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          {allocationData.map((item, index) => (
            <div key={item.type} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div style={{
                  width: '0.75rem',
                  height: '0.75rem',
                  borderRadius: '0.125rem',
                  backgroundColor: colors[index % colors.length]
                }} />
                <span style={{ fontSize: '0.875rem', color: '#cbd5e1' }}>{item.type}</span>
              </div>
              <span style={{ fontSize: '0.875rem', fontWeight: '600', color: 'white' }}>
                {item.percentage.toFixed(1)}%
              </span>
            </div>
          ))}
        </div>
      </div>
    </WidgetContainer>
  );
}
