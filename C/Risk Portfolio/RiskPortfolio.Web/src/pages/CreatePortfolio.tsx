import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreatePortfolio } from '@/hooks/usePortfolios';
import { marketDataService } from '@/services/marketData';
import Layout from '@/components/Layout';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { SymbolSearch } from '@/components/SymbolSearch';
import type { CreatePortfolioRequest, CreatePortfolioPositionRequest } from '@/types/api';
import type { SymbolSearchResult } from '@/services/marketData';

export default function CreatePortfolio() {
  const navigate = useNavigate();
  const createPortfolio = useCreatePortfolio();

  const [formData, setFormData] = useState({
    name: '',
    owner: '',
    baseCurrency: 'USD',
  });

  const [positions, setPositions] = useState<CreatePortfolioPositionRequest[]>([]);
  const [currentPosition, setCurrentPosition] = useState<CreatePortfolioPositionRequest>({
    symbol: '',
    quantity: 0,
    averagePrice: 0,
    currentPrice: 0,
    volatility: 0,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Portfolio name is required';
    } else if (formData.name.length > 200) {
      newErrors.name = 'Portfolio name must be 200 characters or less';
    }

    if (!formData.owner.trim()) {
      newErrors.owner = 'Owner name is required';
    } else if (formData.owner.length > 200) {
      newErrors.owner = 'Owner name must be 200 characters or less';
    }

    if (!/^[A-Z]{3}$/.test(formData.baseCurrency)) {
      newErrors.baseCurrency = 'Currency must be a 3-letter ISO code (e.g., USD)';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validatePosition = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!currentPosition.symbol.trim()) {
      newErrors.posSymbol = 'Symbol is required';
    } else if (currentPosition.symbol.length > 20) {
      newErrors.posSymbol = 'Symbol must be 20 characters or less';
    }

    if (currentPosition.quantity <= 0) {
      newErrors.posQuantity = 'Quantity must be greater than 0';
    }

    if (currentPosition.averagePrice <= 0) {
      newErrors.posAveragePrice = 'Average price must be greater than 0';
    }

    if (currentPosition.currentPrice <= 0) {
      newErrors.posCurrentPrice = 'Current price must be greater than 0';
    }

    if (currentPosition.volatility < 0 || currentPosition.volatility > 1) {
      newErrors.posVolatility = 'Volatility must be between 0 and 1';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAddPosition = () => {
    if (!validatePosition()) return;

    const symbolExists = positions.some(
      (p) => p.symbol.toUpperCase() === currentPosition.symbol.toUpperCase()
    );

    if (symbolExists) {
      setErrors({ posSymbol: 'Position with this symbol already exists' });
      return;
    }

    setPositions([...positions, { ...currentPosition, symbol: currentPosition.symbol.toUpperCase() }]);
    setCurrentPosition({
      symbol: '',
      quantity: 0,
      averagePrice: 0,
      currentPrice: 0,
      volatility: 0,
    });
    setErrors({});
  };

  const handleRemovePosition = (index: number) => {
    setPositions(positions.filter((_, i) => i !== index));
  };

  const handleSymbolSelect = async (result: SymbolSearchResult) => {
    // Auto-fill the current position with the selected symbol
    setCurrentPosition({
      ...currentPosition,
      symbol: result.symbol,
    });

    // Try to fetch live quote data to pre-fill prices
    try {
      const quote = await marketDataService.getQuote(result.symbol);
      if (quote) {
        setCurrentPosition({
          symbol: result.symbol,
          quantity: 0,
          averagePrice: quote.price,
          currentPrice: quote.price,
          volatility: quote.volatility || 0,
        });
      }
    } catch {
      console.log('Could not fetch quote data, using defaults');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      const data: CreatePortfolioRequest = {
        ...formData,
        positions: positions.length > 0 ? positions : undefined,
      };

      const portfolio = await createPortfolio.mutateAsync(data);
      navigate(`/portfolios/${portfolio.id}`);
    } catch (error: unknown) {
      console.error('Failed to create portfolio:', error);
      if (error && typeof error === 'object' && 'errors' in error) {
        const apiErrors = error.errors as Record<string, string[]>;
        const newErrors: Record<string, string> = {};
        Object.entries(apiErrors).forEach(([key, messages]) => {
          newErrors[key.toLowerCase()] = messages[0];
        });
        setErrors(newErrors);
      } else {
        alert('Failed to create portfolio. Please try again.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Layout showDashboardLink={true}>
      {/* Main Content */}
      <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '1.5rem' }}>
        <div style={{ borderRadius: '0.5rem', border: '1px solid rgba(51, 65, 85, 0.5)', backgroundColor: 'rgba(15, 20, 25, 0.7)', padding: '1rem' }}>
          <div>
            <p style={{ fontSize: '1.25rem', fontWeight: '600', color: 'white' }}>Create New Portfolio</p>
          </div>
          <div style={{ paddingTop: '0.5rem' }}>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <h3 style={{ fontSize: '0.875rem', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.088em', color: '#64748b' }}>
                  Portfolio Information
                </h3>

                <Input
                  label="Portfolio Name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="e.g., Tech Growth Portfolio"
                  error={errors.name}
                  required
                  maxLength={200}
                />

                <Input
                  label="Owner"
                  value={formData.owner}
                  onChange={(e) => setFormData({ ...formData, owner: e.target.value })}
                  placeholder="e.g., John Doe"
                  error={errors.owner}
                  required
                  maxLength={200}
                />

                <Input
                  label="Base Currency"
                  value={formData.baseCurrency}
                  onChange={(e) => setFormData({ ...formData, baseCurrency: e.target.value.toUpperCase() })}
                  placeholder="e.g., USD"
                  error={errors.baseCurrency}
                  required
                  maxLength={3}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', borderTop: '1px solid rgba(51, 65, 85, 0.6)', paddingTop: '1.5rem' }}>
                <h3 style={{ fontSize: '0.875rem', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.088em', color: '#64748b' }}>
                  Positions {positions.length > 0 && `(${positions.length})`}
                </h3>

                {positions.length > 0 && (
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', fontSize: '0.875rem', color: '#cbd5e1' }}>
                      <thead style={{ backgroundColor: 'rgba(15, 23, 42, 0.8)', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.07em', color: '#94a3b8' }}>
                        <tr style={{ textAlign: 'left' }}>
                          <th style={{ padding: '0.5rem 1rem' }}>Symbol</th>
                          <th style={{ padding: '0.5rem 1rem', textAlign: 'right' }}>Quantity</th>
                          <th style={{ padding: '0.5rem 1rem', textAlign: 'right' }}>Avg Price</th>
                          <th style={{ padding: '0.5rem 1rem', textAlign: 'right' }}>Current Price</th>
                          <th style={{ padding: '0.5rem 1rem', textAlign: 'right' }}>Volatility</th>
                          <th style={{ padding: '0.5rem 1rem' }}></th>
                        </tr>
                      </thead>
                      <tbody>
                        {positions.map((pos, index) => (
                          <tr key={index} style={{ borderBottom: '1px solid rgba(51, 65, 85, 0.6)', backgroundColor: 'rgba(2, 6, 23, 0.4)' }}>
                            <td style={{ padding: '0.5rem 1rem', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.05em', color: '#e2e8f0' }}>
                              {pos.symbol}
                            </td>
                            <td style={{ padding: '0.5rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>{pos.quantity}</td>
                            <td style={{ padding: '0.5rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>{pos.averagePrice.toFixed(2)}</td>
                            <td style={{ padding: '0.5rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>{pos.currentPrice.toFixed(2)}</td>
                            <td style={{ padding: '0.5rem 1rem', textAlign: 'right', color: '#cbd5e1' }}>{pos.volatility.toFixed(3)}</td>
                            <td style={{ padding: '0.5rem 1rem', textAlign: 'right' }}>
                              <button
                                type="button"
                                onClick={() => handleRemovePosition(index)}
                                style={{
                                  fontSize: '10px',
                                  fontWeight: '600',
                                  textTransform: 'uppercase',
                                  letterSpacing: '0.07em',
                                  color: '#fb7185',
                                  transition: 'color 200ms',
                                  backgroundColor: 'transparent',
                                  border: 'none',
                                  cursor: 'pointer',
                                  padding: 0,
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.color = '#fda4af'}
                                onMouseLeave={(e) => e.currentTarget.style.color = '#fb7185'}
                              >
                                Remove
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', borderRadius: '0.75rem', border: '1px solid rgba(51, 65, 85, 0.6)', backgroundColor: 'rgba(2, 6, 23, 0.6)', padding: '1rem' }}>
                  <p style={{ fontSize: '10px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.08em', color: '#94a3b8' }}>
                    Add Position
                  </p>

                  {/* Symbol Search */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <label style={{ fontSize: '0.875rem', fontWeight: '500', color: '#94a3b8' }}>
                      Search Stock Symbol
                    </label>
                    <SymbolSearch
                      onSelectSymbol={handleSymbolSelect}
                      placeholder="Search for stocks (e.g., TSLA, AAPL, GOOGL)..."
                    />
                    <p style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                      Search and select a stock to auto-fill prices, or enter manually below
                    </p>
                  </div>

                  {/* Manual Entry Fields */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
                    <Input
                      label="Symbol"
                      value={currentPosition.symbol}
                      onChange={(e) =>
                        setCurrentPosition({ ...currentPosition, symbol: e.target.value.toUpperCase() })
                      }
                      placeholder="e.g., AAPL"
                      error={errors.posSymbol}
                      maxLength={20}
                    />

                    <Input
                      type="number"
                      label="Quantity"
                      value={currentPosition.quantity || ''}
                      onChange={(e) =>
                        setCurrentPosition({
                          ...currentPosition,
                          quantity: parseFloat(e.target.value) || 0,
                        })
                      }
                      placeholder="e.g., 100"
                      error={errors.posQuantity}
                      step="0.0001"
                    />

                    <Input
                      type="number"
                      label="Average Price"
                      value={currentPosition.averagePrice || ''}
                      onChange={(e) =>
                        setCurrentPosition({
                          ...currentPosition,
                          averagePrice: parseFloat(e.target.value) || 0,
                        })
                      }
                      placeholder="e.g., 150.00"
                      error={errors.posAveragePrice}
                      step="0.01"
                    />

                    <Input
                      type="number"
                      label="Current Price"
                      value={currentPosition.currentPrice || ''}
                      onChange={(e) =>
                        setCurrentPosition({
                          ...currentPosition,
                          currentPrice: parseFloat(e.target.value) || 0,
                        })
                      }
                      placeholder="e.g., 155.00"
                      error={errors.posCurrentPrice}
                      step="0.01"
                    />

                    <Input
                      type="number"
                      label="Volatility (0-1)"
                      value={currentPosition.volatility || ''}
                      onChange={(e) =>
                        setCurrentPosition({
                          ...currentPosition,
                          volatility: parseFloat(e.target.value) || 0,
                        })
                      }
                      placeholder="e.g., 0.25"
                      error={errors.posVolatility}
                      step="0.001"
                      min="0"
                      max="1"
                    />
                  </div>

                  <Button type="button" onClick={handleAddPosition} variant="secondary">
                    Add Position
                  </Button>
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', borderTop: '1px solid rgba(51, 65, 85, 0.6)', paddingTop: '1.5rem' }}>
                <Button type="button" variant="secondary" onClick={() => navigate('/portfolios')}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? 'Creating...' : 'Create Portfolio'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Layout>
  );
}