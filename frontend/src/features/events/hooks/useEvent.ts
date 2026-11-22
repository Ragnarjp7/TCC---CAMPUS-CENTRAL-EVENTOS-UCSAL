import { useQuery } from "@tanstack/react-query";
import { fetchEventById } from "../services/eventsService";

/** Carrega um evento pelo id; só dispara quando `enabled` e houver id. */
export function useEvent(id: number | null, enabled = true) {
  return useQuery({
    queryKey: ["events", "detail", id],
    queryFn: () => fetchEventById(id as number),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}
