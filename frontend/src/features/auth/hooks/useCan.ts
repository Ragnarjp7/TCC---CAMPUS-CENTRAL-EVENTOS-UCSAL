import { useAuth } from "../context/AuthContext";
import { can } from "../permissions";
import type { Permission } from "../permissions";

export function useCan(permission: Permission): boolean {
  const { user } = useAuth();
  return can(user, permission);
}
