import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePortfolios, useDeletePortfolio } from '@/hooks/usePortfolios';
import Layout from '@/components/Layout';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { formatCurrency, formatNumber } from '@/lib/utils';
import { format } from 'date-fns';
import type { PortfolioResponse } from '@/types/api';

interface StatCardProps {
  label: string;
  value: string;
  icon: React.ReactNode;
  trend?: 'up' | 'down' | 'neutral';
  gradient?: boolean;
}

function StatCard({ label, value, icon, trend = 'neutral', gradient = false }: StatCardProps) {
  const trendColors = {
    up: '#34d399',
    down: '#fb7185',
    neutral: '#94a3b8',
  };

  return (
    <div style={{
      position: 'relative',
      overflow: 'hidden',
      borderRadius: '0.5rem',
      border: '1px solid rgba(51, 65, 85, 0.6)',
      backgroundColor: 'rgba(15, 20, 25, 0.7)',
      padding: '1.5rem',
      transition: 'all 300ms',
      cursor: 'default',
    }}
    onMouseEnter={(e) => {
      e.currentTarget.style.borderColor = 'rgba(51, 65, 85, 0.8)';
      e.currentTarget.style.boxShadow = '0 20px 60px -24px rgba(15,23,42,0.9)';
    }}
    onMouseLeave={(e) => {
      e.currentTarget.style.borderColor = 'rgba(51, 65, 85, 0.6)';
      e.currentTarget.style.boxShadow = 'none';
    }}
    >
      <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{
          borderRadius: '0.75rem',
          backgroundColor: gradient ? 'rgba(14, 165, 233, 0.2)' : 'rgba(15, 23, 42, 0.7)',
          padding: '0.75rem',
        }}>
          <div style={{ color: gradient ? '#38bdf8' : '#94a3b8' }}>{icon}</div>
        </div>
        {trend !== 'neutral' && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.25rem',
            fontSize: '10px',
            fontWeight: '600',
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            color: trendColors[trend],
          }}>
            {trend === 'up' ? '↑' : '↓'}
          </div>
        )}
      </div>
      <p style={{ marginBottom: '0.5rem', fontSize: '9.5px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.1em', color: '#64748b' }}>
        {label}
      </p>
      <p style={{ fontSize: '1.875rem', fontWeight: '600', color: gradient ? '#38bdf8' : 'white' }}>
        {value}
      </p>
    </div>
  );
}

