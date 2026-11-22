import { useCallback } from "react";
import { useQuery } from "@tanstack/react-query";
import { getErrorMessage } from "@/shared/api/errors";
import { resolveViewState } from "@/shared/state/viewState";
import { fetchCertificates } from "../services/certificatesService";
import type { Certificate } from "../types";

/** Eventos mais recentes primeiro. */
const newestFirst = (items: Certificate[]) =>
  [...items].sort((a, b) => new Date(b.dataEvento).getTime() - new Date(a.dataEvento).getTime());

export function useCertificates(enabled: boolean) {
  const query = useQuery({
    queryKey: ["certificates"],
    queryFn: fetchCertificates,
    select: newestFirst,
    enabled,
  });

  const certificates = query.data ?? [];
  const { refetch } = query;

  const retry = useCallback(() => {
    void refetch();
  }, [refetch]);

  return {
    certificates,
    viewState: resolveViewState(query, certificates.length === 0),
    errorMessage: query.isError ? getErrorMessage(query.error) : undefined,
    retry,
    isRefetching: query.isRefetching,
  };
}
