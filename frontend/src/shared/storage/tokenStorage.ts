import * as SecureStore from "expo-secure-store";

const TOKEN_KEY = "campus.auth.token";

export const tokenStorage = {
  get: async (): Promise<string | null> => {
    try {
      return await SecureStore.getItemAsync(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set: (token: string): Promise<void> => SecureStore.setItemAsync(TOKEN_KEY, token),
  clear: async (): Promise<void> => {
    try {
      await SecureStore.deleteItemAsync(TOKEN_KEY);
    } catch {
      // keystore indisponível: não há o que limpar
    }
  },
};
