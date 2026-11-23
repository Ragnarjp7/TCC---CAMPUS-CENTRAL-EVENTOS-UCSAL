import type { AuthUser, LeagueMembership, Papel, PermissaoCargo } from "./types";

export type Permission = "post:viewPrivate" | "event:enroll" | "post:create" | "event:create" | "event:review";

const hasPapel = (user: AuthUser, ...papeis: Papel[]) => papeis.some((p) => user.papeis.includes(p));

/** Ligas em que o cargo do usuário concede a permissão (ex.: para escolher a liga ao criar um evento). */
export function leaguesWith(user: AuthUser | null, permission: PermissaoCargo): LeagueMembership[] {
  return user?.leagues.filter((l) => l.permissions.includes(permission)) ?? [];
}

/** Professores e coordenadores podem criar eventos sem vínculo com uma liga (RF27). */
export const isInstitutional = (user: AuthUser | null) => user !== null && hasPapel(user, "PROFESSOR", "COORDENADOR");

// Regras espelham o backend: o papel global define o básico e o cargo na liga libera ações da liga.
// O backend continua sendo a autoridade; isto só controla o que a interface exibe.
const RULES: Record<Permission, (user: AuthUser) => boolean> = {
  "post:viewPrivate": () => true,
  // SecurityConfig: POST /eventos/*/inscricoes exige ALUNO
  "event:enroll": (user) => hasPapel(user, "ALUNO"),
  // RF27 / EventoService.criar: professor, coordenador ou presidência de liga (cargo com CRIAR_EVENTO)
  "event:create": (user) => isInstitutional(user) || leaguesWith(user, "CRIAR_EVENTO").length > 0,
  // RF29 / SecurityConfig: GET /eventos/analise e PUT /eventos/*/status exigem CAEX ou ADMIN
  "event:review": (user) => hasPapel(user, "CAEX", "ADMIN"),
  "post:create": (user) =>
    hasPapel(user, "ADMIN", "CAEX", "PROFESSOR", "COORDENADOR") || leaguesWith(user, "PUBLICAR").length > 0,
};

export function can(user: AuthUser | null, permission: Permission): boolean {
  return user !== null && RULES[permission](user);
}
