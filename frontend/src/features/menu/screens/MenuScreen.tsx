import { Pressable, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import Constants from "expo-constants";
import type { LucideIcon } from "lucide-react-native";
import {
  Accessibility,
  ArrowRight,
  Award,
  Bell,
  CircleHelp,
  FileText,
  Info,
  Lock,
  LogIn,
  Smartphone,
  User,
  UserCog,
  X,
} from "lucide-react-native";
import { useAuth } from "@/features/auth/context/AuthContext";
import type { Role } from "@/features/auth/types";
import type { RootScreenProps } from "@/navigation/types";
import { colors } from "@/shared/theme/colors";

interface MenuItem {
  key: string;
  label: string;
  icon: LucideIcon;
  value?: string;
  onPress?: () => void;
}

interface MenuSection {
  key: string;
  title: string;
  items: MenuItem[];
}

const CloseButton = ({ onPress }: { onPress: () => void }) => (
  <Pressable
    testID="menu-close-button"
    accessibilityRole="button"
    accessibilityLabel="Fechar menu"
    hitSlop={12}
    onPress={onPress}
    className="self-start rounded-full p-1 active:bg-white/20"
  >
    <X size={24} color={colors.white} />
  </Pressable>
);

const APP_VERSION = Constants.expoConfig?.version ?? "1.0.0";

const MenuRow = ({ item, last }: { item: MenuItem; last: boolean }) => {
  const Icon = item.icon;
  return (
    <Pressable
      testID={`menu-item-${item.key}`}
      accessibilityRole={item.onPress ? "button" : "text"}
      accessibilityLabel={item.label}
      disabled={!item.onPress}
      onPress={item.onPress}
      className={`flex-row items-center gap-3 px-4 py-3.5 active:bg-white/10 ${last ? "" : "border-b border-white/25"}`}
    >
      <Icon size={18} color={colors.white} />
      <Text className="flex-1 text-sm font-semibold text-white">{item.label}</Text>
      {item.value && <Text className="text-xs text-white/80">{item.value}</Text>}
      {item.onPress && <ArrowRight size={16} color={colors.white} />}
    </Pressable>
  );
};

export default function MenuScreen({ navigation }: RootScreenProps<"Menu">) {
  const { user, signOut } = useAuth();

  const openPlaceholder = (title: string) => () => navigation.navigate("Placeholder", { title });

  const accessibilityItem: MenuItem = {
    key: "accessibility",
    label: "Acessibilidade",
    icon: Accessibility,
    onPress: openPlaceholder("Acessibilidade"),
  };

  // Visitantes só veem os itens que não dependem de conta
  const settingsItems: MenuItem[] = user
    ? [
        { key: "certificates", label: "Certificados", icon: Award, onPress: () => navigation.navigate("Certificates") },
        { key: "profile", label: "Gerenciar Perfil", icon: UserCog, onPress: () => navigation.navigate("Main", { screen: "Perfil" }) },
        { key: "security", label: "Senha e Segurança", icon: Lock, onPress: openPlaceholder("Senha e Segurança") },
        { key: "notifications", label: "Notificações", icon: Bell, onPress: () => navigation.navigate("Main", { screen: "Notificações" }) },
        accessibilityItem,
      ]
    : [accessibilityItem];

  const sections: MenuSection[] = [
    { key: "settings", title: "Configurações", items: settingsItems },
    {
      key: "more",
      title: "Mais",
      items: [
        { key: "about", label: "Sobre Nós", icon: Info, onPress: openPlaceholder("Sobre Nós") },
        { key: "help", label: "Central de Ajuda", icon: CircleHelp, onPress: openPlaceholder("Central de Ajuda") },
        { key: "terms", label: "Termos de Uso", icon: FileText, onPress: openPlaceholder("Termos de Uso") },
        { key: "version", label: "Versão do App", icon: Smartphone, value: APP_VERSION },
      ],
    },
  ];

  const handleSignOut = async () => {
    await signOut();
    navigation.goBack();
  };

  return (
    <SafeAreaView testID="menu-screen" edges={["top", "bottom"]} className="flex-1 bg-slate-100">
      <StatusBar style="dark" />

      <ScrollView testID="menu-content" contentContainerClassName="gap-4 px-4 py-4 pb-10">
        {user ? (
          <Pressable
            testID="menu-profile-card"
            accessibilityRole="button"
            onPress={() => navigation.navigate("Main", { screen: "Perfil" })}
            className="flex-row items-center gap-4 rounded-2xl bg-brand p-4"
          >
            <View className="size-16 items-center justify-center rounded-full bg-white/20">
              <User size={32} color={colors.white} />
            </View>
            <View className="flex-1">
              <Text testID="menu-profile-name" className="text-lg font-bold text-white" numberOfLines={1}>{user.name}</Text>
              <Text className="text-xs text-white/70">{ROLE_LABELS[user.role]}</Text>
            </View>
            <CloseButton onPress={() => navigation.goBack()} />
          </Pressable>
        ) : (
          <Pressable
            testID="menu-signin-card"
            accessibilityRole="button"
            onPress={() => navigation.navigate("Login")}
            className="flex-row items-center gap-4 rounded-2xl bg-brand p-4"
          >
            <View className="size-16 items-center justify-center rounded-full bg-white/20">
              <LogIn size={28} color={colors.white} />
            </View>
            <View className="flex-1">
              <Text className="text-lg font-bold text-white">Entrar</Text>
              <Text className="text-xs text-white/70">Acesse conteúdos exclusivos da comunidade acadêmica.</Text>
            </View>
            <CloseButton onPress={() => navigation.goBack()} />
          </Pressable>
        )}

        {sections.map((section) => (
          <View key={section.key} testID={`menu-section-${section.key}`} className="gap-1.5">
            <Text className="px-1 text-xs font-bold text-brand">{section.title}</Text>
            <View className="overflow-hidden rounded-2xl bg-brand">
              {section.items.map((item, i) => (
                <MenuRow key={item.key} item={item} last={i === section.items.length - 1} />
              ))}
            </View>
          </View>
        ))}

        {user && (
          <View testID="menu-section-logout">
            <Pressable
              testID="menu-signout-button"
              accessibilityRole="button"
              onPress={() => void handleSignOut()}
              className="items-center rounded-2xl bg-brand py-3.5 active:bg-brand-dark"
            >
              <Text className="text-sm font-bold text-white">Sair</Text>
            </Pressable>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const ROLE_LABELS: Record<Role, string> = {
  STUDENT: "Estudante",
  TEACHER: "Professor(a)",
  ADMIN: "Administrador(a)",
};
