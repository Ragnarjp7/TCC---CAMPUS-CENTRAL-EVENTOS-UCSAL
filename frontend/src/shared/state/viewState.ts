export type ViewState = "loading" | "error" | "empty" | "ready";

interface QueryLike {
  data: unknown;
  isError: boolean;
}

export function resolveViewState(query: QueryLike, isEmpty: boolean): ViewState {
  if (query.data === undefined) return query.isError ? "error" : "loading";
  return isEmpty ? "empty" : "ready";
}
