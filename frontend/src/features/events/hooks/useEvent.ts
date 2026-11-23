import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import { fetchEventById } from "../services/eventsService";
import type { CampusEvent } from "../types";

interface UseEventOptions {
  enabled?: boolean;
  /** Dados já conhecidos (ex.: vindos da lista) para renderizar na hora enquanto atualiza. */
  initialData?: CampusEvent;
}

export const eventDetailKey = (id: number | null, userId: number | null) => ["events", "detail", id, userId] as const;

/** Carrega um evento pelo id; só dispara quando `enabled` e houver id. */
export function useEvent(id: number | null, { enabled = true, initialData }: UseEventOptions = {}) {
  const { user } = useAuth();
  return useQuery({
    // o usuário entra na chave porque a resposta muda com o login (campo "inscrito")
    queryKey: eventDetailKey(id, user?.id ?? null),
    queryFn: () => fetchEventById(id as number),
    enabled: enabled && id != null,
    staleTime: 60_000,
    initialData,
    // força refetch em segundo plano quando o dado inicial veio da lista
    initialDataUpdatedAt: initialData ? 0 : undefined,
  });
}
