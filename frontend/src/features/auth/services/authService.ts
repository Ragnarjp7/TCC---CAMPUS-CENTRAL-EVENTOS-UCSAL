import { apiRequest } from "@/shared/api/client";
import { isMockApi } from "@/shared/api/config";
import { ApiError } from "@/shared/api/errors";
import { tokenStorage } from "@/shared/storage/tokenStorage";
import type { AuthUser, LeagueMembership, LoginResponse, LoginResponseDTO, MeResponseDTO, Papel } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export function toAuthUser(dto: MeResponseDTO): AuthUser {
  return {
    id: dto.id,
    name: dto.nome,
    email: dto.email,
    papeis: dto.papeis ?? [],
    leagues: (dto.ligas ?? []).map((l) => ({
      leagueId: l.ligaId,
      leagueName: l.ligaNome,
      roleId: l.cargoId,
      roleName: l.cargoNome,
      permissions: l.permissoes ?? [],
    })),
  };
}

// --- Modo mock: o usuário digitado escolhe o perfil (espelha a carga inicial do backend) ---

const MOCK_TOKEN_PREFIX = "mock-jwt.";
const MOCK_LEAGUE = { leagueId: 1, leagueName: "Liga Acadêmica de Tecnologia" };
const ALL_PERMISSIONS: LeagueMembership["permissions"] = [
  "CRIAR_EVENTO",
  "EDITAR_EVENTO",
  "VER_INSCRITOS",
  "REGISTRAR_PRESENCA",
  "EMITIR_CERTIFICADO",
  "PUBLICAR",
  "GERENCIAR_MEMBROS",
  "GERENCIAR_CARGOS",
];

const MOCK_PROFILES: Record<string, Pick<AuthUser, "papeis" | "leagues">> = {
  admin: { papeis: ["ADMIN"], leagues: [] },
  caex: { papeis: ["CAEX"], leagues: [] },
  estagio: { papeis: ["CEPRO"], leagues: [] },
  professor: { papeis: ["PROFESSOR"], leagues: [] },
  coordenador: { papeis: ["PROFESSOR", "COORDENADOR"], leagues: [] },
  presidente: {
    papeis: ["ALUNO"],
    leagues: [{ ...MOCK_LEAGUE, roleId: 1, roleName: "Presidente", permissions: ALL_PERMISSIONS }],
  },
  ligante: { papeis: ["ALUNO"], leagues: [{ ...MOCK_LEAGUE, roleId: null, roleName: null, permissions: [] }] },
};

const DEFAULT_MOCK_PROFILE = { papeis: ["ALUNO"] as Papel[], leagues: [] };

const mockUser = (username: string): AuthUser => ({
  id: 1,
  name: username,
  email: `${username.toLowerCase()}@ucsal.edu.br`,
  ...(MOCK_PROFILES[username.toLowerCase()] ?? DEFAULT_MOCK_PROFILE),
});

export async function login(email: string, password: string): Promise<LoginResponse> {
  if (isMockApi) {
    await delay(600);
    if (!email.trim() || !password) throw new ApiError("client", 400, "Informe usuário e senha.");
    const name = email.trim();
    return { token: `${MOCK_TOKEN_PREFIX}${name}`, user: mockUser(name) };
  }

  try {
    const res = await apiRequest<LoginResponseDTO>("/auth/login", {
      method: "POST",
      body: { email: email.trim(), senha: password },
      skipUnauthorizedHandler: true,
    });
    return { token: res.token, user: toAuthUser(res.usuario) };
  } catch (error) {
    if (error instanceof ApiError && error.kind === "unauthorized") {
      throw new ApiError("unauthorized", 401, "E-mail ou senha inválidos.");
    }
    throw error;
  }
}

export async function fetchCurrentUser(): Promise<AuthUser> {
  if (isMockApi) {
    await delay(200);
    const token = await tokenStorage.get();
    if (!token?.startsWith(MOCK_TOKEN_PREFIX)) throw new ApiError("unauthorized", 401);
    return mockUser(token.slice(MOCK_TOKEN_PREFIX.length));
  }
  return toAuthUser(await apiRequest<MeResponseDTO>("/auth/me"));
}
