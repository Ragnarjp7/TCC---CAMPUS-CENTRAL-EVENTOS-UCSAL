import { useState } from "react";
import { ActivityIndicator, FlatList, Pressable, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import { useAuth } from "@/features/auth/context/AuthContext";
import { EventCard } from "@/features/events/components/EventCard";
import { useEventCategories } from "@/features/events/hooks/useEventCategories";
import { useEventsFeed } from "@/features/events/hooks/useEventsFeed";
import type { EventMode } from "@/features/events/types";
import { StateView } from "@/shared/components/StateView";
import { useDebouncedValue } from "@/shared/hooks/useDebouncedValue";
import { colors } from "@/shared/theme/colors";
import type { TabScreenProps } from "@/navigation/types";
import { FeedHeader } from "../components/FeedHeader";
import { FilterChip } from "../components/FilterChip";

const MODES: readonly EventMode[] = ["Presenciais", "Online"];

/**
 * Feed inicial: lista dos eventos disponíveis, com busca e filtros de modalidade e categoria (RF31).
 * Aberta a visitantes; a inscrição pede login na tela de detalhe (RF33).
 */
export default function HomeScreen({ navigation }: TabScreenProps<"Início">) {
  const { user } = useAuth();
  const isAuthenticated = user !== null;
  const [headerHeight, setHeaderHeight] = useState(0);

  const [query, setQuery] = useState("");
  const [mode, setMode] = useState<EventMode | null>(null);
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const search = useDebouncedValue(query.trim());
  const categories = useEventCategories();

  const feed = useEventsFeed({ mode, categoryId, search });
  const hasFilters = search !== "" || categoryId !== null || mode !== null;

  const clearFilters = () => {
    setQuery("");
    setMode(null);
    setCategoryId(null);
  };

  const feedBg = isAuthenticated ? "bg-brand" : "bg-slate-100";

  const filters = (
    <ScrollView
      testID="home-filters"
      horizontal
      showsHorizontalScrollIndicator={false}
      keyboardShouldPersistTaps="handled"
      contentContainerClassName="items-center gap-2 px-5 pb-3"
      className="-mx-5 grow-0"
    >
      {MODES.map((m) => (
        <FilterChip
          key={m}
          label={m}
          selected={mode === m}
          onPress={() => setMode((current) => (current === m ? null : m))}
          onDark={isAuthenticated}
        />
      ))}
      {categories.length > 0 && (
        <View className={`mx-1 h-5 w-px ${isAuthenticated ? "bg-white/40" : "bg-brand/30"}`} />
      )}
      {categories.map((c) => (
        <FilterChip
          key={c.id}
          label={c.name}
          selected={categoryId === c.id}
          onPress={() => setCategoryId((current) => (current === c.id ? null : c.id))}
          onDark={isAuthenticated}
        />
      ))}
    </ScrollView>
  );

  return (
    <SafeAreaView testID="home-screen" edges={["top"]} className={`flex-1 ${feedBg}`}>
      <StatusBar style={isAuthenticated ? "light" : "dark"} />
      <View className="flex-1">
        {feed.viewState === "ready" ? (
          <FlatList
            testID="home-event-list"
            data={feed.events}
            keyExtractor={(e) => String(e.id)}
            contentContainerClassName="gap-4 px-5 pb-[110px]"
            contentContainerStyle={{ paddingTop: headerHeight }}
            keyboardShouldPersistTaps="handled"
            ListHeaderComponent={filters}
            renderItem={({ item }) => (
              <EventCard event={item} onPress={() => navigation.navigate("EventDetail", { eventId: item.id, event: item })} />
            )}
            refreshing={feed.isRefetching}
            onRefresh={feed.retry}
            progressViewOffset={headerHeight}
            onEndReached={feed.loadMore}
            onEndReachedThreshold={0.4}
            ListFooterComponent={
              feed.isFetchingNextPage ? (
                <ActivityIndicator testID="home-load-more-indicator" className="py-4" color={isAuthenticated ? colors.white : colors.brand} />
              ) : undefined
            }
          />
        ) : (
          <View testID="home-state-container" className="flex-1 px-5 pb-[110px]" style={{ paddingTop: headerHeight }}>
            {filters}
            <StateView
              testID="home-state-view"
              state={feed.viewState}
              errorMessage={feed.errorMessage}
              emptyMessage={hasFilters ? "Nenhum evento encontrado com esses filtros." : "Nenhum evento disponível no momento."}
              onRetry={feed.retry}
              inverted={isAuthenticated}
            />
            {feed.viewState === "empty" && hasFilters && (
              <Pressable testID="home-clear-filters" onPress={clearFilters} className="items-center pb-6">
                <Text className={`text-sm font-semibold ${isAuthenticated ? "text-white" : "text-brand"}`}>Limpar filtros</Text>
              </Pressable>
            )}
          </View>
        )}

        <View
          testID="home-header-container"
          className="absolute inset-x-0 top-0"
          pointerEvents="box-none"
          onLayout={(e) => setHeaderHeight(e.nativeEvent.layout.height)}
        >
          <FeedHeader query={query} onQueryChange={setQuery} onMenuPress={() => navigation.navigate("Menu")} />
        </View>
      </View>
    </SafeAreaView>
  );
}
