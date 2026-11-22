import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Button } from "@/shared/components/ui";
import type { TabScreenProps } from "@/navigation/types";
import { useAuth } from "../context/AuthContext";

export default function ProfileScreen({ navigation }: TabScreenProps<"Perfil">) {
  const { user, signOut } = useAuth();

  return (
    <SafeAreaView testID="profile-screen" edges={["top"]} className="flex-1 bg-slate-50">
      <View testID="profile-content" className="flex-1 items-center justify-center gap-4 px-8">
        {user ? (
          <>
            <Text testID="profile-greeting" className="text-lg font-bold text-slate-900">Olá, {user.name}!</Text>
            <Button testID="profile-signout-button" title="Sair" variant="outline" onPress={() => void signOut()} />
          </>
        ) : (
          <>
            <Text testID="profile-guest-message" className="text-center text-sm text-slate-500">
              Entre para acessar conteúdos exclusivos da comunidade acadêmica.
            </Text>
            <Button testID="profile-signin-button" title="Entrar" onPress={() => navigation.navigate("Login")} />
          </>
        )}
      </View>
    </SafeAreaView>
  );
}
