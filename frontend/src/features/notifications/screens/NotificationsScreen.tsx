import { useState } from "react";
import { RefreshControl, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StateView } from "@/shared/components/StateView";
import { useAuth } from "@/features/auth/context/AuthContext";
import { NotificationCard } from "../components/NotificationCard";
import { useNotifications } from "../hooks/useNotifications";

export default function NotificationsScreen() {
  const { user, isRestoring } = useAuth();
  const isAuthenticated = user !== null;
  const feed = useNotifications(isAuthenticated && !isRestoring);
  /** Um cartão aberto por vez: abrir outro fecha o anterior. */
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const toggle = (id: number, lida: boolean) => {
    setExpandedId((current) => (current === id ? null : id));
    if (!lida) feed.markAsRead(id);
  };

  return (
    <SafeAreaView testID="notifications-screen" edges={["top"]} className="flex-1 bg-slate-50">
      <View className="px-5 pb-3 pt-4">
        <Text testID="notifications-title" className="text-2xl font-extrabold text-slate-900">Notificações</Text>
      </View>

      {!isAuthenticated && !isRestoring ? (
        <View testID="notifications-signed-out" className="flex-1 items-center px-8 pt-10">
          <Text className="text-center text-sm text-slate-400">Entre na sua conta para ver suas notificações.</Text>
        </View>
      ) : feed.viewState === "ready" ? (
        // ScrollView (e não FlatList) para que os cartões sejam irmãos diretos:
        // assim a transição de layout anima também os vizinhos empurrados.
        <ScrollView
          testID="notifications-list"
          contentContainerClassName="px-4 pt-1 pb-[110px]"
          refreshControl={<RefreshControl refreshing={feed.isRefetching} onRefresh={feed.retry} />}
        >
          {feed.notifications.map((item, index) => (
            <NotificationCard
              key={item.id}
              notification={item}
              index={index}
              expanded={expandedId === item.id}
              onToggle={() => toggle(item.id, item.lida)}
            />
          ))}
        </ScrollView>
      ) : (
        <StateView
          testID="notifications-state-view"
          state={feed.viewState}
          errorMessage={feed.errorMessage}
          emptyMessage="Você não tem notificações."
          onRetry={feed.retry}
        />
      )}
    </SafeAreaView>
  );
}
