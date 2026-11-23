import type { CampusEvent } from "@/features/events/types";

/** Pessoa que tem a mesma "tag" (liga, empresa júnior, atlética...) que o usuário. */
export interface GroupMember {
  /** Id do vínculo (MembroLiga). */
  id: number;
  name: string;
  email: string;
  groupId: number;
  groupName: string;
  /** Cargo no grupo; null = ligante/membro sem cargo. */
  roleName: string | null;
}

/**
 * INSCRITO: evento ainda vai acontecer.
 * PARTICIPOU: presença registrada.
 * AUSENTE: o evento passou e não houve presença.
 */
export type EnrollmentStatus = "INSCRITO" | "PARTICIPOU" | "AUSENTE";

export interface Enrollment {
  id: number;
  status: EnrollmentStatus;
  /** ISO 8601. */
  enrolledAt: string;
  event: CampusEvent;
}

/** Formato cru de GET /ligas/{ligaId}/membros (DTOMembroLigaResponse). */
export interface MembroLigaDTO {
  id: number;
  alunoId: number;
  alunoNome: string;
  alunoEmail: string;
  matricula: string;
  ligaId: number;
  ligaNome: string;
  status: "SOLICITADO" | "ATIVO" | "RECUSADO" | "INATIVO";
  cargoId: number | null;
  cargoNome: string | null;
}

/** Formato cru de GET /alunos/me/inscricoes (DTOInscricaoResponse). */
export interface InscricaoDTO {
  id: number;
  alunoId: number;
  alunoNome: string;
  eventoId: number;
  eventoTitulo: string;
  dataInscricao: string;
}
