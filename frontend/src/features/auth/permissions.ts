import type { AuthUser, Role } from "./types";

export type Permission = "post:viewPrivate" | "event:enroll" | "post:create" | "event:create";

const ROLE_PERMISSIONS: Record<Role, readonly Permission[]> = {
  STUDENT: ["post:viewPrivate", "event:enroll"],
  TEACHER: ["post:viewPrivate", "event:enroll", "post:create", "event:create"],
  ADMIN: ["post:viewPrivate", "event:enroll", "post:create", "event:create"],
};

export function can(user: AuthUser | null, permission: Permission): boolean {
  return user !== null && ROLE_PERMISSIONS[user.role].includes(permission);
}
