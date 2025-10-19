import { useState, useMemo } from 'react';
import { usePortfolios } from '@/hooks/usePortfolios';
import Layout from '@/components/Layout';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { formatCurrency, formatNumber } from '@/lib/utils';
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ScatterChart, Scatter } from 'recharts';

const COLORS = ['#38bdf8', '#22d3ee', '#0ea5e9', '#3b82f6', '#6366f1', '#8b5cf6', '#a855f7', '#d946ef'];

type AnalysisTab = 'overview' | 'risk' | 'performance' | 'diversification' | 'positions';

export default function Analytics() {
  const { data: portfolios, isLoading, error } = usePortfolios();
  const [activeTab, setActiveTab] = useState<AnalysisTab>('overview');

  const analytics = useMemo(() => {
    if (!portfolios || portfolios.length === 0) return null;

    const totalValue = portfolios.reduce((sum, p) => sum + p.totalValue, 0);
    const totalVar = portfolios.reduce((sum, p) => sum + p.valueAtRisk, 0);
    const avgRiskScore = portfolios.reduce((sum, p) => sum + p.riskScore, 0) / portfolios.length;

    // Risk distribution
    const riskDistribution = portfolios.map(p => ({
      name: p.name,
      riskScore: p.riskScore,
      valueAtRisk: p.valueAtRisk,
      totalValue: p.totalValue,
      varPercentage: (p.valueAtRisk / p.totalValue) * 100,
      classification: ['Low', 'Moderate', 'Elevated', 'Critical'][p.riskClassification - 1]
    }));

    // Portfolio size comparison
    const portfolioSizes = portfolios.map(p => ({
      name: p.name.length > 15 ? p.name.substring(0, 15) + '...' : p.name,
      value: p.totalValue,
      positions: p.positions.length,
      riskScore: p.riskScore
    })).sort((a, b) => b.value - a.value);

    // Asset allocation across all portfolios
    const allSymbols = new Map<string, { quantity: number; value: number; count: number }>();
    portfolios.forEach(portfolio => {
      portfolio.positions.forEach(pos => {
        const existing = allSymbols.get(pos.symbol) || { quantity: 0, value: 0, count: 0 };
        allSymbols.set(pos.symbol, {
          quantity: existing.quantity + pos.quantity,
          value: existing.value + pos.marketValue,
          count: existing.count + 1
        });
      });
    });

    const topAssets = Array.from(allSymbols.entries())
      .map(([symbol, data]) => ({
        symbol,
        value: data.value,
        count: data.count,
        percentage: (data.value / totalValue) * 100
      }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);

    // Risk vs Return scatter
    const riskReturnData = portfolios.map(p => {
      const allPositions = p.positions;
      const totalReturn = allPositions.reduce((sum, pos) => {
        const returnPct = ((pos.currentPrice - pos.averagePrice) / pos.averagePrice) * 100;
        return sum + returnPct * (pos.marketValue / p.totalValue);
      }, 0);

      return {
        name: p.name,
        risk: p.riskScore,
        return: totalReturn,
        value: p.totalValue
      };
    });

    // Volatility analysis
    const volatilityData = portfolios.flatMap(p =>
      p.positions.map(pos => ({
        symbol: pos.symbol,
        volatility: pos.volatility * 100,
        marketValue: pos.marketValue,
        portfolio: p.name
      }))
    ).sort((a, b) => b.volatility - a.volatility).slice(0, 15);

    // Currency distribution
    const currencyDistribution = portfolios.reduce((acc, p) => {
      acc[p.baseCurrency] = (acc[p.baseCurrency] || 0) + p.totalValue;
      return acc;
    }, {} as Record<string, number>);

    const currencyData = Object.entries(currencyDistribution).map(([currency, value]) => ({
      currency,
      value,
      percentage: (value / totalValue) * 100
    }));

    // Risk classification summary
    const riskClassificationCounts = portfolios.reduce((acc, p) => {
      const classification = ['Low', 'Moderate', 'Elevated', 'Critical'][p.riskClassification - 1];
      acc[classification] = (acc[classification] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    return {
      totalValue,
      totalVar,
      avgRiskScore,
      portfolioCount: portfolios.length,
      totalPositions: portfolios.reduce((sum, p) => sum + p.positions.length, 0),
      riskDistribution,
      portfolioSizes,
      topAssets,
      riskReturnData,
      volatilityData,
      currencyData,
      riskClassificationCounts
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

  if (error || !analytics) {
    return (
      <Layout>
        <div style={{ display: 'flex', minHeight: 'calc(100vh - 3.5rem)', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ textAlign: 'center' }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'white', marginBottom: '1rem' }}>No Data Available</h2>
            <p style={{ color: '#94a3b8' }}>Create portfolios to view analytics</p>
          </div>
        </div>
      </Layout>
    );
  }

  const tabs: { id: AnalysisTab; label: string; icon: string }[] = [
    { id: 'overview', label: 'Overview', icon: 'M4 5a1 1 0 011-1h4a1 1 0 011 1v7a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM14 5a1 1 0 011-1h4a1 1 0 011 1v3a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v3a1 1 0 01-1 1H5a1 1 0 01-1-1v-3zM14 12a1 1 0 011-1h4a1 1 0 011 1v7a1 1 0 01-1 1h-4a1 1 0 01-1-1v-7z' },
    { id: 'risk', label: 'Risk Analysis', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z' },
    { id: 'performance', label: 'Performance', icon: 'M13 7h8m0 0v8m0-8l-8 8-4-4-6 6' },
    { id: 'diversification', label: 'Diversification', icon: 'M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z' },
    { id: 'positions', label: 'Top Positions', icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' }
  ];

  return (
    <Layout>
      <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '1.5rem' }}>
        {/* Header */}
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'white', marginBottom: '0.5rem' }}>
            Portfolio Analytics
          </h1>
          <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>
            Comprehensive analysis of your investment portfolio
          </p>
        </div>

        {/* Summary Cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', marginBottom: '2rem' }}>
          <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.25rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
            <p style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Total Value</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'white' }}>{formatCurrency(analytics.totalValue, 'USD', 0)}</p>
          </div>
          <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.25rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
            <p style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Portfolios</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'white' }}>{analytics.portfolioCount}</p>
          </div>
          <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.25rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
            <p style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Avg Risk Score</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#fbbf24' }}>{formatNumber(analytics.avgRiskScore, 1)}</p>
          </div>
          <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.25rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
            <p style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Total VAR</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f87171' }}>{formatCurrency(analytics.totalVar, 'USD', 0)}</p>
          </div>
        </div>

        {/* Tabs */}
        <div style={{ marginBottom: '2rem', borderBottom: '1px solid rgba(148, 163, 184, 0.1)' }}>
          <div style={{ display: 'flex', gap: '1rem' }}>
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.75rem 1rem',
                  fontSize: '0.875rem',
                  fontWeight: '500',
                  color: activeTab === tab.id ? '#38bdf8' : '#94a3b8',
                  backgroundColor: 'transparent',
                  border: 'none',
                  borderBottom: activeTab === tab.id ? '2px solid #38bdf8' : '2px solid transparent',
                  cursor: 'pointer',
                  transition: 'all 200ms'
                }}
              >
                <svg style={{ height: '1rem', width: '1rem' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={tab.icon} />
                </svg>
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* Tab Content */}
        <div>
          {activeTab === 'overview' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1.5rem' }}>
              {/* Portfolio Sizes */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Portfolio Values</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={analytics.portfolioSizes}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                    <XAxis dataKey="name" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} />
                    <YAxis stroke="#94a3b8" style={{ fontSize: '0.75rem' }} tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`} />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      labelStyle={{ color: 'white' }}
                      formatter={(value: number) => [formatCurrency(value, 'USD', 0), 'Value']}
                    />
                    <Bar dataKey="value" fill="#38bdf8" />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Currency Distribution */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Currency Distribution</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={analytics.currencyData}
                      dataKey="value"
                      nameKey="currency"
                      cx="50%"
                      cy="50%"
                      outerRadius={100}
                      label={(entry) => `${entry.currency} (${entry.percentage.toFixed(1)}%)`}
                    >
                      {analytics.currencyData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      formatter={(value: number) => formatCurrency(value, 'USD', 0)}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          {activeTab === 'risk' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1.5rem' }}>
              {/* Risk Distribution */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Risk Score by Portfolio</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={analytics.riskDistribution} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                    <XAxis type="number" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} />
                    <YAxis type="category" dataKey="name" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} width={120} />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      formatter={(value: number) => [value.toFixed(2), 'Risk Score']}
                    />
                    <Bar dataKey="riskScore" fill="#fbbf24" />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* VAR Percentage */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Value at Risk (%)</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={analytics.riskDistribution}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                    <XAxis dataKey="name" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} angle={-45} textAnchor="end" height={100} />
                    <YAxis stroke="#94a3b8" style={{ fontSize: '0.75rem' }} tickFormatter={(value) => `${value.toFixed(0)}%`} />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      formatter={(value: number) => [`${value.toFixed(2)}%`, 'VAR %']}
                    />
                    <Bar dataKey="varPercentage" fill="#f87171" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          {activeTab === 'performance' && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '1.5rem' }}>
              {/* Risk vs Return Scatter */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Risk vs Return Analysis</h3>
                <ResponsiveContainer width="100%" height={400}>
                  <ScatterChart>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                    <XAxis type="number" dataKey="risk" name="Risk Score" stroke="#94a3b8" label={{ value: 'Risk Score', position: 'insideBottom', offset: -5, fill: '#94a3b8' }} />
                    <YAxis type="number" dataKey="return" name="Return %" stroke="#94a3b8" label={{ value: 'Return %', angle: -90, position: 'insideLeft', fill: '#94a3b8' }} />
                    <Tooltip
                      cursor={{ strokeDasharray: '3 3' }}
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      formatter={(value: number, name: string) => {
                        if (name === 'return') return [`${value.toFixed(2)}%`, 'Return'];
                        if (name === 'risk') return [value.toFixed(2), 'Risk Score'];
                        return [formatCurrency(value as number, 'USD', 0), 'Value'];
                      }}
                    />
                    <Scatter name="Portfolios" data={analytics.riskReturnData} fill="#38bdf8" />
                  </ScatterChart>
                </ResponsiveContainer>
              </div>

              {/* Volatility Analysis */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Top 15 Most Volatile Positions</h3>
                <ResponsiveContainer width="100%" height={400}>
                  <BarChart data={analytics.volatilityData} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                    <XAxis type="number" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} tickFormatter={(value) => `${value.toFixed(0)}%`} />
                    <YAxis type="category" dataKey="symbol" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} width={80} />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      formatter={(value: number) => [`${value.toFixed(2)}%`, 'Volatility']}
                    />
                    <Bar dataKey="volatility" fill="#a855f7" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          {activeTab === 'diversification' && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '1.5rem' }}>
              {/* Top Assets */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Top 10 Assets Across All Portfolios</h3>
                <ResponsiveContainer width="100%" height={400}>
                  <BarChart data={analytics.topAssets}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.1)" />
                    <XAxis dataKey="symbol" stroke="#94a3b8" style={{ fontSize: '0.75rem' }} />
                    <YAxis stroke="#94a3b8" style={{ fontSize: '0.75rem' }} tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`} />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(148, 163, 184, 0.2)', borderRadius: '0.5rem' }}
                      formatter={(value: number) => [formatCurrency(value, 'USD', 0), 'Value']}
                    />
                    <Bar dataKey="value" fill="#6366f1" />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Asset concentration table */}
              <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>Asset Concentration</h3>
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', fontSize: '0.875rem' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.2)' }}>
                        <th style={{ padding: '0.75rem', textAlign: 'left', color: '#94a3b8', fontWeight: '500' }}>Symbol</th>
                        <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Total Value</th>
                        <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>% of Total</th>
                        <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Portfolios</th>
                      </tr>
                    </thead>
                    <tbody>
                      {analytics.topAssets.map((asset, index) => (
                        <tr key={asset.symbol} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.1)' }}>
                          <td style={{ padding: '0.75rem', color: 'white', fontWeight: '500' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                              <span style={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: '1.5rem',
                                height: '1.5rem',
                                borderRadius: '50%',
                                backgroundColor: COLORS[index % COLORS.length],
                                fontSize: '0.625rem',
                                fontWeight: 'bold'
                              }}>
                                {index + 1}
                              </span>
                              {asset.symbol}
                            </div>
                          </td>
                          <td style={{ padding: '0.75rem', textAlign: 'right', color: 'white' }}>{formatCurrency(asset.value, 'USD', 0)}</td>
                          <td style={{ padding: '0.75rem', textAlign: 'right', color: '#38bdf8' }}>{asset.percentage.toFixed(2)}%</td>
                          <td style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8' }}>{asset.count}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'positions' && (
            <div style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid rgba(148, 163, 184, 0.1)' }}>
              <h3 style={{ fontSize: '1rem', fontWeight: '600', color: 'white', marginBottom: '1rem' }}>All Positions Summary</h3>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', fontSize: '0.875rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.2)' }}>
                      <th style={{ padding: '0.75rem', textAlign: 'left', color: '#94a3b8', fontWeight: '500' }}>Symbol</th>
                      <th style={{ padding: '0.75rem', textAlign: 'left', color: '#94a3b8', fontWeight: '500' }}>Portfolio</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Quantity</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Avg Price</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Current Price</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Market Value</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8', fontWeight: '500' }}>Gain/Loss</th>
                    </tr>
                  </thead>
                  <tbody>
                    {portfolios?.flatMap(portfolio =>
                      portfolio.positions.map(pos => {
                        const gainLoss = (pos.currentPrice - pos.averagePrice) * pos.quantity;
                        const gainLossPct = ((pos.currentPrice - pos.averagePrice) / pos.averagePrice) * 100;
                        return (
                          <tr key={`${portfolio.id}-${pos.id}`} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.1)' }}>
                            <td style={{ padding: '0.75rem', color: 'white', fontWeight: '500' }}>{pos.symbol}</td>
                            <td style={{ padding: '0.75rem', color: '#94a3b8' }}>{portfolio.name}</td>
                            <td style={{ padding: '0.75rem', textAlign: 'right', color: 'white' }}>{formatNumber(pos.quantity, 2)}</td>
                            <td style={{ padding: '0.75rem', textAlign: 'right', color: '#94a3b8' }}>{formatCurrency(pos.averagePrice, 'USD', 2)}</td>
                            <td style={{ padding: '0.75rem', textAlign: 'right', color: 'white' }}>{formatCurrency(pos.currentPrice, 'USD', 2)}</td>
                            <td style={{ padding: '0.75rem', textAlign: 'right', color: 'white', fontWeight: '500' }}>{formatCurrency(pos.marketValue, 'USD', 0)}</td>
                            <td style={{ padding: '0.75rem', textAlign: 'right', color: gainLoss >= 0 ? '#34d399' : '#f87171', fontWeight: '500' }}>
                              {gainLoss >= 0 ? '+' : ''}{formatCurrency(gainLoss, 'USD', 0)} ({gainLossPct.toFixed(2)}%)
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
