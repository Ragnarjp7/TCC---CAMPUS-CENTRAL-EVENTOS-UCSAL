import { useState } from "react";
import { ActivityIndicator, FlatList, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StateView } from "@/shared/components/StateView";
import { AppHeader, Tabs } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";
import { EventCard } from "../components/EventCard";
import { useEventsFeed } from "../hooks/useEventsFeed";
import type { EventMode } from "../types";

type EventFilter = "Todos" | EventMode;

const FILTERS: readonly EventFilter[] = ["Todos", "Presenciais", "Online"];

export default function EventsScreen({ navigation }: RootScreenProps<"Events">) {
  const [tab, setTab] = useState<EventFilter>("Todos");
  const feed = useEventsFeed(tab === "Todos" ? null : tab);

  return (
    <SafeAreaView testID="events-screen" edges={["top"]} className="flex-1 bg-white">
      <AppHeader testID="events-header">
        <Text testID="events-title" className="text-lg font-extrabold text-slate-900">Eventos</Text>
      </AppHeader>
      <Tabs testID="events-filter-tabs" items={FILTERS} value={tab} onChange={setTab} className="px-5" />
      <View testID="events-content" className="flex-1 bg-slate-50">
        {feed.viewState === "ready" ? (
          <FlatList
            testID="events-list"
            data={feed.events}
            keyExtractor={(e) => String(e.id)}
            contentContainerClassName="gap-3 px-5 py-4"
            renderItem={({ item }) => (
              <EventCard event={item} imageClass="h-28" onPress={() => navigation.navigate("EventDetail", { event: item })} />
            )}
            refreshing={feed.isRefetching}
            onRefresh={feed.retry}
            onEndReached={feed.loadMore}
            onEndReachedThreshold={0.4}
            ListFooterComponent={
              feed.isFetchingNextPage ? (
                <ActivityIndicator testID="events-load-more-indicator" className="py-4" color={colors.brand} />
              ) : undefined
            }
          />
        ) : (
          <StateView
            testID="events-state-view"
            state={feed.viewState}
            errorMessage={feed.errorMessage}
            emptyMessage="Nenhum evento encontrado."
            onRetry={feed.retry}
          />
        )}
      </View>
    </SafeAreaView>
  );
}
