import { apiRequest } from "@/shared/api/client";
import { isMockApi, PAGE_SIZE } from "@/shared/api/config";
import { ApiError } from "@/shared/api/errors";
import { paginate } from "@/shared/api/types";
import type { Page } from "@/shared/api/types";
import { EVENT_CATEGORIES, EVENTS } from "../data/mock";
import { EVENT_MONTHS, formatHour, parseLocalDateTime } from "../dates";
import type { CampusEvent, CategoriaDTO, EventCategory, EventFilters, EventMode, EventoDTO } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

/** Valor enviado em `modalidade`; o backend compara sem diferenciar maiúsculas. */
const MODE_PARAM: Record<EventMode, string> = { Presenciais: "presencial", Online: "online" };

export function toCampusEvent(dto: EventoDTO): CampusEvent {
  const start = parseLocalDateTime(dto.dataInicio);
  const end = parseLocalDateTime(dto.dataFim);

  return {
    id: dto.id,
    day: start ? String(start.day).padStart(2, "0") : "--",
    month: start ? EVENT_MONTHS[start.month - 1] : "JAN",
    title: dto.titulo,
    place: dto.local,
    mode: dto.modalidade?.toLowerCase().includes("online") ? "Online" : "Presenciais",
    organizer: dto.ligaNome ?? "UCSAL",
    time: start && end ? `${formatHour(start.hour, start.minute)} às ${formatHour(end.hour, end.minute)}` : "",
    description: dto.descricao,
    capacity: dto.capacidade ?? undefined,
    enrolled: dto.inscritos,
    startsAt: dto.dataInicio,
    endsAt: dto.dataFim,
    categoryId: dto.categoriaId ?? undefined,
    categoryName: dto.categoriaNome ?? undefined,
    isEnrolled: dto.inscrito ?? undefined,
    status: dto.status,
  };
}

export interface EventsQuery extends EventFilters {
  page: number;
}

// --- Modo mock: inscrições ficam só em memória ---

const mockEnrolled = new Set<number>();

const withMockEnrollment = (e: CampusEvent): CampusEvent => ({
  ...e,
  isEnrolled: mockEnrolled.has(e.id),
  enrolled: (e.enrolled ?? 0) + (mockEnrolled.has(e.id) ? 1 : 0),
});

const matchesSearch = (e: CampusEvent, search: string) => {
  const term = search.toLowerCase();
  return [e.title, e.place, e.organizer, e.description ?? ""].some((v) => v.toLowerCase().includes(term));
};

/** RF31/RF33: eventos disponíveis (aprovados e não encerrados), abertos a visitantes. */
export async function fetchEvents({ page, mode, categoryId, search }: EventsQuery): Promise<Page<CampusEvent>> {
  if (!isMockApi) {
    const res = await apiRequest<Page<EventoDTO>>("/eventos/publicos", {
      params: {
        page,
        size: PAGE_SIZE,
        modalidade: mode ? MODE_PARAM[mode] : null,
        categoriaId: categoryId,
        busca: search.trim() || null,
      },
    });
    return { ...res, content: res.content.map(toCampusEvent) };
  }

  await delay(400);
  const category = EVENT_CATEGORIES.find((c) => c.id === categoryId);
  const filtered = EVENTS.filter(
    (e) =>
      (!mode || e.mode === mode) &&
      (!category || e.kind === category.name) &&
      (!search.trim() || matchesSearch(e, search.trim())),
  );
  return paginate(filtered.map(withMockEnrollment), page, PAGE_SIZE);
}

export async function fetchEventById(id: number): Promise<CampusEvent> {
  if (!isMockApi) return toCampusEvent(await apiRequest<EventoDTO>(`/eventos/${id}`));

  await delay(250);
  const event = EVENTS.find((e) => e.id === id);
  if (!event) throw new ApiError("notFound", 404, "Evento não encontrado.");
  return withMockEnrollment(event);
}

/** Categorias usadas como filtro na lista de eventos (rota pública). */
export async function fetchEventCategories(): Promise<EventCategory[]> {
  if (!isMockApi) {
    const categorias = await apiRequest<CategoriaDTO[]>("/categorias");
    return categorias.map((c) => ({ id: c.id, name: c.nome }));
  }

  await delay(150);
  return EVENT_CATEGORIES.map((c) => ({ ...c }));
}

/** Inscreve o aluno logado (POST /eventos/{id}/inscricoes). */
export async function enrollInEvent(eventId: number): Promise<void> {
  if (!isMockApi) {
    await apiRequest(`/eventos/${eventId}/inscricoes`, { method: "POST" });
    return;
  }

  await delay(400);
  const event = EVENTS.find((e) => e.id === eventId);
  if (!event) throw new ApiError("notFound", 404, "Evento não encontrado.");
  if (mockEnrolled.has(eventId)) throw new ApiError("client", 409, "O aluno já está inscrito neste evento");
  if (event.capacity != null && (event.enrolled ?? 0) >= event.capacity) {
    throw new ApiError("client", 409, "O evento atingiu a capacidade máxima");
  }
  mockEnrolled.add(eventId);
}

/** Cancela a inscrição do aluno logado (DELETE /eventos/{id}/inscricoes). */
export async function cancelEnrollment(eventId: number): Promise<void> {
  if (!isMockApi) {
    await apiRequest(`/eventos/${eventId}/inscricoes`, { method: "DELETE" });
    return;
  }

  await delay(400);
  if (!mockEnrolled.delete(eventId)) throw new ApiError("notFound", 404, "Inscrição não encontrada");
}
