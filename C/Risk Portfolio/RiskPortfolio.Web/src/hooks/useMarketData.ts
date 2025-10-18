import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { marketDataService } from '@/services/marketData';

/**
 * Hook to fetch market quote for a single symbol
 */
export function useMarketQuote(symbol: string, enabled = true) {
  return useQuery({
    queryKey: ['market-quote', symbol],
    queryFn: () => marketDataService.getQuote(symbol),
    enabled: enabled && !!symbol,
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 5 * 60 * 1000, // Auto-refresh every 5 minutes
  });
}

/**
 * Hook to fetch quotes for multiple symbols
 */
export function useBatchQuotes(symbols: string[], enabled = true) {
  return useQuery({
    queryKey: ['market-quotes-batch', symbols],
    queryFn: () => marketDataService.getBatchQuotes(symbols),
    enabled: enabled && symbols.length > 0,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook to refresh position prices
 */
export function useRefreshPositions() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (symbols: string[]) => marketDataService.refreshPositions(symbols),
    onSuccess: () => {
      // Invalidate portfolio queries to refetch with new prices
      queryClient.invalidateQueries({ queryKey: ['portfolios'] });
      queryClient.invalidateQueries({ queryKey: ['portfolio'] });
    },
  });
}

/**
 * Hook to get current price for a symbol
 */
export function useMarketPrice(symbol: string, enabled = true) {
  return useQuery({
    queryKey: ['market-price', symbol],
    queryFn: () => marketDataService.getPrice(symbol),
    enabled: enabled && !!symbol,
    staleTime: 5 * 60 * 1000, // 5 minutes
    select: (data) => data.price,
  });
}

/**
 * Hook to search for symbols by keyword
 */
export function useSymbolSearch(keyword: string, enabled = true) {
  return useQuery({
    queryKey: ['symbol-search', keyword],
    queryFn: () => marketDataService.searchSymbols(keyword),
    enabled: enabled && !!keyword && keyword.length >= 1,
    staleTime: 30 * 60 * 1000, // 30 minutes - search results don't change often
  });
}
