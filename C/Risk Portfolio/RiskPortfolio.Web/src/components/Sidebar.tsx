import { Link } from 'react-router-dom';
import { formatCurrency, formatNumber } from '@/lib/utils';

interface Portfolio {
  id: string;
  name: string;
  totalValue: number;
  riskScore: number;
  positions: Array<{ id: string }>;
}

interface SidebarProps {
  totalValue: number;
  periodGain: number;
  periodGainPercent: number;
  portfolios: Portfolio[];
  showDashboardLink?: boolean; // Show "Back to Dashboard" link when on other pages
}

export default function Sidebar({ totalValue, periodGain, periodGainPercent, portfolios, showDashboardLink = false }: SidebarProps) {
  return (
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
        <p style={{ marginBottom: '0.625rem', fontSize: '1.5rem', color: 'white', fontWeight: 'bold' }}>{formatCurrency(totalValue, 'USD', 0)}</p>
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
          <span style={{ fontSize: '10px', color: '#34d399', fontWeight: 'bold' }}>+{formatNumber(periodGainPercent, 2)}%</span>
          <span style={{ fontSize: '10px', color: '#475569', fontWeight: '500' }}>14 DAYS</span>
        </div>
        <div style={{ fontSize: '0.75rem', color: '#34d399', fontWeight: 'bold' }}>+{formatCurrency(periodGain, 'USD', 0)}</div>
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

      {/* Portfolio List */}
      <div>
        <h3 style={{ marginBottom: '0.75rem', fontSize: '10px', letterSpacing: '0.1em', color: '#64748b', fontWeight: 'bold', textTransform: 'uppercase' }}>My Portfolios</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
          {/* Dashboard Link (when on other pages) */}
          {showDashboardLink && (
            <Link
              to="/"
              style={{ 
                display: 'block',
                borderRadius: '0.5rem',
                border: '1px solid rgba(148, 163, 184, 0.5)',
                backgroundColor: 'rgba(14, 165, 233, 0.1)',
                padding: '0.75rem',
                textDecoration: 'none',
                transition: 'all 200ms'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = 'rgba(14, 165, 233, 0.7)';
                e.currentTarget.style.backgroundColor = 'rgba(14, 165, 233, 0.15)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = 'rgba(148, 163, 184, 0.5)';
                e.currentTarget.style.backgroundColor = 'rgba(14, 165, 233, 0.1)';
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                <div 
                  style={{ 
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: '0.25rem',
                    height: '1.75rem',
                    width: '1.75rem',
                    background: 'linear-gradient(to bottom right, #0ea5e9, #3b82f6)'
                  }}
                >
                  <svg style={{ height: '0.875rem', width: '0.875rem', color: 'white' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                  </svg>
                </div>
                <p style={{ fontSize: '0.75rem', color: '#0ea5e9', lineHeight: '1.25', fontWeight: 'bold' }}>← Back to Dashboard</p>
              </div>
            </Link>
          )}
          
          {portfolios.map((portfolio) => {
            const riskColor =
              portfolio.riskScore > 7
                ? { color: '#fb7185', backgroundColor: 'rgba(244, 63, 94, 0.1)' }
                : portfolio.riskScore > 4
                ? { color: '#fbbf24', backgroundColor: 'rgba(251, 191, 36, 0.1)' }
                : { color: '#34d399', backgroundColor: 'rgba(16, 185, 129, 0.1)' };

            return (
              <Link
                key={portfolio.id}
                to={`/portfolios/${portfolio.id}`}
                style={{ 
                  display: 'block',
                  borderRadius: '0.5rem',
                  border: '1px solid rgba(148, 163, 184, 0.5)',
                  backgroundColor: 'rgba(18, 22, 31, 0.4)',
                  padding: '0.75rem',
                  textDecoration: 'none',
                  transition: 'all 200ms'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = 'rgba(148, 163, 184, 0.7)';
                  e.currentTarget.style.backgroundColor = 'rgba(26, 31, 46, 0.4)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = 'rgba(148, 163, 184, 0.5)';
                  e.currentTarget.style.backgroundColor = 'rgba(18, 22, 31, 0.4)';
                }}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '0.625rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div 
                      style={{ 
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        borderRadius: '0.25rem',
                        height: '1.75rem',
                        width: '1.75rem',
                        background: 'linear-gradient(to bottom right, #38bdf8, #2563eb)'
                      }}
                    >
                      <svg style={{ height: '0.875rem', width: '0.875rem', color: 'white' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                      </svg>
                    </div>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: 'white', lineHeight: '1.25', fontWeight: 'bold' }}>{portfolio.name}</p>
                      <p style={{ fontSize: '10px', color: '#475569', lineHeight: '1.25', fontWeight: '500' }}>USD</p>
                    </div>
                  </div>
                  <span style={{ fontSize: '0.75rem', color: '#34d399', fontWeight: 'bold' }}>+8.00%</span>
                </div>

                <div style={{ marginBottom: '0.5rem' }}>
                  <p style={{ fontSize: '1.125rem', color: 'white', fontWeight: 'bold' }}>{formatCurrency(portfolio.totalValue, 'USD', 0)}</p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                    <span 
                      style={{ 
                        borderRadius: '0.25rem',
                        paddingLeft: '0.375rem',
                        paddingRight: '0.375rem',
                        paddingTop: '0.125rem',
                        paddingBottom: '0.125rem',
                        fontSize: '9px',
                        letterSpacing: '0.025em',
                        fontWeight: 'bold',
                        textTransform: 'uppercase',
                        color: riskColor.color,
                        backgroundColor: riskColor.backgroundColor
                      }}
                    >
                      Risk
                    </span>
                    <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 'bold' }}>{formatNumber(portfolio.riskScore, 1)}</span>
                  </div>
                  <p style={{ fontSize: '10px', color: '#475569', fontWeight: '500' }}>{portfolio.positions.length} positions</p>
                </div>
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
}