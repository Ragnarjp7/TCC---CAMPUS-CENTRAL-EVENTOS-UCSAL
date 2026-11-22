import { apiRequest } from "@/shared/api/client";
import { isMockApi } from "@/shared/api/config";
import { ApiError } from "@/shared/api/errors";
import { tokenStorage } from "@/shared/storage/tokenStorage";
import type { AuthUser, LoginResponse, Role } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

const MOCK_TOKEN_PREFIX = "mock-jwt.";
const MOCK_ROLES: Record<string, Role> = { professor: "TEACHER", admin: "ADMIN" };

const mockUser = (username: string): AuthUser => ({
  id: 1,
  name: username,
  role: MOCK_ROLES[username.toLowerCase()] ?? "STUDENT",
});

export async function login(username: string, password: string): Promise<LoginResponse> {
  if (isMockApi) {
    await delay(600);
    if (!username.trim() || !password) throw new ApiError("client", 400, "Informe usuário e senha.");
    const name = username.trim();
    return { token: `${MOCK_TOKEN_PREFIX}${name}`, user: mockUser(name) };
  }

  try {
    return await apiRequest<LoginResponse>("/auth/login", {
      method: "POST",
      body: { username, password },
      skipUnauthorizedHandler: true,
    });
  } catch (error) {
    if (error instanceof ApiError && error.kind === "unauthorized") {
      throw new ApiError("unauthorized", 401, "Usuário ou senha inválidos.");
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
  return apiRequest<AuthUser>("/auth/me");
}
