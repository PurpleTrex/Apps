import { useMemo } from 'react';
import type { PortfolioResponse } from '@/types/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import { formatCurrency, formatNumber, getRiskClassificationColor } from '@/lib/utils';

interface RiskDashboardProps {
  portfolio: PortfolioResponse;
}

export default function RiskDashboard({ portfolio }: RiskDashboardProps) {
  // Prepare data for allocation chart
  const allocationData = useMemo(() => {
    return portfolio.positions.map((position) => ({
      name: position.symbol,
      value: position.marketValue,
      percentage: (position.marketValue / portfolio.totalValue) * 100,
    }));
  }, [portfolio.positions, portfolio.totalValue]);

  // Prepare data for risk contribution chart
  const riskContributionData = useMemo(() => {
    return portfolio.positions
      .map((position) => ({
        name: position.symbol,
        riskContribution: position.riskContribution,
        marketValue: position.marketValue,
        volatility: position.volatility * 100,
      }))
      .sort((a, b) => b.riskContribution - a.riskContribution);
  }, [portfolio.positions]);

  // Generate colors for pie chart
  const COLORS = [
    '#3b82f6', // blue-500
    '#10b981', // green-500
    '#f59e0b', // amber-500
    '#ef4444', // red-500
    '#8b5cf6', // violet-500
    '#ec4899', // pink-500
    '#14b8a6', // teal-500
    '#f97316', // orange-500
  ];

  const CustomTooltip = ({ active, payload }: { active?: boolean; payload?: Array<{
    name: string;
    value: number;
    payload: {
      percentage?: number;
      volatility?: number;
      marketValue?: number;
    };
  }> }) => {
    if (active && payload && payload.length) {
      const data = payload[0];
      return (
        <div className="rounded-lg border border-slate-800/70 bg-slate-950/90 p-3 text-slate-200 shadow-[0_20px_45px_-24px_rgba(15,23,42,0.9)]">
          <p className="font-semibold text-white">{data.name}</p>
          {data.payload.percentage !== undefined && (
            <>
              <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
                Value: {formatCurrency(data.value, portfolio.baseCurrency)}
              </p>
              <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
                Allocation: {formatNumber(data.payload.percentage, 2)}%
              </p>
            </>
          )}
          {data.payload.volatility !== undefined && (
            <>
              <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
                Risk: {formatCurrency(data.value, portfolio.baseCurrency)}
              </p>
              <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
                Volatility: {formatNumber(data.payload.volatility, 2)}%
              </p>
            </>
          )}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Portfolio Allocation */}
      <Card>
        <CardHeader>
          <CardTitle className="text-xs font-semibold uppercase tracking-[0.36em] text-slate-500">
            Portfolio Allocation
          </CardTitle>
        </CardHeader>
        <CardContent>
          {portfolio.positions.length === 0 ? (
            <div className="flex h-64 items-center justify-center text-slate-500">
              No positions to display
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={allocationData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percentage }) =>
                    percentage > 5 ? `${name} ${formatNumber(percentage, 1)}%` : ''
                  }
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {allocationData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  wrapperStyle={{
                    color: 'rgb(148 163 184)',
                    fontSize: '0.75rem',
                    textTransform: 'uppercase',
                    letterSpacing: '0.18em',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {/* Risk Contribution by Position */}
      <Card>
        <CardHeader>
          <CardTitle className="text-xs font-semibold uppercase tracking-[0.36em] text-slate-500">
            Risk Contribution by Position
          </CardTitle>
        </CardHeader>
        <CardContent>
          {portfolio.positions.length === 0 ? (
            <div className="flex h-64 items-center justify-center text-slate-500">
              No positions to display
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={riskContributionData}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.2)" />
                <XAxis dataKey="name" stroke="rgba(148,163,184,0.6)" tick={{ fill: 'rgba(148,163,184,0.7)', fontSize: 12 }} />
                <YAxis stroke="rgba(148,163,184,0.6)" tick={{ fill: 'rgba(148,163,184,0.7)', fontSize: 12 }} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="riskContribution" fill="url(#riskGradient)" />
                <defs>
                  <linearGradient id="riskGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="rgba(249,115,22,0.9)" />
                    <stop offset="100%" stopColor="rgba(251,191,36,0.6)" />
                  </linearGradient>
                </defs>
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {/* Risk Metrics Summary */}
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle className="text-xs font-semibold uppercase tracking-[0.36em] text-slate-500">
            Risk Metrics Summary
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '1.5rem' }}>
            <div
              className="rounded-xl border border-slate-800/70 bg-slate-950/50 p-4"
              style={{
                boxShadow: `0 12px 30px -18px rgba(15,23,42,0.8), inset 0 0 0 1px ${getRiskClassificationColor(portfolio.riskClassification)}22`,
              }}
            >
              <p className="text-[0.55rem] font-semibold uppercase tracking-[0.45em] text-slate-500">
                Risk Classification
              </p>
              <p className="mt-3 text-2xl font-semibold text-white">
                {portfolio.riskClassification === 0 ? 'Unspecified' :
                 portfolio.riskClassification === 1 ? 'Low' :
                 portfolio.riskClassification === 2 ? 'Moderate' :
                 portfolio.riskClassification === 3 ? 'Elevated' : 'Critical'}
              </p>
            </div>

            <div className="rounded-xl border border-slate-800/70 bg-slate-950/50 p-4">
              <p className="text-[0.55rem] font-semibold uppercase tracking-[0.45em] text-slate-500">
                Total Positions
              </p>
              <p className="mt-3 text-2xl font-semibold text-white">
                {portfolio.positions.length}
              </p>
            </div>

            <div className="rounded-xl border border-slate-800/70 bg-slate-950/50 p-4">
              <p className="text-[0.55rem] font-semibold uppercase tracking-[0.45em] text-slate-500">
                Average Volatility
              </p>
              <p className="mt-3 text-2xl font-semibold text-white">
                {portfolio.positions.length > 0
                  ? formatNumber(
                      (portfolio.positions.reduce((sum, p) => sum + p.volatility, 0) /
                        portfolio.positions.length) *
                        100,
                      2
                    )
                  : '0.00'}
                %
              </p>
            </div>

            <div className="rounded-xl border border-slate-800/70 bg-slate-950/50 p-4">
              <p className="text-[0.55rem] font-semibold uppercase tracking-[0.45em] text-slate-500">
                VaR % of Portfolio
              </p>
              <p className="mt-3 text-2xl font-semibold text-white">
                {portfolio.totalValue > 0
                  ? formatNumber((portfolio.valueAtRisk / portfolio.totalValue) * 100, 2)
                  : '0.00'}
                %
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
