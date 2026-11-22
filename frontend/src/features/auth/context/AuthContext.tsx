import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import type { ReactNode } from "react";
import { Alert } from "react-native";
import { useQueryClient } from "@tanstack/react-query";
import { setUnauthorizedHandler } from "@/shared/api/client";
import { tokenStorage } from "@/shared/storage/tokenStorage";
import { fetchCurrentUser, login } from "../services/authService";
import type { AuthUser } from "../types";

interface AuthContextValue {
  user: AuthUser | null;
  isRestoring: boolean;
  signIn: (username: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isRestoring, setIsRestoring] = useState(true);
  const userRef = useRef<AuthUser | null>(null);

  const updateUser = useCallback(
    (next: AuthUser | null) => {
      userRef.current = next;
      setUser(next);
      queryClient.removeQueries();
    },
    [queryClient],
  );

  useEffect(() => {
    setUnauthorizedHandler(() => {
      const hadSession = userRef.current !== null;
      updateUser(null);
      if (hadSession) Alert.alert("Sessão expirada", "Entre novamente para continuar.");
    });
    return () => setUnauthorizedHandler(null);
  }, [updateUser]);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        if (!(await tokenStorage.get())) return;
        const restored = await fetchCurrentUser();
        if (active) updateUser(restored);
      } catch {
        // 401 já limpa o token no cliente HTTP; falha de rede mantém o app como visitante nesta sessão
      } finally {
        if (active) setIsRestoring(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [updateUser]);

  const signIn = useCallback(
    async (username: string, password: string) => {
      const session = await login(username, password);
      await tokenStorage.set(session.token);
      updateUser(session.user);
    },
    [updateUser],
  );

  const signOut = useCallback(async () => {
    await tokenStorage.clear();
    updateUser(null);
  }, [updateUser]);

  const value = useMemo(() => ({ user, isRestoring, signIn, signOut }), [user, isRestoring, signIn, signOut]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth deve ser usado dentro de AuthProvider");
  return ctx;
}
