/**
 * RF27/RF29 - Envio de eventos para a CAEX e análise pela CAEX.
 *
 * Fluxo: criar (AGUARDANDO_ANALISE) → CAEX aprova, pede ajustes ou rejeita →
 * com ajustes, o responsável edita e reenvia (volta para AGUARDANDO_ANALISE) → aprovado aparece no feed.
 */
import type { AuthUser } from "@/features/auth/types";
import { apiRequest } from "@/shared/api/client";
import { isMockApi } from "@/shared/api/config";
import { ApiError } from "@/shared/api/errors";
import { EVENT_CATEGORIES, EVENTS } from "../data/mock";
import { EVENT_MONTHS, formatHour, parseLocalDateTime } from "../dates";
import type {
  CampusEvent,
  EventoAnaliseDTO,
  EventoRequestDTO,
  EventStatus,
  EventSubmission,
  ReviewDecision,
} from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export function toSubmission(dto: EventoAnaliseDTO): EventSubmission {
  return {
    id: dto.id,
    title: dto.titulo,
    description: dto.descricao,
    place: dto.local,
    startsAt: dto.dataInicio,
    endsAt: dto.dataFim,
    mode: dto.modalidade?.toLowerCase().includes("online") ? "Online" : "Presenciais",
    status: dto.status,
    capacity: dto.capacidade,
    categoryId: dto.categoriaId,
    categoryName: dto.categoriaNome,
    leagueId: dto.ligaId,
    leagueName: dto.ligaNome,
    responsible: { id: dto.responsavelId, name: dto.responsavelNome ?? "Não informado", email: dto.responsavelEmail },
    caexNote: dto.observacaoCaex,
    proposal: dto.proposta,
    submittedAt: dto.enviadoEm ?? dto.criadoEm,
  };
}

// ---------- API ----------

/** Eventos pelos quais o usuário logado responde (GET /eventos/meus). */
export async function fetchMyEvents(user: AuthUser): Promise<EventSubmission[]> {
  if (!isMockApi) return (await apiRequest<EventoAnaliseDTO[]>("/eventos/meus")).map(toSubmission);

  await delay(300);
  return mock.list.filter((s) => s.responsible.email === user.email).map(clone);
}

/** Fila da CAEX (GET /eventos/analise?status=...). */
export async function fetchReviewQueue(status: EventStatus): Promise<EventSubmission[]> {
  if (!isMockApi) {
    return (await apiRequest<EventoAnaliseDTO[]>("/eventos/analise", { params: { status } })).map(toSubmission);
  }

  await delay(300);
  return mock.list
    .filter((s) => s.status === status)
    .sort((a, b) => (a.submittedAt ?? "").localeCompare(b.submittedAt ?? ""))
    .map(clone);
}

/** Evento com a proposta completa (GET /eventos/{id}/analise). */
export async function fetchSubmission(id: number): Promise<EventSubmission> {
  if (!isMockApi) return toSubmission(await apiRequest<EventoAnaliseDTO>(`/eventos/${id}/analise`));

  await delay(200);
  return clone(findMock(id));
}

/** RF27 - Cadastra o evento; ele entra na fila da CAEX (POST /eventos). */
export async function createEvent(body: EventoRequestDTO, author: AuthUser): Promise<EventSubmission> {
  if (!isMockApi) return toSubmission(await apiRequest<EventoAnaliseDTO>("/eventos", { method: "POST", body }));

  await delay(500);
  const now = localNowIso();
  const created: EventSubmission = {
    ...fromRequest(body, mock.nextId++),
    status: "AGUARDANDO_ANALISE",
    responsible: { id: author.id, name: author.name, email: author.email },
    caexNote: null,
    submittedAt: now,
  };
  mock.list.push(created);
  return clone(created);
}

/** Reenvia o evento após ajustes; volta para a fila da CAEX (PUT /eventos/{id}). */
export async function resubmitEvent(id: number, body: EventoRequestDTO): Promise<EventSubmission> {
  if (!isMockApi) return toSubmission(await apiRequest<EventoAnaliseDTO>(`/eventos/${id}`, { method: "PUT", body }));

  await delay(500);
  const current = findMock(id);
  if (current.status !== "AJUSTES_SOLICITADOS") {
    throw new ApiError("client", 409, "Somente eventos com ajustes solicitados pelo CAEX podem ser editados");
  }
  Object.assign(current, fromRequest(body, id), {
    leagueId: current.leagueId,
    leagueName: current.leagueName,
    status: "AGUARDANDO_ANALISE",
    submittedAt: localNowIso(),
  });
  return clone(current);
}

