import { apiRequest } from "@/shared/api/client";
import { isMockApi, PAGE_SIZE } from "@/shared/api/config";
import { paginate } from "@/shared/api/types";
import type { Page } from "@/shared/api/types";
import { POSTS } from "../data/mock";
import type { Post, PostCategory } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export interface PostsQuery {
  page: number;
  search: string;
  category: PostCategory | null;
  authenticated: boolean;
}

// O backend filtra a visibilidade pelo JWT; o filtro local existe só para o mock.
export async function fetchPosts({ page, search, category, authenticated }: PostsQuery): Promise<Page<Post>> {
  if (!isMockApi) {
    return apiRequest<Page<Post>>("/posts", { params: { page, size: PAGE_SIZE, search: search.trim(), category } });
  }

  await delay(500);
  const term = search.trim().toLowerCase();
  const matches = POSTS.filter(
    (p) =>
      (authenticated || p.visibility === "public") &&
      (!category || p.category === category) &&
      (!term || `${p.title} ${p.author} ${p.body}`.toLowerCase().includes(term)),
  );
  return paginate(matches, page, PAGE_SIZE);
}
