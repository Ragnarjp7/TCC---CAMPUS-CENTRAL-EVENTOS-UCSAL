import { useEffect } from "react";
import { Pressable } from "react-native";
import { StatusBar } from "expo-status-bar";
import { useAuth } from "@/features/auth/context/AuthContext";
import { Logo } from "@/shared/components/ui";
import type { RootScreenProps } from "@/navigation/types";

export default function SplashScreen({ navigation }: RootScreenProps<"Splash">) {
  const { isRestoring } = useAuth();

  useEffect(() => {
    if (isRestoring) return;
    const t = setTimeout(() => navigation.replace("Main"), 1800);
    return () => clearTimeout(t);
  }, [navigation, isRestoring]);

  return (
    <Pressable
      testID="splash-screen"
      onPress={() => {
        if (!isRestoring) navigation.replace("Main");
      }}
      className="flex-1 items-center justify-center bg-brand"
    >
      <StatusBar style="light" />
      <Logo testID="splash-logo" light size="xl" />
    </Pressable>
  );
}
