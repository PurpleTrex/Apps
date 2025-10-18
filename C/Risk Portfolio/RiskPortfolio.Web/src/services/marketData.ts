import apiClient from '@/lib/api-client';

export interface MarketQuote {
  symbol: string;
  price: number;
  change?: number;
  changePercent?: number;
  volume?: number;
  high?: number;
  low?: number;
  open?: number;
  previousClose?: number;
  lastUpdated: string;
  volatility?: number;
}

export interface PriceResponse {
  symbol: string;
  price: number;
}

export interface BatchPricesRequest {
  symbols: string[];
}

export interface SymbolSearchResult {
  symbol: string;
  name: string;
  type: string;
  region: string;
  currency: string;
  marketOpen?: string;
  marketClose?: string;
  timezone?: string;
}

export const marketDataService = {
  /**
   * Get current price for a single symbol
   */
  async getPrice(symbol: string): Promise<PriceResponse> {
    const response = await apiClient.get<PriceResponse>(`/market-data/${symbol}/price`);
    return response.data;
  },

  /**
   * Get detailed quote for a single symbol
   */
  async getQuote(symbol: string): Promise<MarketQuote> {
    const response = await apiClient.get<MarketQuote>(`/market-data/${symbol}/quote`);
    return response.data;
  },

  /**
   * Get quotes for multiple symbols
   */
  async getBatchQuotes(symbols: string[]): Promise<Record<string, MarketQuote>> {
    const response = await apiClient.post<Record<string, MarketQuote>>('/market-data/quotes', {
      symbols,
    });
    return response.data;
  },

  /**
   * Refresh prices for multiple positions
   */
  async refreshPositions(symbols: string[]): Promise<Record<string, number>> {
    const response = await apiClient.post<Record<string, number>>('/market-data/refresh-positions', {
      symbols,
    });
    return response.data;
  },

  /**
   * Search for symbols by keyword
   */
  async searchSymbols(keyword: string): Promise<SymbolSearchResult[]> {
    const response = await apiClient.get<SymbolSearchResult[]>(`/market-data/search`, {
      params: { keyword },
    });
    return response.data;
  },
};
