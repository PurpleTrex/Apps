// Widget Types

export const WidgetType = {
  // Portfolio Widgets
  PortfolioValue: 'PortfolioValue',
  PortfolioAllocation: 'PortfolioAllocation',
  PortfolioPerformance: 'PortfolioPerformance',
  PortfolioRiskGauge: 'PortfolioRiskGauge',

  // Position Widgets
  PositionList: 'PositionList',
  PositionCard: 'PositionCard',
  WatchList: 'WatchList',

  // Performance Widgets
  TopGainers: 'TopGainers',
  TopLosers: 'TopLosers',
  DailyPnL: 'DailyPnL',
  PerformanceChart: 'PerformanceChart',

  // Market Widgets
  MarketOverview: 'MarketOverview',
  SectorPerformance: 'SectorPerformance',
  MarketNews: 'MarketNews',
  EconomicCalendar: 'EconomicCalendar',

  // Risk Widgets
  RiskMetrics: 'RiskMetrics',
  ValueAtRisk: 'ValueAtRisk',
  CorrelationMatrix: 'CorrelationMatrix',
  VolatilityChart: 'VolatilityChart',

  // Analytics Widgets
  AssetAllocation: 'AssetAllocation',
  DiversificationScore: 'DiversificationScore',
  RebalancingNeeds: 'RebalancingNeeds',
  TaxLossHarvesting: 'TaxLossHarvesting',

  // Statistics Widgets
  QuickStats: 'QuickStats',
  ReturnsCalendar: 'ReturnsCalendar',
  Dividends: 'Dividends',
  Transactions: 'Transactions',

  // Custom Widgets
  CustomChart: 'CustomChart',
  Notes: 'Notes',
  Calculator: 'Calculator',
  Alerts: 'Alerts',
} as const;

export type WidgetType = typeof WidgetType[keyof typeof WidgetType];

export interface Widget {
  id: string;
  title: string;
  type: WidgetType;
  configuration: Record<string, any>;
  positionX: number;
  positionY: number;
  width: number;
  height: number;
  isVisible: boolean;
  displayOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWidgetRequest {
  title: string;
  type: string;
  configuration: string;
  positionX: number;
  positionY: number;
  width: number;
  height: number;
  displayOrder?: number;
}

export interface UpdateWidgetRequest {
  title?: string;
  configuration?: string;
  positionX?: number;
  positionY?: number;
  width?: number;
  height?: number;
  isVisible?: boolean;
  displayOrder?: number;
}

export interface WidgetTypeInfo {
  type: string;
  category: string;
  description: string;
  defaultWidth: number;
  defaultHeight: number;
}

export interface WidgetConfigProps<T = any> {
  widget: Widget;
  config: T;
  onUpdateConfig: (config: T) => void;
}
