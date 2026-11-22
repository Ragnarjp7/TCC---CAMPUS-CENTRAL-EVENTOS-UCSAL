import { keepPreviousData, useInfiniteQuery } from "@tanstack/react-query";
import { getErrorMessage } from "@/shared/api/errors";
import { resolveViewState } from "@/shared/state/viewState";
import { fetchPosts } from "../services/postsService";
import type { PostCategory } from "../types";

interface UsePostsFeedOptions {
  search: string;
  category: PostCategory | null;
  userId: number | null;
  enabled?: boolean;
}

export function usePostsFeed({ search, category, userId, enabled = true }: UsePostsFeedOptions) {
  const query = useInfiniteQuery({
    queryKey: ["posts", { userId, search, category }],
    queryFn: ({ pageParam }) => fetchPosts({ page: pageParam, search, category, authenticated: userId !== null }),
    enabled,
    initialPageParam: 0,
    getNextPageParam: (last) => (last.last ? undefined : last.number + 1),
    placeholderData: keepPreviousData,
    select: (data) => data.pages.flatMap((page) => page.content),
  });

  const posts = query.data ?? [];

  return {
    posts,
    viewState: resolveViewState(query, posts.length === 0),
    errorMessage: query.isError ? getErrorMessage(query.error) : undefined,
    retry: () => {
      void query.refetch();
    },
    isRefetching: query.isRefetching && !query.isFetchingNextPage,
    hasNextPage: query.hasNextPage,
    isFetchingNextPage: query.isFetchingNextPage,
    fetchNextPage: query.fetchNextPage,
  };
}
