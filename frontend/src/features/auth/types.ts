/** Papéis globais do usuário (espelha backend: model/enums/Papel.java). */
export type Papel = "ALUNO" | "PROFESSOR" | "COORDENADOR" | "ADMIN" | "CAEX" | "CEPRO";

/** Permissões de cargo dentro de uma liga (espelha backend: model/enums/Permissao.java). */
export type PermissaoCargo =
  | "CRIAR_EVENTO"
  | "EDITAR_EVENTO"
  | "VER_INSCRITOS"
  | "REGISTRAR_PRESENCA"
  | "EMITIR_CERTIFICADO"
  | "PUBLICAR"
  | "GERENCIAR_MEMBROS"
  | "GERENCIAR_CARGOS";

/** Vínculo ativo do usuário com uma liga. Sem cargo = ligante padrão. */
export interface LeagueMembership {
  leagueId: number;
  leagueName: string;
  roleId: number | null;
  roleName: string | null;
  permissions: PermissaoCargo[];
}

export interface AuthUser {
  id: number;
  name: string;
  email: string;
  papeis: Papel[];
  leagues: LeagueMembership[];
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
}

/** Formato cru de GET /auth/me (DTOMeResponse). */
export interface MeResponseDTO {
  id: number;
  nome: string;
  email: string;
  papeis: Papel[];
  ligas?: {
    ligaId: number;
    ligaNome: string;
    cargoId: number | null;
    cargoNome: string | null;
    permissoes: PermissaoCargo[];
  }[];
}

/** Formato cru de POST /auth/login (DTOLoginResponse). */
export interface LoginResponseDTO {
  token: string;
  tipo: string;
  expiraEmMinutos: number;
  usuario: MeResponseDTO;
}
