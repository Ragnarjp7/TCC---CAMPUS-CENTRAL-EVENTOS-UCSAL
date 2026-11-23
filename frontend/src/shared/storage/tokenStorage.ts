import { Platform } from "react-native";
import * as SecureStore from "expo-secure-store";

const TOKEN_KEY = "campus.auth.token";

// expo-secure-store não tem implementação web: no navegador usamos localStorage.
const isWeb = Platform.OS === "web";

export const tokenStorage = {
  get: async (): Promise<string | null> => {
    try {
      if (isWeb) return globalThis.localStorage?.getItem(TOKEN_KEY) ?? null;
      return await SecureStore.getItemAsync(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set: async (token: string): Promise<void> => {
    if (isWeb) {
      globalThis.localStorage?.setItem(TOKEN_KEY, token);
      return;
    }
    await SecureStore.setItemAsync(TOKEN_KEY, token);
  },
  clear: async (): Promise<void> => {
    try {
      if (isWeb) {
        globalThis.localStorage?.removeItem(TOKEN_KEY);
        return;
      }
      await SecureStore.deleteItemAsync(TOKEN_KEY);
    } catch {
      // keystore indisponível: não há o que limpar
    }
  },
};
