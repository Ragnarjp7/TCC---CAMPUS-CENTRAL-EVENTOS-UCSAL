import { useCallback } from "react";
import { keepPreviousData, useInfiniteQuery } from "@tanstack/react-query";
import { getErrorMessage } from "@/shared/api/errors";
import { resolveViewState } from "@/shared/state/viewState";
import { fetchEvents } from "../services/eventsService";
import type { EventMode } from "../types";

export function useEventsFeed(mode: EventMode | null) {
  const query = useInfiniteQuery({
    queryKey: ["events", { mode }],
    queryFn: ({ pageParam }) => fetchEvents({ page: pageParam, mode }),
    initialPageParam: 0,
    getNextPageParam: (last) => (last.last ? undefined : last.number + 1),
    placeholderData: keepPreviousData,
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
