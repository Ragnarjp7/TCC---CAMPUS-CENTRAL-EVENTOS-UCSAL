import { QueryClient } from "@tanstack/react-query";
import { ApiError } from "./errors";

const MAX_RETRIES = 2;
const RETRYABLE = new Set(["network", "timeout", "server"]);

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: (failureCount, error) => {
        if (error instanceof ApiError && !RETRYABLE.has(error.kind)) return false;
        return failureCount < MAX_RETRIES;
      },
    },
  },
});
