import { useCallback } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getErrorMessage } from "@/shared/api/errors";
import { resolveViewState } from "@/shared/state/viewState";
import { fetchNotifications, markNotificationAsRead } from "../services/notificationsService";
import type { AppNotification } from "../types";

const QUERY_KEY = ["notifications"] as const;

/** Mais recentes primeiro (ordem inversa de chegada). */
const newestFirst = (items: AppNotification[]) =>
  [...items].sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime());

export function useNotifications(enabled: boolean) {
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: QUERY_KEY,
    queryFn: fetchNotifications,
    select: newestFirst,
    enabled,
  });

  const markAsRead = useMutation({
    mutationFn: markNotificationAsRead,
    onMutate: (id) => {
      queryClient.setQueryData<AppNotification[]>(QUERY_KEY, (old) =>
        old?.map((n) => (n.id === id ? { ...n, lida: true } : n)),
      );
    },
    onError: () => queryClient.invalidateQueries({ queryKey: QUERY_KEY }),
  });

  const notifications = query.data ?? [];
  const { refetch } = query;

  const retry = useCallback(() => {
    void refetch();
  }, [refetch]);

  return {
    notifications,
    viewState: resolveViewState(query, notifications.length === 0),
    errorMessage: query.isError ? getErrorMessage(query.error) : undefined,
    retry,
    isRefetching: query.isRefetching,
    markAsRead: markAsRead.mutate,
  };
}
