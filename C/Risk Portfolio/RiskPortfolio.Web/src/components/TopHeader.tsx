import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePortfolios } from '@/hooks/usePortfolios';
import { formatCurrency } from '@/lib/utils';

export default function TopHeader() {
  const navigate = useNavigate();
  const { data: portfolios } = usePortfolios();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <div 
      className="flex items-center justify-between" 
      style={{ 
        height: '3.5rem',
        paddingLeft: '1.5rem',
        paddingRight: '1.5rem',
        borderBottom: '1px solid rgba(148, 163, 184, 0.3)',
        backgroundColor: '#0F1419',
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 40
      }}
    >
      {/* Left side: Logo/Title */}
      <div className="flex items-center gap-4">
        <h1 
          className="font-semibold tracking-wide" 
          style={{ 
            fontSize: '1rem', 
            color: 'white',
            textTransform: 'uppercase',
            letterSpacing: '0.1em'
          }}
        >
          INVESTMENT PORTFOLIO
        </h1>
      </div>

      {/* Right side: User Account / Portfolio Dropdown */}
      <div style={{ position: 'relative' }}>
        <button
          onClick={() => setIsDropdownOpen(!isDropdownOpen)}
          style={{ 
            display: 'flex',
            alignItems: 'center',
            borderRadius: '0.5rem',
            padding: '0.375rem 0.75rem',
            gap: '0.625rem',
            backgroundColor: '#12161F',
            border: 'none',
            cursor: 'pointer',
            transition: 'background-color 200ms'
          }}
          onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#1a1f2e'}
          onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#12161F'}
        >
          <div 
            style={{ 
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '50%',
              fontWeight: 'bold',
              height: '2rem',
              width: '2rem',
              fontSize: '0.75rem',
              background: 'linear-gradient(to bottom right, #38bdf8, #2563eb)',
              color: 'white'
            }}
          >
            MP
          </div>
          <div style={{ textAlign: 'left' }}>
            <p style={{ fontSize: '0.75rem', color: 'white', lineHeight: '1.25', fontWeight: '600' }}>Main Portfolio</p>
            <p style={{ fontSize: '10px', color: '#34d399', lineHeight: '1.25', fontWeight: '500' }}>+15%</p>
          </div>
          <svg 
            style={{ 
              height: '0.875rem', 
              width: '0.875rem', 
              color: '#64748b',
              transform: isDropdownOpen ? 'rotate(180deg)' : 'rotate(0deg)',
              transition: 'transform 200ms'
            }} 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        {/* Portfolio Navigation Dropdown */}
        {isDropdownOpen && portfolios && (
          <div 
            style={{
              position: 'absolute',
              right: 0,
              top: '100%',
              marginTop: '0.5rem',
              width: '16rem',
              borderRadius: '0.75rem',
              border: '1px solid rgba(148, 163, 184, 0.5)',
              backgroundColor: '#12161F',
              padding: '0.5rem',
              boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
              zIndex: 50
            }}
          >
            <div style={{ 
              padding: '0.75rem 0.5rem', 
              borderBottom: '1px solid rgba(148, 163, 184, 0.2)',
              marginBottom: '0.5rem'
            }}>
              <p style={{ fontSize: '0.75rem', fontWeight: '600', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Navigation
              </p>
            </div>
            <button
              onClick={() => {
                navigate('/');
                setIsDropdownOpen(false);
              }}
              style={{
                width: '100%',
                borderRadius: '0.5rem',
                padding: '0.75rem',
                textAlign: 'left',
                fontSize: '0.875rem',
                backgroundColor: 'transparent',
                border: 'none',
                cursor: 'pointer',
                transition: 'background-color 200ms',
                marginBottom: '0.5rem'
              }}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(148, 163, 184, 0.15)'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <svg style={{ height: '1rem', width: '1rem', color: '#38bdf8' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                </svg>
                <p style={{ fontWeight: '600', color: 'white', fontSize: '0.875rem' }}>Dashboard</p>
              </div>
            </button>
            <button
              onClick={() => {
                navigate('/portfolios');
                setIsDropdownOpen(false);
              }}
              style={{
                width: '100%',
                borderRadius: '0.5rem',
                padding: '0.75rem',
                textAlign: 'left',
                fontSize: '0.875rem',
                backgroundColor: 'transparent',
                border: 'none',
                cursor: 'pointer',
                transition: 'background-color 200ms',
                marginBottom: '0.5rem'
              }}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(148, 163, 184, 0.15)'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <svg style={{ height: '1rem', width: '1rem', color: '#38bdf8' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                </svg>
                <p style={{ fontWeight: '600', color: 'white', fontSize: '0.875rem' }}>All Portfolios</p>
              </div>
            </button>
            <div style={{ 
              padding: '0.75rem 0.5rem', 
              borderBottom: '1px solid rgba(148, 163, 184, 0.2)',
              marginBottom: '0.5rem',
              marginTop: '0.5rem'
            }}>
              <p style={{ fontSize: '0.75rem', fontWeight: '600', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                My Portfolios
              </p>
            </div>
            {portfolios.map((portfolio) => (
              <button
                key={portfolio.id}
                onClick={() => {
                  navigate(`/portfolios/${portfolio.id}`);
                  setIsDropdownOpen(false);
                }}
                style={{
                  width: '100%',
                  borderRadius: '0.5rem',
                  padding: '0.75rem',
                  textAlign: 'left',
                  fontSize: '0.875rem',
                  backgroundColor: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  transition: 'background-color 200ms',
                  marginBottom: '0.25rem'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(148, 163, 184, 0.15)'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                  <p style={{ fontWeight: '600', color: 'white', fontSize: '0.875rem' }}>{portfolio.name}</p>
                  <span style={{ 
                    fontSize: '0.75rem',
                    fontWeight: '600',
                    color: '#34d399',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    padding: '0.125rem 0.5rem',
                    borderRadius: '0.25rem'
                  }}>
                    +8%
                  </span>
                </div>
                <p style={{ fontSize: '0.75rem', color: '#64748b' }}>{formatCurrency(portfolio.totalValue, 'USD', 0)}</p>
              </button>
            ))}
            <div style={{ 
              marginTop: '0.5rem',
              paddingTop: '0.5rem',
              borderTop: '1px solid rgba(148, 163, 184, 0.2)'
            }}>
              <button
                onClick={() => {
                  navigate('/portfolios/new');
                  setIsDropdownOpen(false);
                }}
                style={{
                  width: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  padding: '0.625rem',
                  borderRadius: '0.5rem',
                  backgroundColor: '#0ea5e9',
                  border: 'none',
                  color: 'white',
                  fontSize: '0.75rem',
                  fontWeight: '600',
                  cursor: 'pointer',
                  transition: 'background-color 200ms'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0284c7'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#0ea5e9'}
              >
                <svg style={{ height: '1rem', width: '1rem' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                New Portfolio
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
