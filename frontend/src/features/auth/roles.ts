import type { AuthUser, Papel } from "./types";

export const PAPEL_LABELS: Record<Papel, string> = {
  ADMIN: "Administrador(a)",
  CAEX: "CAEX",
  CEPRO: "Núcleo de Estágio",
  COORDENADOR: "Coordenador(a)",
  PROFESSOR: "Professor(a)",
  ALUNO: "Estudante",
};

// Do papel mais amplo para o mais restrito: o usuário pode ter vários (ex.: PROFESSOR + COORDENADOR)
const PAPEL_PRIORITY: Papel[] = ["ADMIN", "CAEX", "CEPRO", "COORDENADOR", "PROFESSOR"];

/**
 * Texto curto da função do usuário no app.
 * Papéis institucionais têm prioridade; para estudantes, mostra o cargo na liga
 * (ex.: "Presidente · Liga Acadêmica de Tecnologia"), "Ligante" ou "Estudante".
 */
export function getRoleLabel(user: AuthUser): string {
  const papel = PAPEL_PRIORITY.find((p) => user.papeis.includes(p));
  if (papel) return PAPEL_LABELS[papel];

  const withRole = user.leagues.filter((l) => l.roleName);
  if (withRole.length > 0) {
    const [first] = withRole;
    const extra = withRole.length > 1 ? ` +${withRole.length - 1}` : "";
    return `${first.roleName} · ${first.leagueName}${extra}`;
  }

  if (user.leagues.length > 0) return `Ligante · ${user.leagues[0].leagueName}`;
  return PAPEL_LABELS.ALUNO;
}
