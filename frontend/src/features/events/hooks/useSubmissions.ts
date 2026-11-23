import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import {
  createEvent,
  decideEvent,
  fetchMyEvents,
  fetchReviewQueue,
  fetchSubmission,
  resubmitEvent,
} from "../services/submissionsService";
import type { EventoRequestDTO, EventStatus, ReviewDecision } from "../types";

export const submissionKeys = {
  mine: (userId: number | null) => ["events", "mine", userId] as const,
  review: (status: EventStatus) => ["events", "review", status] as const,
  detail: (id: number) => ["events", "submission", id] as const,
};

/** Mudanças de situação afetam o feed, o detalhe, a fila da CAEX e "Meus eventos": tudo sob ["events"]. */
const useInvalidateEvents = () => {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ["events"] });
};

/** RF27 - Eventos pelos quais o usuário responde. */
export function useMyEvents() {
  const { user } = useAuth();
  return useQuery({
    queryKey: submissionKeys.mine(user?.id ?? null),
    queryFn: () => fetchMyEvents(user!),
    enabled: user !== null,
    staleTime: 15_000,
  });
}

/** RF29 - Fila de análise da CAEX por situação. */
export function useReviewQueue(status: EventStatus) {
  return useQuery({
    queryKey: submissionKeys.review(status),
    queryFn: () => fetchReviewQueue(status),
    staleTime: 15_000,
  });
}

/** Evento com a proposta completa (análise ou edição). */
export function useSubmission(id: number | null) {
  return useQuery({
    queryKey: submissionKeys.detail(id ?? -1),
    queryFn: () => fetchSubmission(id as number),
    enabled: id != null,
    staleTime: 0,
  });
}

/** Cria (sem `eventId`) ou reenvia após ajustes (com `eventId`). */
export function useSubmitEvent(eventId: number | null) {
  const { user } = useAuth();
  const invalidate = useInvalidateEvents();
  return useMutation({
    mutationFn: (body: EventoRequestDTO) => (eventId != null ? resubmitEvent(eventId, body) : createEvent(body, user!)),
    onSuccess: () => void invalidate(),
  });
}

/** RF29 - Aprovar, pedir ajustes ou rejeitar. */
export function useReviewDecision(eventId: number) {
  const invalidate = useInvalidateEvents();
  return useMutation({
    mutationFn: ({ decision, note }: { decision: ReviewDecision; note: string }) => decideEvent(eventId, decision, note),
    // mesmo em erro (ex.: outro analista já decidiu), atualiza para mostrar a situação real
    onSettled: () => void invalidate(),
  });
}
