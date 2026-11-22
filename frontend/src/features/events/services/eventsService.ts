import { apiRequest } from "@/shared/api/client";
import { isMockApi, PAGE_SIZE } from "@/shared/api/config";
import { paginate } from "@/shared/api/types";
import type { Page } from "@/shared/api/types";
import { EVENTS } from "../data/mock";
import type { CampusEvent, EventMode } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export interface EventsQuery {
  page: number;
  mode: EventMode | null;
}

export async function fetchEvents({ page, mode }: EventsQuery): Promise<Page<CampusEvent>> {
  if (!isMockApi) {
    return apiRequest<Page<CampusEvent>>("/events", { params: { page, size: PAGE_SIZE, mode } });
  }

  await delay(400);
  return paginate(mode ? EVENTS.filter((e) => e.mode === mode) : EVENTS, page, PAGE_SIZE);
}

export async function fetchEventById(id: number): Promise<CampusEvent> {
  if (!isMockApi) return apiRequest<CampusEvent>(`/events/${id}`);

  await delay(250);
  const event = EVENTS.find((e) => e.id === id);
  if (!event) throw new Error("Evento não encontrado.");
  return { ...event };
}