export default function PortfolioList() {
  const navigate = useNavigate();
  const { data: portfolios, isLoading, error } = usePortfolios();
  const deletePortfolio = useDeletePortfolio();
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const handleDelete = async (id: string, name: string) => {
    if (!window.confirm(`Are you sure you want to delete portfolio "${name}"?`)) {
      return;
    }

    setDeletingId(id);
    try {
      await deletePortfolio.mutateAsync(id);
    } catch (error) {
      console.error('Failed to delete portfolio:', error);
      alert('Failed to delete portfolio. Please try again.');
    } finally {
      setDeletingId(null);
    }
  };

  if (isLoading) {
    return (
      <Layout showDashboardLink={true}>
        <div style={{ display: 'flex', minHeight: 'calc(100vh - 3.5rem)', alignItems: 'center', justifyContent: 'center' }}>
          <LoadingSpinner size="lg" />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout showDashboardLink={true}>
        <div style={{ display: 'flex', minHeight: 'calc(100vh - 3.5rem)', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ fontSize: '0.875rem', color: '#fb7185' }}>Error loading portfolios. Please try again.</div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout showDashboardLink={true}>
      {/* Main Content */}
      <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '1.5rem' }}>
        {/* Modern Hero Section */}
        <div style={{ marginBottom: '2rem' }}>
          <div style={{ marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ flex: 1 }}>
              <p style={{ marginBottom: '0.75rem', fontSize: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.095em', color: '#64748b' }}>
                Wealth Management
              </p>
              <h1 style={{ marginBottom: '0.75rem', fontSize: '1.875rem', fontWeight: '600', color: 'white' }}>
                Your Portfolios
              </h1>
              <p style={{ maxWidth: '48rem', fontSize: '1rem', lineHeight: '1.75', color: '#94a3b8' }}>
                Monitor performance, track risk exposure, and manage your investments with institutional-grade analytics
              </p>
            </div>
            <Button
              onClick={() => navigate('/portfolios/new')}
              size="lg"
            >
              <svg style={{ height: '1.25rem', width: '1.25rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Create Portfolio
            </Button>
          </div>

          {/* Enhanced Stats Cards */}
          {portfolios && portfolios.length > 0 && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
              <StatCard
                label="Total Portfolios"
                value={portfolios.length.toString()}
                icon={
                  <svg style={{ height: '1.25rem', width: '1.25rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                  </svg>
                }
                trend="neutral"
              />
              <StatCard
                label="Assets Under Management"
                value={formatCurrency(portfolios.reduce((sum, p) => sum + p.totalValue, 0), 'USD')}
                icon={
                  <svg style={{ height: '1.25rem', width: '1.25rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                }
                trend="up"
                gradient
              />
              <StatCard
                label="Total Positions"
                value={portfolios.reduce((sum, p) => sum + p.positions.length, 0).toString()}
                icon={
                  <svg style={{ height: '1.25rem', width: '1.25rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                }
                trend="neutral"
              />
              <StatCard
                label="Average Risk Score"
                value={formatNumber(portfolios.reduce((sum, p) => sum + p.riskScore, 0) / portfolios.length, 1)}
                icon={
                  <svg style={{ height: '1.25rem', width: '1.25rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                }
                trend={portfolios.reduce((sum, p) => sum + p.riskScore, 0) / portfolios.length < 100 ? 'down' : 'neutral'}
              />
            </div>
          )}
        </div>

        {!portfolios || portfolios.length === 0 ? (
          <div style={{ display: 'flex', minHeight: '50vh', alignItems: 'center', justifyContent: 'center', borderRadius: '1rem', border: '1px solid rgba(51, 65, 85, 0.6)', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '4rem', textAlign: 'center' }}>
            <div>
              <div style={{ margin: '0 auto 1.5rem', display: 'flex', height: '6rem', width: '6rem', alignItems: 'center', justifyContent: 'center', borderRadius: '1rem', background: 'linear-gradient(to bottom right, #1e293b, #0f172a)' }}>
                <svg style={{ height: '3rem', width: '3rem', color: '#64748b' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                </svg>
              </div>
              <h3 style={{ marginBottom: '0.75rem', fontSize: '1.5rem', fontWeight: '600', color: 'white' }}>No Portfolios Yet</h3>
              <p style={{ margin: '0 auto 2rem', maxWidth: '28rem', color: '#94a3b8' }}>
                Get started by creating your first portfolio to track investments and monitor risk with professional analytics
              </p>
              <Button
                onClick={() => navigate('/portfolios/new')}
                size="lg"
              >
                <svg style={{ height: '1.25rem', width: '1.25rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                Create Your First Portfolio
              </Button>
            </div>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.5rem' }}>
            {portfolios.map((portfolio) => (
              <PortfolioCard
                key={portfolio.id}
                portfolio={portfolio}
                onDelete={handleDelete}
                onView={() => navigate(`/portfolios/${portfolio.id}`)}
                isDeleting={deletingId === portfolio.id}
              />
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}

interface PortfolioCardProps {
  portfolio: PortfolioResponse;
  onDelete: (id: string, name: string) => void;
  onView: () => void;
  isDeleting: boolean;
}

function PortfolioCard({ portfolio, onDelete, onView, isDeleting }: PortfolioCardProps) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div
      style={{
        position: 'relative',
        overflow: 'hidden',
        borderRadius: '1rem',
        border: '1px solid rgba(51, 65, 85, 0.6)',
        backgroundColor: 'rgba(15, 20, 25, 0.8)',
        padding: '1.5rem',
        transition: 'all 300ms',
        cursor: 'pointer',
        borderColor: isHovered ? 'rgba(51, 65, 85, 0.7)' : 'rgba(51, 65, 85, 0.6)',
        boxShadow: isHovered ? '0 20px 60px -24px rgba(15,23,42,0.9)' : 'none',
      }}
      onClick={onView}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Header */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div style={{ flex: 1 }}>
          <h3 style={{ marginBottom: '0.5rem', fontSize: '1.25rem', fontWeight: '600', color: isHovered ? '#38bdf8' : 'white', transition: 'color 200ms' }}>
            {portfolio.name}
          </h3>
          <p style={{ fontSize: '0.875rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>{portfolio.owner}</p>
        </div>
        <Badge variant="risk" riskClassification={portfolio.riskClassification} />
      </div>

      {/* Value Display */}
      <div style={{ marginBottom: '1.5rem', borderBottom: '1px solid rgba(51, 65, 85, 0.6)', paddingBottom: '1.5rem' }}>
        <p style={{ marginBottom: '0.5rem', fontSize: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.075em', color: '#64748b' }}>Portfolio Value</p>
        <p style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#38bdf8' }}>
          {formatCurrency(portfolio.totalValue, portfolio.baseCurrency)}
        </p>
      </div>

      {/* Risk Metrics Grid */}
      <div style={{ marginBottom: '1.5rem', display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
        <div style={{ borderRadius: '0.75rem', border: '1px solid rgba(51, 65, 85, 0.6)', backgroundColor: 'rgba(15, 23, 42, 0.5)', padding: '1rem' }}>
          <p style={{ marginBottom: '0.5rem', fontSize: '9.5px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.088em', color: '#64748b' }}>Risk Score</p>
          <p style={{ fontSize: '1.5rem', fontWeight: '600', color: 'white' }}>
            {formatNumber(portfolio.riskScore, 1)}
          </p>
        </div>
        <div style={{ borderRadius: '0.75rem', border: '1px solid rgba(51, 65, 85, 0.6)', backgroundColor: 'rgba(15, 23, 42, 0.5)', padding: '1rem' }}>
          <p style={{ marginBottom: '0.5rem', fontSize: '9.5px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.088em', color: '#64748b' }}>Value at Risk</p>
          <p style={{ fontSize: '1.125rem', fontWeight: '600', color: '#fbbf24' }}>
            {formatCurrency(portfolio.valueAtRisk, portfolio.baseCurrency)}
          </p>
        </div>
      </div>

      {/* Footer Info */}
      <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid rgba(51, 65, 85, 0.6)', paddingBottom: '1rem', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.06em', color: '#64748b' }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <svg style={{ height: '1rem', width: '1rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          {portfolio.positions.length} Positions
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <svg style={{ height: '1rem', width: '1rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {portfolio.lastEvaluatedAt
            ? format(new Date(portfolio.lastEvaluatedAt), 'MMM d')
            : 'Not Evaluated'}
        </span>
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <Button
          variant="secondary"
          size="sm"
          style={{ flex: 1 }}
          onClick={(e) => {
            e.stopPropagation();
            onView();
          }}
        >
          View Details
        </Button>
        <Button
          variant="danger"
          size="sm"
          onClick={(e) => {
            e.stopPropagation();
            onDelete(portfolio.id, portfolio.name);
          }}
          disabled={isDeleting}
        >
          {isDeleting ? (
            <svg style={{ height: '1rem', width: '1rem', animation: 'spin 1s linear infinite' }} fill="none" viewBox="0 0 24 24">
              <circle style={{ opacity: 0.25 }} cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path style={{ opacity: 0.75 }} fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          ) : (
            <svg style={{ height: '1rem', width: '1rem' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          )}
        </Button>
      </div>
    </div>
  );
}