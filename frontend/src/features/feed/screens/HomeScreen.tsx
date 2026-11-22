import { useState } from "react";
import { ActivityIndicator, FlatList, ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import { StateView } from "@/shared/components/StateView";
import { colors } from "@/shared/theme/colors";
import type { TabScreenProps } from "@/navigation/types";
import { FeedHeader } from "../components/FeedHeader";
import { FilterChip } from "../components/FilterChip";
import { PostCard } from "../components/PostCard";
import { useHomeScreen } from "../hooks/useHomeScreen";
import { POST_CATEGORIES } from "../types";

export default function HomeScreen({ navigation }: TabScreenProps<"Início">) {
  const home = useHomeScreen();
  const [headerHeight, setHeaderHeight] = useState(0);
  const feedBg = home.isAuthenticated ? "bg-brand" : "bg-slate-100";

  const categoryFilters = (
    <ScrollView
      testID="home-category-filters"
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerClassName="gap-2 px-5 pb-3"
      className="-mx-5 grow-0"
    >
      {POST_CATEGORIES.map((c) => (
        <FilterChip
          key={c}
          label={c}
          selected={home.category === c}
          onPress={() => home.toggleCategory(c)}
          onDark={home.isAuthenticated}
        />
      ))}
    </ScrollView>
  );

  return (
    <SafeAreaView testID="home-screen" edges={["top"]} className={`flex-1 ${feedBg}`}>
      <StatusBar style={home.isAuthenticated ? "light" : "dark"} />
      <View className="flex-1">
        {home.viewState === "ready" ? (
          <FlatList
            testID="home-post-list"
            data={home.posts}
            keyExtractor={(p) => String(p.id)}
            contentContainerClassName="gap-4 px-5 pb-[110px]"
            contentContainerStyle={{ paddingTop: headerHeight }}
            ListHeaderComponent={categoryFilters}
            renderItem={({ item }) => <PostCard post={item} onPress={() => undefined} />}
            refreshing={home.isRefetching}
            onRefresh={home.retry}
            progressViewOffset={headerHeight}
            onEndReached={home.loadMore}
            onEndReachedThreshold={0.4}
            ListFooterComponent={
              home.isFetchingNextPage ? (
                <ActivityIndicator testID="home-load-more-indicator" className="py-4" color={home.isAuthenticated ? colors.white : colors.brand} />
              ) : undefined
            }
          />
        ) : (
          <View testID="home-state-container" className="flex-1 px-5" style={{ paddingTop: headerHeight }}>
            {categoryFilters}
            <StateView
              testID="home-state-view"
              state={home.viewState}
              errorMessage={home.errorMessage}
              emptyMessage={home.category ? `Nenhuma publicação em ${home.category}.` : "Nenhuma publicação encontrada."}
              onRetry={home.retry}
              inverted={home.isAuthenticated}
            />
          </View>
        )}

        <View
          testID="home-header-container"
          className="absolute inset-x-0 top-0"
          pointerEvents="box-none"
          onLayout={(e) => setHeaderHeight(e.nativeEvent.layout.height)}
        >
          <FeedHeader query={home.query} onQueryChange={home.setQuery} onMenuPress={() => navigation.navigate("Menu")} />
        </View>
      </View>
    </SafeAreaView>
  );
}
