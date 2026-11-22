import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, Text, TextInput, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import { ChevronLeft } from "lucide-react-native";
import { Logo } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";
import { useLoginForm } from "../hooks/useLoginForm";

const underlined = "border-b border-slate-300 pb-2 pt-1 text-sm text-slate-800";

export default function LoginScreen({ navigation }: RootScreenProps<"Login">) {
  const form = useLoginForm({ onSuccess: () => navigation.reset({ index: 0, routes: [{ name: "Main" }] }) });
  const loading = form.viewState === "loading";
  const goBack = () =>
    navigation.canGoBack() ? navigation.goBack() : navigation.reset({ index: 0, routes: [{ name: "Main" }] });

  return (
    <SafeAreaView testID="login-screen" className="flex-1 bg-brand">
      <StatusBar style="light" />
      <View className="flex-row px-4 pt-2">
        <Pressable
          testID="login-back-button"
          accessibilityRole="button"
          accessibilityLabel="Voltar"
          hitSlop={8}
          onPress={goBack}
          className="size-9 items-center justify-center rounded-full bg-white/15 active:opacity-70"
        >
          <ChevronLeft size={22} color="#ffffff" />
        </Pressable>
      </View>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} className="flex-1">
        <ScrollView
          testID="login-scroll"
          contentContainerClassName="flex-grow justify-center px-6 py-8"
          keyboardShouldPersistTaps="handled"
        >
          <View className="mb-8 items-center"><Logo testID="login-logo" light size="md" /></View>

          <View testID="login-form" className="gap-6 rounded-2xl bg-white p-6">
            <View>
              <Text testID="login-username-label" className="text-xs font-bold text-slate-900">Usuário</Text>
              <TextInput
                testID="login-username-input"
                value={form.username}
                onChangeText={form.setUsername}
                autoCapitalize="none"
                autoCorrect={false}
                placeholderTextColor={colors.slate400}
                className={underlined}
              />
            </View>
            <View>
              <Text testID="login-password-label" className="text-xs font-bold text-slate-900">Senha</Text>
              <TextInput
                testID="login-password-input"
                value={form.password}
                onChangeText={form.setPassword}
                secureTextEntry
                placeholderTextColor={colors.slate400}
                className={underlined}
              />
            </View>

            {form.error && <Text testID="login-error-message" className="text-xs text-red-600">{form.error}</Text>}

            <Pressable
              testID="login-submit-button"
              accessibilityRole="button"
              disabled={loading}
              onPress={form.submit}
              className="h-11 items-center justify-center rounded-lg bg-brand active:opacity-80"
            >
              {loading ? (
                <ActivityIndicator testID="login-submit-loading" color="#ffffff" />
              ) : (
                <Text className="text-sm font-semibold text-white">Entrar</Text>
              )}
            </Pressable>
          </View>

          <Pressable testID="login-guest-button" onPress={() => navigation.goBack()} className="items-center pt-6">
            <Text className="text-sm font-medium text-white">Continuar sem login</Text>
          </Pressable>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
