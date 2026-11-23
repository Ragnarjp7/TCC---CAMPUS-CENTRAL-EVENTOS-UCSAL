import { apiRequest } from "@/shared/api/client";
import { isMockApi } from "@/shared/api/config";
import { fetchEventById } from "@/features/events/services/eventsService";
import { POSTS } from "@/features/feed/data/mock";
import type { Post } from "@/features/feed/types";
import { ENROLLMENTS, MEMBERS } from "../data/mock";
import type { Enrollment, GroupMember, InscricaoDTO, MembroLigaDTO } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

const toGroupMember = (dto: MembroLigaDTO): GroupMember => ({
  id: dto.id,
  name: dto.alunoNome,
  email: dto.alunoEmail,
  groupId: dto.ligaId,
  groupName: dto.ligaNome,
  roleName: dto.cargoNome,
});

/** Publicações dos grupos do usuário, mais recentes primeiro. */
export async function fetchGroupPosts(groupIds: number[]): Promise<Post[]> {
  if (groupIds.length === 0) return [];
  // TODO: o backend ainda não tem posts; combinar o endpoint (ex.: GET /posts?ligaIds=1,2).
  if (!isMockApi) return apiRequest<Post[]>("/posts", { params: { ligaIds: groupIds.join(",") } });

  await delay(400);
  return POSTS.filter((p) => p.leagueId != null && groupIds.includes(p.leagueId))
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .map((p) => ({ ...p }));
}

/** Membros ativos de todos os grupos do usuário. */
export async function fetchGroupMembers(groupIds: number[]): Promise<GroupMember[]> {
  if (groupIds.length === 0) return [];
  if (!isMockApi) {
    const lists = await Promise.all(groupIds.map((id) => apiRequest<MembroLigaDTO[]>(`/ligas/${id}/membros`)));
    return lists.flat().filter((m) => m.status === "ATIVO").map(toGroupMember);
  }

  await delay(400);
  return MEMBERS.filter((m) => groupIds.includes(m.groupId)).map((m) => ({ ...m }));
}

/** Eventos em que o usuário se inscreveu ou participou. */
export async function fetchMyEnrollments(): Promise<Enrollment[]> {
  if (!isMockApi) {
    const inscricoes = await apiRequest<InscricaoDTO[]>("/alunos/me/inscricoes");
    return Promise.all(
      inscricoes.map(async (i) => ({
        id: i.id,
        // TODO: o DTO ainda não informa presença; quando informar, mapear PARTICIPOU/AUSENTE aqui.
        status: "INSCRITO" as const,
        enrolledAt: i.dataInscricao,
        event: await fetchEventById(i.eventoId),
      })),
    );
  }

  await delay(400);
  return Promise.all(
    ENROLLMENTS.map(async ({ eventId, ...rest }) => ({ ...rest, event: await fetchEventById(eventId) })),
  );
}
