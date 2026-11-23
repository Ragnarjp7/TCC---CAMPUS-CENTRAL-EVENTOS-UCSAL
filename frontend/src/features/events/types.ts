export type EventMode = "Presenciais" | "Online";

export type EventMonth = "JAN" | "FEV" | "MAR" | "ABR" | "MAI" | "JUN" | "JUL" | "AGO" | "SET" | "OUT" | "NOV" | "DEZ";

export interface EventCategory {
  id: number;
  name: string;
}

/** Filtros da consulta de eventos disponíveis (RF31). Todos opcionais. */
export interface EventFilters {
  mode: EventMode | null;
  categoryId: number | null;
  search: string;
}

export type EventKind = "Palestra" | "Workshop" | "Oficina" | "Minicurso" | "Seminário" | "Encontro" | "Semana acadêmica";

export interface EventSpeaker {
  name: string;
  /** Cargo, formação ou empresa. */
  role?: string;
}

export interface CampusEvent {
  id: number;
  day: string;
  month: EventMonth;
  title: string;
  place: string;
  mode: EventMode;
  organizer: string;
  time: string;
  /** Texto da seção "Sobre o evento". */
  description?: string;
  /** Total de vagas; ausente = sem limite. */
  capacity?: number;
  /** Quantidade de inscritos até o momento. */
  enrolled?: number;
  /** Tipo de atividade (palestra, workshop...). */
  kind?: EventKind;
  /** Horas complementares concedidas a quem participar. */
  complementaryHours?: number;
  /** Se emite certificado de participação. */
  certificate?: boolean;
  /** Valor da inscrição em reais; 0 ou ausente = gratuito. */
  price?: number;
  /** Público-alvo (ex.: "Todos os cursos", "Alunos de Computação"). */
  audience?: string;
  /** Pré-requisitos ou o que levar. */
  requirements?: string;
  speakers?: EventSpeaker[];
  /** Início/fim em ISO 8601 (horário local, sem fuso), quando vier da API. */
  startsAt?: string;
  endsAt?: string;
  categoryId?: number;
  categoryName?: string;
  /** Se o aluno logado está inscrito; ausente = não se aplica (visitante ou não aluno). */
  isEnrolled?: boolean;
  /** Situação na análise do CAEX; ausente no modo mock (= aprovado). */
  status?: EventStatus;
}

/** Situação do evento (espelha backend: Evento.StatusEvento). */
export type EventStatus = "AGUARDANDO_ANALISE" | "APROVADO" | "AJUSTES_SOLICITADOS" | "REJEITADO" | "CANCELADO" | "ENCERRADO";

/** Formato cru de GET /eventos/publicos e GET /eventos/{id} (DTOEventoResponse). */
export interface EventoDTO {
  id: number;
  titulo: string;
  descricao: string;
  local: string;
  dataInicio: string;
  dataFim: string;
  modalidade: string;
  status: EventStatus;
  capacidade: number | null;
  inscritos: number;
  categoriaId: number | null;
  categoriaNome: string | null;
  ligaId: number | null;
  ligaNome: string | null;
  inscrito: boolean | null;
}

/**
 * Formato cru de GET /eventos/meus, GET /eventos/analise e GET /eventos/{id}/analise (DTOEventoAnaliseResponse).
 * Só chega ao CAEX/ADMIN e a quem responde pelo evento.
 */
export interface EventoAnaliseDTO {
  id: number;
  titulo: string;
  descricao: string;
  local: string;
  dataInicio: string;
  dataFim: string;
  modalidade: string;
  status: EventStatus;
  capacidade: number | null;
  categoriaId: number | null;
  categoriaNome: string | null;
  ligaId: number | null;
  ligaNome: string | null;
  responsavelId: number | null;
  responsavelNome: string | null;
  responsavelEmail: string | null;
  observacaoCaex: string | null;
  /** JSON da proposta completa (ver proposal.ts). */
  proposta: string | null;
  criadoEm: string | null;
  enviadoEm: string | null;
}

/** Corpo de POST /eventos e PUT /eventos/{id} (DTOEventoRequest). */
export interface EventoRequestDTO {
  titulo: string;
  descricao: string;
  local: string;
  dataInicio: string;
  dataFim: string;
  modalidade: string;
  capacidade: number | null;
  categoriaId: number;
  ligaId: number | null;
  proposta: string;
}

/** Evento submetido ao CAEX, como as telas de acompanhamento e análise usam (RF27/RF29). */
export interface EventSubmission {
  id: number;
  title: string;
  description: string;
  place: string;
  /** ISO 8601 em horário local, sem fuso. */
  startsAt: string;
  endsAt: string;
  mode: EventMode;
  status: EventStatus;
  capacity: number | null;
  categoryId: number | null;
  categoryName: string | null;
  leagueId: number | null;
  leagueName: string | null;
  responsible: { id: number | null; name: string; email: string | null };
  /** Justificativa da última decisão do CAEX. */
  caexNote: string | null;
  /** JSON cru da proposta completa; ver `parseStoredProposal`. */
  proposal: string | null;
  submittedAt: string | null;
}

/** Decisões que o CAEX registra na análise (PUT /eventos/{id}/status). */
export type ReviewDecision = "APROVADO" | "AJUSTES_SOLICITADOS" | "REJEITADO";

/** Formato cru de GET /categorias. */
export interface CategoriaDTO {
  id: number;
  nome: string;
  descricao: string | null;
}
