export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export function paginate<T>(items: readonly T[], page: number, size: number): Page<T> {
  const start = page * size;
  const totalPages = Math.max(1, Math.ceil(items.length / size));
  return {
    content: items.slice(start, start + size),
    number: page,
    size,
    totalElements: items.length,
    totalPages,
    last: page >= totalPages - 1,
  };
}
