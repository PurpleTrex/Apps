import { useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  usePortfolio,
  useRemovePosition,
  useRecalculateRisk,
  useUpdatePortfolioOwner,
} from '@/hooks/usePortfolios';
import Layout from '@/components/Layout';
import Button from '@/components/ui/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Modal from '@/components/ui/Modal';
import Input from '@/components/ui/Input';
import RiskDashboard from '@/components/RiskDashboard';
import PositionsTable from '@/components/PositionsTable';
import AddPositionForm from '@/components/AddPositionForm';
import { formatCurrency, formatNumber } from '@/lib/utils';
import { format } from 'date-fns';

export default function PortfolioDetails() {
  const { id } = useParams<{ id: string }>();
  const { data: portfolio, isLoading, error } = usePortfolio(id!);
  const removePosition = useRemovePosition();
  const recalculateRisk = useRecalculateRisk();
  const updateOwner = useUpdatePortfolioOwner();

  const [showAddPosition, setShowAddPosition] = useState(false);
  const [showEditOwner, setShowEditOwner] = useState(false);
  const [newOwner, setNewOwner] = useState('');
  const [isRecalculating, setIsRecalculating] = useState(false);

  const handleRemovePosition = async (positionId: string, symbol: string) => {
    if (!window.confirm(`Are you sure you want to remove position ${symbol}?`)) {
      return;
    }

    try {
      await removePosition.mutateAsync({ id: id!, positionId });
    } catch (error) {
      console.error('Failed to remove position:', error);
      alert('Failed to remove position. Please try again.');
    }
  };

  const handleRecalculateRisk = async () => {
    setIsRecalculating(true);
    try {
      await recalculateRisk.mutateAsync(id!);
    } catch (error) {
      console.error('Failed to recalculate risk:', error);
      alert('Failed to recalculate risk. Please try again.');
    } finally {
      setIsRecalculating(false);
    }
  };

  const handleUpdateOwner = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newOwner.trim()) return;

    try {
      await updateOwner.mutateAsync({ id: id!, data: { owner: newOwner } });
      setShowEditOwner(false);
      setNewOwner('');
    } catch (error) {
      console.error('Failed to update owner:', error);
      alert('Failed to update owner. Please try again.');
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

  if (error || !portfolio) {
    return (
      <Layout showDashboardLink={true}>
        <div style={{ display: 'flex', minHeight: 'calc(100vh - 3.5rem)', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ fontSize: '0.875rem', color: '#fb7185' }}>Failed to load portfolio details</div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout showDashboardLink={true}>
      {/* Main Content */}
      <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '1.5rem' }}>
        <div style={{ marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <p style={{ fontSize: '10px', letterSpacing: '0.025em', color: '#64748b', marginBottom: '0.75rem', fontWeight: '500', textTransform: 'uppercase' }}>
                Portfolio Overview
              </p>
              <h1 style={{ marginTop: '0.75rem', fontSize: '1.875rem', fontWeight: '600', color: 'white' }}>
                {portfolio.name}
              </h1>
              <div style={{ marginTop: '1rem', display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '1rem', fontSize: '0.875rem', color: '#94a3b8' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  <svg style={{ height: '1rem', width: '1rem' }} viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                  Owner: {portfolio.owner}
                </span>
                <button
                  onClick={() => {
                    setNewOwner(portfolio.owner);
                    setShowEditOwner(true);
                  }}
                  style={{
                    fontSize: '10px',
                    fontWeight: '600',
                    textTransform: 'uppercase',
                    letterSpacing: '0.07em',
                    color: '#0ea5e9',
                    transition: 'color 200ms',
                    backgroundColor: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    padding: 0,
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.color = '#38bdf8'}
                  onMouseLeave={(e) => e.currentTarget.style.color = '#0ea5e9'}
                >
                  Edit Owner
                </button>
              </div>
              <p style={{ marginTop: '0.75rem', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.08em', color: '#64748b' }}>
                Created {format(new Date(portfolio.createdAt), 'MMM d, yyyy')}
                {portfolio.lastEvaluatedAt &&
                  ` • Last evaluated ${format(new Date(portfolio.lastEvaluatedAt), 'MMM d, yyyy h:mm a')}`}
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ borderRadius: '0.5rem', border: '1px solid rgba(51, 65, 85, 0.5)', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '1rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', paddingTop: '0.5rem' }}>
                <div>
                  <p style={{ fontSize: '9px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.1em', color: '#64748b' }}>
                    Value at Risk (95%)
                  </p>
                  <p style={{ marginTop: '0.5rem', fontSize: '1.5rem', fontWeight: '600', color: 'white' }}>
                    {formatCurrency(portfolio.valueAtRisk, portfolio.baseCurrency)}
                  </p>
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '0.75rem' }}>
                  <Button
                    variant="primary"
                    size="md"
                    onClick={handleRecalculateRisk}
                    disabled={isRecalculating}
                  >
                    {isRecalculating ? 'Recalculating…' : 'Recalculate Risk'}
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: '2rem' }}>
          <div style={{ borderRadius: '0.5rem', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '1rem' }}>
            <div>
              <p style={{ fontSize: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.09em', color: '#64748b' }}>
                Total Value
              </p>
            </div>
            <div style={{ paddingTop: '0.5rem' }}>
              <p style={{ fontSize: '1.875rem', fontWeight: '600', color: 'white' }}>
                {formatCurrency(portfolio.totalValue, portfolio.baseCurrency)}
              </p>
              <p style={{ marginTop: '0.75rem', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.07em', color: '#64748b' }}>
                {portfolio.positions.length} positions
              </p>
            </div>
          </div>

          <div style={{ borderRadius: '0.5rem', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '1rem' }}>
            <div>
              <p style={{ fontSize: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.09em', color: '#64748b' }}>
                Risk Score
              </p>
            </div>
            <div style={{ paddingTop: '0.5rem' }}>
              <p style={{ fontSize: '1.875rem', fontWeight: '600', color: 'white' }}>
                {formatNumber(portfolio.riskScore, 1)}
              </p>
              <p style={{ marginTop: '0.75rem', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.07em', color: '#64748b' }}>Scale 0-200</p>
            </div>
          </div>

          <div style={{ borderRadius: '0.5rem', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '1rem' }}>
            <div>
              <p style={{ fontSize: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.09em', color: '#64748b' }}>
                Daily VaR %
              </p>
            </div>
            <div style={{ paddingTop: '0.5rem' }}>
              <p style={{ fontSize: '1.875rem', fontWeight: '600', color: 'white' }}>
                {portfolio.totalValue > 0
                  ? `${formatNumber((portfolio.valueAtRisk / portfolio.totalValue) * 100, 2)}%`
                  : '0.00%'}
              </p>
              <p style={{ marginTop: '0.75rem', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.07em', color: '#64748b' }}>
                Value at risk relative exposure
              </p>
            </div>
          </div>
        </div>

        <div style={{ marginTop: '2rem' }}>
          <RiskDashboard portfolio={portfolio} />
        </div>

        <div style={{ marginTop: '2rem', borderRadius: '0.5rem', border: '1px solid rgba(51, 65, 85, 0.5)', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '1rem' }}>
          <div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', paddingBottom: '1rem' }}>
              <div>
                <p style={{ fontSize: '1.125rem', fontWeight: '600', color: 'white' }}>Positions</p>
                <p style={{ fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.07em', color: '#64748b' }}>
                  Manage individual holdings and risk contribution
                </p>
              </div>
              <Button onClick={() => setShowAddPosition(true)}>Add Position</Button>
            </div>
          </div>
          <div style={{ paddingTop: '0.5rem' }}>
            <PositionsTable
              positions={portfolio.positions}
              baseCurrency={portfolio.baseCurrency}
              onRemove={handleRemovePosition}
            />
          </div>
        </div>

        {/* Add Position Modal */}
        <Modal
          isOpen={showAddPosition}
          onClose={() => setShowAddPosition(false)}
          title="Add Position"
          size="lg"
        >
          <AddPositionForm
            portfolioId={id!}
            onSuccess={() => setShowAddPosition(false)}
            onCancel={() => setShowAddPosition(false)}
          />
        </Modal>

        {/* Edit Owner Modal */}
        <Modal
          isOpen={showEditOwner}
          onClose={() => setShowEditOwner(false)}
          title="Update Portfolio Owner"
          footer={
            <>
              <Button variant="secondary" onClick={() => setShowEditOwner(false)}>
                Cancel
              </Button>
              <Button onClick={handleUpdateOwner} disabled={!newOwner.trim()}>
                Update
              </Button>
            </>
          }
        >
          <form onSubmit={handleUpdateOwner}>
            <Input
              label="Owner Name"
              value={newOwner}
              onChange={(e) => setNewOwner(e.target.value)}
              placeholder="Enter owner name"
              required
              maxLength={200}
            />
          </form>
        </Modal>
      </div>
    </Layout>
  );
}