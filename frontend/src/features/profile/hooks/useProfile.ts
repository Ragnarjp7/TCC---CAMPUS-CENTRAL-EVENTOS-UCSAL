import { useCallback } from "react";
import { useQuery, type UseQueryResult } from "@tanstack/react-query";
import { getErrorMessage } from "@/shared/api/errors";
import { resolveViewState } from "@/shared/state/viewState";
import { fetchGroupMembers, fetchGroupPosts, fetchMyEnrollments } from "../services/profileService";

/** Formato comum das três abas: dados + estado da tela + ações. */
function useListState<T>(query: UseQueryResult<T[]>) {
  const items = query.data ?? [];
  const { refetch } = query;
  const retry = useCallback(() => {
    void refetch();
  }, [refetch]);

  return {
    items,
    viewState: resolveViewState(query, items.length === 0),
    errorMessage: query.isError ? getErrorMessage(query.error) : undefined,
    retry,
    isRefetching: query.isRefetching,
  };
}

export function useGroupPosts(groupIds: number[], enabled: boolean) {
  return useListState(
    useQuery({ queryKey: ["profile", "posts", groupIds], queryFn: () => fetchGroupPosts(groupIds), enabled }),
  );
}

export function useGroupMembers(groupIds: number[], enabled: boolean) {
  return useListState(
    useQuery({ queryKey: ["profile", "members", groupIds], queryFn: () => fetchGroupMembers(groupIds), enabled }),
  );
}

export function useMyEnrollments(enabled: boolean) {
  return useListState(useQuery({ queryKey: ["profile", "enrollments"], queryFn: fetchMyEnrollments, enabled }));
}
