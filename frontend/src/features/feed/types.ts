export const POST_CATEGORIES = [
  "Esporte",
  "Ligas",
  "Empresa Júnior",
  "Extensão",
  "Acadêmico",
  "Cultural",
] as const;

export type PostCategory = (typeof POST_CATEGORIES)[number];

export type PostVisibility = "public" | "private";

export interface Post {
  id: number;
  author: string;
  category: PostCategory;
  createdAt: string;
  title: string;
  body: string;
  imageUrl: string | null;
  visibility: PostVisibility;
}
