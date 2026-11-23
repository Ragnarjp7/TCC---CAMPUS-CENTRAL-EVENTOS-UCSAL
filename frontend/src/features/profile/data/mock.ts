import type { GroupMember } from "../types";

const LEAGUE = { groupId: 1, groupName: "Liga Acadêmica de Tecnologia" };

export const MEMBERS: GroupMember[] = [
  { id: 1, name: "Cloves Neto", email: "cloves.neto@ucsal.edu.br", roleName: "Presidente", ...LEAGUE },
  { id: 2, name: "Ana Silva", email: "ana.silva@ucsal.edu.br", roleName: "Vice-Presidente", ...LEAGUE },
  { id: 3, name: "Cleber Pereira", email: "cleber.pereira@ucsal.edu.br", roleName: "Tesoureiro", ...LEAGUE },
  { id: 4, name: "Marta Alves", email: "marta.alves@ucsal.edu.br", roleName: null, ...LEAGUE },
  { id: 5, name: "Fernando Melo", email: "fernando.melo@ucsal.edu.br", roleName: null, ...LEAGUE },
  { id: 6, name: "Juliana Costa", email: "juliana.costa@ucsal.edu.br", roleName: null, ...LEAGUE },
];

const daysAgo = (d: number) => new Date(Date.now() - d * 86_400_000).toISOString();

/** Inscrições do usuário logado: eventId + situação. */
export const ENROLLMENTS = [
  { id: 1, eventId: 5, status: "INSCRITO", enrolledAt: daysAgo(2) },
  { id: 2, eventId: 1, status: "INSCRITO", enrolledAt: daysAgo(6) },
  { id: 3, eventId: 2, status: "PARTICIPOU", enrolledAt: daysAgo(15) },
  { id: 4, eventId: 3, status: "AUSENTE", enrolledAt: daysAgo(20) },
] as const;
