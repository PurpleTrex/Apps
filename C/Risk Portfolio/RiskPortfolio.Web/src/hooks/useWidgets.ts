import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { widgetApi } from '@/lib/api-client';
import type { CreateWidgetRequest, UpdateWidgetRequest } from '@/types/widgets';

export const WIDGET_KEYS = {
  all: ['widgets'] as const,
  lists: () => [...WIDGET_KEYS.all, 'list'] as const,
  list: (visibleOnly: boolean) => [...WIDGET_KEYS.lists(), { visibleOnly }] as const,
  details: () => [...WIDGET_KEYS.all, 'detail'] as const,
  detail: (id: string) => [...WIDGET_KEYS.details(), id] as const,
  types: () => [...WIDGET_KEYS.all, 'types'] as const,
};

export function useWidgets(visibleOnly = false) {
  return useQuery({
    queryKey: WIDGET_KEYS.list(visibleOnly),
    queryFn: () => widgetApi.getAll(visibleOnly),
  });
}

export function useWidget(id: string) {
  return useQuery({
    queryKey: WIDGET_KEYS.detail(id),
    queryFn: () => widgetApi.getById(id),
    enabled: !!id,
  });
}

export function useWidgetTypes() {
  return useQuery({
    queryKey: WIDGET_KEYS.types(),
    queryFn: () => widgetApi.getTypes(),
  });
}

export function useCreateWidget() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateWidgetRequest) => widgetApi.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WIDGET_KEYS.lists() });
    },
  });
}

export function useUpdateWidget() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, request }: { id: string; request: UpdateWidgetRequest }) =>
      widgetApi.update(id, request),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: WIDGET_KEYS.lists() });
      queryClient.invalidateQueries({ queryKey: WIDGET_KEYS.detail(data.id) });
    },
  });
}

export function useDeleteWidget() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => widgetApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WIDGET_KEYS.lists() });
    },
  });
}

export function useUpdateWidgetPositions() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (widgets: Array<{ id: string; positionX: number; positionY: number; displayOrder: number }>) =>
      widgetApi.updatePositions(widgets),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WIDGET_KEYS.lists() });
    },
  });
}
