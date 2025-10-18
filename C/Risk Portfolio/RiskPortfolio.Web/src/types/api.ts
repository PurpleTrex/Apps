// API Types matching the backend Risk Portfolio API

export const RiskClassification = {
  Unspecified: 0,
  Low: 1,
  Moderate: 2,
  Elevated: 3,
  Critical: 4,
} as const;

export type RiskClassification = typeof RiskClassification[keyof typeof RiskClassification];

export interface PositionResponse {
  id: string;
  symbol: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  volatility: number;
  marketValue: number;
  riskContribution: number;
}

export interface PortfolioResponse {
  id: string;
  name: string;
  owner: string;
  baseCurrency: string;
  totalValue: number;
  riskScore: number;
  valueAtRisk: number;
  riskClassification: RiskClassification;
  createdAt: string;
  lastEvaluatedAt: string | null;
  positions: PositionResponse[];
}

export interface RiskAssessmentResponse {
  portfolioId: string;
  riskScore: number;
  totalValue: number;
  valueAtRisk: number;
  classification: RiskClassification;
  calculatedAt: string;
}

export interface CreatePortfolioPositionRequest {
  symbol: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  volatility: number;
}

export interface CreatePortfolioRequest {
  name: string;
  owner: string;
  baseCurrency?: string;
  positions?: CreatePortfolioPositionRequest[];
}

export interface AddOrUpdatePositionRequest {
  symbol: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  volatility: number;
}

export interface UpdatePortfolioOwnerRequest {
  owner: string;
}

export interface ApiError {
  message: string;
  errors?: Record<string, string[]>;
}
