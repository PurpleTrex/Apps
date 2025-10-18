import { useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { usePortfolios } from '@/hooks/usePortfolios';
import Layout from '@/components/Layout';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { formatCurrency, formatNumber } from '@/lib/utils';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

const COLORS = ['#38bdf8', '#22d3ee', '#0ea5e9', '#3b82f6', '#6366f1', '#8b5cf6'];

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    payload: {
      name: string;
      value: number;
      percentage: number;
    };
  }>;
}

const CustomTooltip = ({ active, payload }: CustomTooltipProps) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div style={{
        borderRadius: '0.5rem',
        border: '1px solid #1e293b',
        backgroundColor: 'rgba(15, 23, 42, 0.98)',
        padding: '0.75rem',
        fontSize: '0.75rem',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)'
      }}>
        <p style={{ fontWeight: '600', color: 'white', marginBottom: '0.25rem' }}>{data.name}</p>
        <p style={{ color: '#94a3b8' }}>
          {formatCurrency(data.value, 'USD', 0)} ({formatNumber(data.percentage, 1)}%)
        </p>
      </div>
    );
  }
  return null;
};

export default function Dashboard() {
  const navigate = useNavigate();
  const { data: portfolios, isLoading, error } = usePortfolios();

  const dashboardData = useMemo(() => {
    if (!portfolios || portfolios.length === 0) return null;

    const totalValue = portfolios.reduce((sum, p) => sum + p.totalValue, 0);
    const totalPositions = portfolios.reduce((sum, p) => sum + p.positions.length, 0);
    const totalVar = portfolios.reduce((sum, p) => sum + p.valueAtRisk, 0);
    const avgRiskScore = portfolios.reduce((sum, p) => sum + p.riskScore, 0) / portfolios.length;

    const allocation = portfolios.map(p => ({
      name: p.name,
      value: p.totalValue,
      percentage: (p.totalValue / totalValue) * 100,
      id: p.id,
      riskScore: p.riskScore,
    }));

    const allPositions = portfolios.flatMap(portfolio =>
      portfolio.positions.map(pos => ({
        ...pos,
        portfolioId: portfolio.id,
        portfolioName: portfolio.name,
        gainLoss: (pos.currentPrice - pos.averagePrice) * pos.quantity,
        gainLossPercent: ((pos.currentPrice - pos.averagePrice) / pos.averagePrice) * 100,
      }))
    );

    const topPerformers = [...allPositions].sort((a, b) => b.gainLoss - a.gainLoss).slice(0, 3);
    const bottomPerformers = [...allPositions].sort((a, b) => a.gainLoss - b.gainLoss).slice(0, 3);

    const periodGain = totalValue * 0.152;
    const periodGainPercent = 15.2;

    return {
      totalValue,
      totalPositions,
      totalVar,
      avgRiskScore,
      periodGain,
      periodGainPercent,
      allocation,
      topPerformers,
      bottomPerformers,
      portfolios,
    };
  }, [portfolios]);

  if (isLoading) {
    return (
      <Layout>
        <div style={{ display: 'flex', minHeight: 'calc(100vh - 3.5rem)', alignItems: 'center', justifyContent: 'center' }}>
          <LoadingSpinner size="lg" />
        </div>
      </Layout>
    );
  }

  if (error || !dashboardData) {
    return (
      <Layout>
        <div style={{ display: 'flex', minHeight: 'calc(100vh - 3.5rem)', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ fontSize: '0.875rem', color: '#fb7185' }}>Failed to load dashboard data</div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      {/* Main Content - Full Width */}
      <div style={{ 
        maxWidth: '1400px',
        margin: '0 auto',
        padding: '1.5rem'
      }}>
          <div style={{ marginBottom: '1.5rem' }}>
            <h1 style={{ fontSize: '1.125rem', color: 'white', marginBottom: '0.5rem', fontWeight: 'bold' }}>Mein Vermögen</h1>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.625rem' }}>
              <span style={{ fontSize: '1.875rem', color: 'white', fontWeight: 'bold' }}>
                {formatCurrency(dashboardData.totalValue, 'USD', 0)}
              </span>
              <span style={{ display: 'flex', alignItems: 'center', fontWeight: '600', gap: '0.25rem', fontSize: '0.75rem', color: '#34d399' }}>
                <svg style={{ height: '0.75rem', width: '0.75rem' }} fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
                +{formatNumber(dashboardData.periodGainPercent, 2)}%
              </span>
            </div>
          </div>

          {/* Stats Grid */}
          <div style={{ 
            display: 'grid',
            marginBottom: '2rem', 
            gridTemplateColumns: 'repeat(4, 1fr)', 
            gap: '1rem' 
          }}>
            <div>
              <p style={{ fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', marginBottom: '0.25rem', fontWeight: '500', textTransform: 'uppercase' }}>Portfolios</p>
              <p style={{ fontSize: '1.25rem', color: 'white', fontWeight: 'bold' }}>{dashboardData.portfolios.length}</p>
            </div>
            <div>
              <p style={{ fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', marginBottom: '0.25rem', fontWeight: '500', textTransform: 'uppercase' }}>Assets</p>
              <p style={{ fontSize: '1.25rem', color: 'white', fontWeight: 'bold' }}>{dashboardData.totalPositions}</p>
            </div>
            <div>
              <p style={{ fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', marginBottom: '0.25rem', fontWeight: '500', textTransform: 'uppercase' }}>Avg Risk</p>
              <p style={{ fontSize: '1.25rem', color: '#38bdf8', fontWeight: 'bold' }}>{formatNumber(dashboardData.avgRiskScore, 1)}</p>
            </div>
            <div>
              <p style={{ fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', marginBottom: '0.25rem', fontWeight: '500', textTransform: 'uppercase' }}>VaR</p>
              <p style={{ fontSize: '1.25rem', color: 'white', fontWeight: 'bold' }}>{formatCurrency(dashboardData.totalVar, 'USD', 0)}</p>
            </div>
          </div>

          {/* Section Title */}
          <h3 style={{ marginBottom: '1rem', fontSize: '10px', letterSpacing: '0.1em', color: '#64748b', fontWeight: 'bold', textTransform: 'uppercase' }}>Assetklassen</h3>

          {/* Donut Chart */}
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '2rem' }}>
            <div style={{ position: 'relative', width: 360, height: 360 }}>
              <ResponsiveContainer>
                <PieChart>
                  <Pie
                    data={dashboardData.allocation}
                    cx="50%"
                    cy="50%"
                    innerRadius={100}
                    outerRadius={150}
                    paddingAngle={2}
                    dataKey="value"
                    strokeWidth={0}
                  >
                    {dashboardData.allocation.map((_, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={COLORS[index % COLORS.length]}
                        style={{ cursor: 'pointer', transition: 'opacity 200ms', opacity: 1 }}
                        onClick={() => navigate(`/portfolios/${dashboardData.allocation[index].id}`)}
                        onMouseEnter={(e) => e.currentTarget.style.opacity = '0.7'}
                        onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                      />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                </PieChart>
              </ResponsiveContainer>
              <div style={{ 
                position: 'absolute', 
                inset: 0, 
                display: 'flex', 
                flexDirection: 'column', 
                alignItems: 'center', 
                justifyContent: 'center', 
                pointerEvents: 'none' 
              }}>
                <p style={{ fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', marginBottom: '0.25rem', textTransform: 'uppercase' }}>Total Value</p>
                <p style={{ fontSize: '2.25rem', color: 'white', fontWeight: 'bold' }}>100%</p>
                <p style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>{formatCurrency(dashboardData.totalValue, 'USD', 0)}</p>
              </div>
            </div>
          </div>

          {/* Portfolio Cards */}
          <div style={{ 
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)', 
            gap: '0.625rem' 
          }}>
            {dashboardData.allocation.map((item, index) => (
              <div
                key={item.id}
                style={{ 
                  cursor: 'pointer',
                  borderLeft: `2px solid ${COLORS[index % COLORS.length]}`,
                  padding: '0.625rem 0.75rem',
                  backgroundColor: 'rgba(18, 22, 31, 0.6)',
                  transition: 'background-color 200ms'
                }}
                onClick={() => navigate(`/portfolios/${item.id}`)}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(26, 31, 46, 0.6)'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'rgba(18, 22, 31, 0.6)'}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.375rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                    <div style={{ height: '0.375rem', width: '0.375rem', borderRadius: '50%', backgroundColor: COLORS[index % COLORS.length] }} />
                    <span style={{ fontSize: '0.75rem', color: '#cbd5e1', fontWeight: '600' }}>{item.name}</span>
                  </div>
                  <span style={{ fontSize: '10px', color: '#475569', fontWeight: '500' }}>
                    RISK {formatNumber(item.riskScore, 1)}
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
                  <span style={{ fontSize: '1.125rem', color: 'white', fontWeight: 'bold' }}>{formatNumber(item.percentage, 1)}%</span>
                  <span style={{ fontSize: '10px', color: '#475569', fontWeight: '500' }}>{formatCurrency(item.value, 'USD', 0)}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Divider */}
          <div style={{ height: '2rem' }} />
          {/* Top Performers */}
          <div style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '10px', letterSpacing: '0.1em', color: '#64748b', fontWeight: 'bold', textTransform: 'uppercase' }}>Top Performers</h3>
              <span style={{ fontSize: '9px', letterSpacing: '0.025em', color: '#34d399', fontWeight: 'bold', textTransform: 'uppercase' }}>Performance %</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {dashboardData.topPerformers.map((position) => (
                <div key={position.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderRadius: '0.5rem', padding: '0.625rem', backgroundColor: 'rgba(15, 20, 25, 0.5)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '0.375rem', height: '2rem', width: '2rem', backgroundColor: 'rgba(16, 185, 129, 0.1)' }}>
                      <span style={{ fontSize: '10px', color: '#34d399', fontWeight: 'bold' }}>{position.symbol.substring(0, 2)}</span>
                    </div>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: 'white', lineHeight: '1.25', fontWeight: 'bold' }}>{position.symbol}</p>
                      <p style={{ fontSize: '10px', color: '#475569', lineHeight: '1.25' }}>{position.quantity.toFixed(0)} shares</p>
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <p style={{ fontSize: '0.75rem', color: 'white', lineHeight: '1.25', fontWeight: 'bold' }}>{formatCurrency(position.marketValue, 'USD', 0)}</p>
                    <p style={{ fontSize: '10px', color: '#34d399', lineHeight: '1.25', fontWeight: 'bold' }}>+{formatNumber(Math.abs(position.gainLossPercent), 2)}%</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Bottom Performers */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '10px', letterSpacing: '0.1em', color: '#64748b', fontWeight: 'bold', textTransform: 'uppercase' }}>Bottom Performers</h3>
              <span style={{ fontSize: '9px', letterSpacing: '0.025em', color: '#fb7185', fontWeight: 'bold', textTransform: 'uppercase' }}>Performance %</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {dashboardData.bottomPerformers.map((position) => (
                <div key={position.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderRadius: '0.5rem', padding: '0.625rem', backgroundColor: 'rgba(15, 20, 25, 0.5)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '0.375rem', height: '2rem', width: '2rem', backgroundColor: 'rgba(244, 63, 94, 0.1)' }}>
                      <span style={{ fontSize: '10px', color: '#fb7185', fontWeight: 'bold' }}>{position.symbol.substring(0, 1)}</span>
                    </div>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: 'white', lineHeight: '1.25', fontWeight: 'bold' }}>{position.symbol}</p>
                      <p style={{ fontSize: '10px', color: '#475569', lineHeight: '1.25' }}>{position.quantity.toFixed(0)} shares</p>
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <p style={{ fontSize: '0.75rem', color: 'white', lineHeight: '1.25', fontWeight: 'bold' }}>{formatCurrency(position.marketValue, 'USD', 0)}</p>
                    <p style={{ fontSize: '10px', color: '#fb7185', lineHeight: '1.25', fontWeight: 'bold' }}>{formatNumber(position.gainLossPercent, 2)}%</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN */}
        <div style={{ 
          backgroundColor: '#0D1117',
          overflowY: 'auto',
          paddingLeft: '1.25rem', 
          paddingRight: '1.25rem', 
          paddingTop: '1.5rem', 
          paddingBottom: '1.5rem' 
        }}>
          {/* Total Value Card */}
          <div style={{ 
            marginBottom: '1.25rem',
            borderRadius: '0.5rem',
            border: '1px solid rgba(148, 163, 184, 0.5)', 
            backgroundColor: 'rgba(18, 22, 31, 0.4)',
            padding: '1rem'
          }}>
            <p style={{ marginBottom: '0.25rem', fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', fontWeight: 'bold', textTransform: 'uppercase' }}>Total Value</p>
            <p style={{ marginBottom: '0.625rem', fontSize: '1.5rem', color: 'white', fontWeight: 'bold' }}>{formatCurrency(dashboardData.totalValue, 'USD', 0)}</p>
            <div style={{ 
              display: 'flex',
              alignItems: 'center',
              borderRadius: '0.25rem',
              marginBottom: '0.375rem', 
              gap: '0.375rem', 
              backgroundColor: 'rgba(16, 185, 129, 0.1)', 
              paddingLeft: '0.5rem', 
              paddingRight: '0.5rem', 
              paddingTop: '0.375rem', 
              paddingBottom: '0.375rem' 
            }}>
              <svg style={{ height: '0.75rem', width: '0.75rem', color: '#34d399' }} fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
              </svg>
              <span style={{ fontSize: '10px', color: '#34d399', fontWeight: 'bold' }}>+{formatNumber(dashboardData.periodGainPercent, 2)}%</span>
              <span style={{ fontSize: '10px', color: '#475569', fontWeight: '500' }}>14 DAYS</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: '#34d399', fontWeight: 'bold' }}>+{formatCurrency(dashboardData.periodGain, 'USD', 0)}</div>
          </div>

          {/* Action Buttons */}
          <div style={{ marginBottom: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <Link
              to="/portfolios/new"
              style={{ 
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                borderRadius: '0.5rem',
                fontWeight: 'bold',
                gap: '0.375rem',
                backgroundColor: '#0ea5e9',
                paddingLeft: '0.75rem',
                paddingRight: '0.75rem',
                paddingTop: '0.625rem',
                paddingBottom: '0.625rem',
                fontSize: '0.75rem',
                color: 'white',
                textDecoration: 'none',
                transition: 'background-color 200ms'
              }}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0284c7'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#0ea5e9'}
            >
              <svg style={{ height: '0.875rem', width: '0.875rem' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
              </svg>
              <span style={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}>New Portfolio</span>
            </Link>
            <button 
              style={{ 
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                borderRadius: '0.5rem',
                border: '1px solid rgba(51, 65, 85, 0.5)',
                fontWeight: 'bold',
                gap: '0.375rem',
                backgroundColor: 'rgba(30, 41, 59, 0.3)',
                paddingLeft: '0.75rem',
                paddingRight: '0.75rem',
                paddingTop: '0.625rem',
                paddingBottom: '0.625rem',
                fontSize: '0.75rem',
                color: '#94a3b8',
                cursor: 'pointer',
                transition: 'background-color 200ms'
              }}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(30, 41, 59, 0.5)'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'rgba(30, 41, 59, 0.3)'}
            >
              <svg style={{ height: '0.875rem', width: '0.875rem' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <span style={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}>Run Analytics</span>
            </button>
          </div>
        </div>
    </Layout>
  );
}