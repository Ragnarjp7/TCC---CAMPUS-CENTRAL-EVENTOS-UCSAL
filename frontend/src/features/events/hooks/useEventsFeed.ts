import { useCallback } from "react";
import { keepPreviousData, useInfiniteQuery } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getErrorMessage } from "@/shared/api/errors";
import { resolveViewState } from "@/shared/state/viewState";
import { fetchEvents } from "../services/eventsService";
import type { EventFilters } from "../types";

export function useEventsFeed(filters: EventFilters) {
  const { user, isRestoring } = useAuth();
  const query = useInfiniteQuery({
    // o usuário entra na chave porque a resposta muda com o login (campo "inscrito")
    queryKey: ["events", "feed", filters, user?.id ?? null],
    queryFn: ({ pageParam }) => fetchEvents({ page: pageParam, ...filters }),
    initialPageParam: 0,
    getNextPageParam: (last) => (last.last ? undefined : last.number + 1),
    placeholderData: keepPreviousData,
    enabled: !isRestoring,
    select: (data) => data.pages.flatMap((page) => page.content),
  });

  const events = query.data ?? [];
  const { hasNextPage, isFetchingNextPage, fetchNextPage, refetch } = query;

  const loadMore = useCallback(() => {
    if (hasNextPage && !isFetchingNextPage) fetchNextPage();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const retry = useCallback(() => {
    void refetch();
  }, [refetch]);

  return {
    events,
    viewState: resolveViewState(query, events.length === 0),
    errorMessage: query.isError ? getErrorMessage(query.error) : undefined,
    retry,
    isRefetching: query.isRefetching && !isFetchingNextPage,
    isFetchingNextPage,
    loadMore,
  };
}
