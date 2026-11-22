import { useCallback, useState } from "react";
import { useDebouncedValue } from "@/shared/hooks/useDebouncedValue";
import { useAuth } from "@/features/auth/context/AuthContext";
import type { PostCategory } from "../types";
import { usePostsFeed } from "./usePostsFeed";

export function useHomeScreen() {
  const { user, isRestoring } = useAuth();
  const [query, setQuery] = useState("");
  const search = useDebouncedValue(query.trim());
  const [category, setCategory] = useState<PostCategory | null>(null);
  const feed = usePostsFeed({ search, category, userId: user?.id ?? null, enabled: !isRestoring });

  const toggleCategory = useCallback((value: PostCategory) => {
    setCategory((current) => (current === value ? null : value));
  }, []);

  const { hasNextPage, isFetchingNextPage, fetchNextPage } = feed;
  const loadMore = useCallback(() => {
    if (hasNextPage && !isFetchingNextPage) fetchNextPage();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  return {
    query,
    setQuery,
    category,
    toggleCategory,
    isAuthenticated: user !== null,
    posts: feed.posts,
    viewState: feed.viewState,
    errorMessage: feed.errorMessage,
    retry: feed.retry,
    isRefetching: feed.isRefetching,
    isFetchingNextPage,
    loadMore,
  };
}