/** RF29 - Decisão da CAEX, com justificativa obrigatória (PUT /eventos/{id}/status). */
export async function decideEvent(id: number, decision: ReviewDecision, note: string): Promise<void> {
  if (!isMockApi) {
    await apiRequest(`/eventos/${id}/status`, { method: "PUT", body: { status: decision, observacao: note.trim() } });
    return;
  }

  await delay(400);
  const current = findMock(id);
  if (current.status !== "AGUARDANDO_ANALISE") {
    throw new ApiError("client", 409, `Não é possível mudar o status de ${current.status} para ${decision}`);
  }
  if (!note.trim()) throw new ApiError("client", 400, "A observação é obrigatória para registrar a decisão do CAEX");

  current.status = decision;
  current.caexNote = note.trim();
  // no mock, o evento aprovado passa a aparecer no feed público
  if (decision === "APROVADO" && !EVENTS.some((e) => e.id === id)) EVENTS.push(toMockCampusEvent(current));
}

// ---------- Modo mock: tudo em memória ----------

const clone = (s: EventSubmission): EventSubmission => ({ ...s, responsible: { ...s.responsible } });

const pad = (n: number) => String(n).padStart(2, "0");

const toLocalIso = (d: Date) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;

const localNowIso = () => toLocalIso(new Date());

/** Data daqui a `days` dias, no horário indicado (para os exemplos estarem sempre no futuro). */
const inDays = (days: number, hour: number) => {
  const d = new Date();
  d.setDate(d.getDate() + days);
  d.setHours(hour, 0, 0, 0);
  return toLocalIso(d);
};

function fromRequest(body: EventoRequestDTO, id: number): Omit<EventSubmission, "status" | "responsible" | "caexNote" | "submittedAt"> {
  return {
    id,
    title: body.titulo,
    description: body.descricao,
    place: body.local,
    startsAt: body.dataInicio,
    endsAt: body.dataFim,
    mode: body.modalidade === "online" ? "Online" : "Presenciais",
    capacity: body.capacidade,
    categoryId: body.categoriaId,
    categoryName: EVENT_CATEGORIES.find((c) => c.id === body.categoriaId)?.name ?? null,
    leagueId: body.ligaId,
    leagueName: body.ligaId != null ? "Liga Acadêmica de Tecnologia" : null,
    proposal: body.proposta,
  };
}

function toMockCampusEvent(s: EventSubmission): CampusEvent {
  const start = parseLocalDateTime(s.startsAt);
  const end = parseLocalDateTime(s.endsAt);
  return {
    id: s.id,
    day: start ? pad(start.day) : "--",
    month: start ? EVENT_MONTHS[start.month - 1] : "JAN",
    title: s.title,
    place: s.place,
    mode: s.mode,
    organizer: s.leagueName ?? s.responsible.name,
    time: start && end ? `${formatHour(start.hour, start.minute)} às ${formatHour(end.hour, end.minute)}` : "",
    description: s.description,
    capacity: s.capacity ?? undefined,
    enrolled: 0,
    startsAt: s.startsAt,
    endsAt: s.endsAt,
    categoryId: s.categoryId ?? undefined,
    categoryName: s.categoryName ?? undefined,
    status: "APROVADO",
  };
}

function findMock(id: number): EventSubmission {
  const found = mock.list.find((s) => s.id === id);
  if (!found) throw new ApiError("notFound", 404, "Evento não encontrado");
  return found;
}


const mockCategoryId = (name: string) => EVENT_CATEGORIES.find((c) => c.name === name)?.id ?? null;

const mock = {
  nextId: 1001,
  list: [
    {
      id: 901,
      title: "Hackathon de Soluções para o Campus",
      description:
        "Maratona de 12 horas para criar soluções digitais para problemas reais do campus, com mentoria de professores e premiação para as três melhores equipes.",
      place: "Campus da UCSAL - Pituaçu · Laboratório de Redes",
      startsAt: inDays(12, 8),
      endsAt: inDays(12, 20),
      mode: "Presenciais",
      status: "AGUARDANDO_ANALISE",
      capacity: 80,
      categoryId: mockCategoryId("Workshop"),
      categoryName: "Workshop",
      leagueId: 1,
      leagueName: "Liga Acadêmica de Tecnologia",
      responsible: { id: 2, name: "Maria Souza", email: "maria@ucsal.edu.br" },
      caexNote: null,
      proposal: null,
      submittedAt: inDays(-2, 10),
    },
    {
      id: 902,
      title: "Ciclo de Debates: Ética e Inteligência Artificial",
      description: "Encontro com convidados para discutir os impactos éticos e jurídicos do uso de IA na sociedade.",
      place: "Online",
      startsAt: inDays(20, 19),
      endsAt: inDays(20, 21),
      mode: "Online",
      status: "AGUARDANDO_ANALISE",
      capacity: null,
      categoryId: mockCategoryId("Palestra"),
      categoryName: "Palestra",
      leagueId: null,
      leagueName: null,
      responsible: { id: 3, name: "Prof. Carlos Lima", email: "carlos@ucsal.edu.br" },
      caexNote: null,
      proposal: null,
      submittedAt: inDays(-1, 15),
    },
  ] as EventSubmission[],
};
