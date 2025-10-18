import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { portfolioApi } from '@/lib/api-client';
import type {
  CreatePortfolioRequest,
  AddOrUpdatePositionRequest,
  UpdatePortfolioOwnerRequest,
} from '@/types/api';

// Query keys
export const portfolioKeys = {
  all: ['portfolios'] as const,
  lists: () => [...portfolioKeys.all, 'list'] as const,
  list: (filters: string) => [...portfolioKeys.lists(), { filters }] as const,
  details: () => [...portfolioKeys.all, 'detail'] as const,
  detail: (id: string) => [...portfolioKeys.details(), id] as const,
};

// Get all portfolios
export function usePortfolios() {
  return useQuery({
    queryKey: portfolioKeys.lists(),
    queryFn: () => portfolioApi.getAll(),
  });
}

// Get single portfolio
export function usePortfolio(id: string) {
  return useQuery({
    queryKey: portfolioKeys.detail(id),
    queryFn: () => portfolioApi.getById(id),
    enabled: !!id,
  });
}

// Create portfolio
export function useCreatePortfolio() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePortfolioRequest) => portfolioApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: portfolioKeys.lists() });
    },
  });
}

// Update portfolio owner
export function useUpdatePortfolioOwner() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdatePortfolioOwnerRequest }) =>
      portfolioApi.updateOwner(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: portfolioKeys.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: portfolioKeys.lists() });
    },
  });
}

// Add or update position
export function useAddOrUpdatePosition() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: AddOrUpdatePositionRequest }) =>
      portfolioApi.addOrUpdatePosition(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: portfolioKeys.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: portfolioKeys.lists() });
    },
  });
}

// Remove position
export function useRemovePosition() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, positionId }: { id: string; positionId: string }) =>
      portfolioApi.removePosition(id, positionId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: portfolioKeys.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: portfolioKeys.lists() });
    },
  });
}

// Delete portfolio
export function useDeletePortfolio() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => portfolioApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: portfolioKeys.lists() });
    },
  });
}

// Recalculate risk
export function useRecalculateRisk() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => portfolioApi.recalculateRisk(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: portfolioKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: portfolioKeys.lists() });
    },
  });
}
