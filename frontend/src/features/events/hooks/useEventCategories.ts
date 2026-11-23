import { useQuery } from "@tanstack/react-query";
import { fetchEventCategories } from "../services/eventsService";

/** Categorias para o filtro da lista de eventos; falha aqui só esconde o filtro. */
export function useEventCategories() {
  const query = useQuery({
    queryKey: ["events", "categories"],
    queryFn: fetchEventCategories,
    staleTime: 10 * 60_000,
  });
  return query.data ?? [];
}
