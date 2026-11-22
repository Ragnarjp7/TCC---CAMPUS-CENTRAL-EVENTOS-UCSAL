export type Role = "STUDENT" | "TEACHER" | "ADMIN";

export interface AuthUser {
  id: number;
  name: string;
  role: Role;
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
}
