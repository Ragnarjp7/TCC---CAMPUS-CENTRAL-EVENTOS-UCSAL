import { Pressable, TextInput, View } from "react-native";
import { Menu, Search } from "lucide-react-native";
import { Logo } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";

interface FeedHeaderProps {
  query: string;
  onQueryChange: (value: string) => void;
  onMenuPress: () => void;
}

export const FeedHeader = ({ query, onQueryChange, onMenuPress }: FeedHeaderProps) => (
  <View
    testID="feed-header"
    className="mx-[15px] mb-3 mt-[15px] gap-3 rounded-[25px] bg-brand px-5 py-4"
    style={{
      shadowColor: "#000",
      shadowOffset: { width: 0, height: 4 },
      shadowOpacity: 0.3,
      shadowRadius: 6,
      elevation: 8,
    }}
  >
    <View className="flex-row items-center justify-between">
      <Logo testID="feed-header-logo" light size="sm" />
      <Pressable testID="feed-header-menu-button" accessibilityLabel="Menu" onPress={onMenuPress} className="p-1">
        <Menu size={24} color={colors.white} />
      </Pressable>
    </View>
    <View testID="feed-header-search" className="h-10 flex-row items-center gap-2 rounded-full bg-white px-4">
      <Search size={16} color={colors.slate500} />
      <TextInput
        testID="feed-header-search-input"
        value={query}
        onChangeText={onQueryChange}
        placeholder="Pesquisar"
        placeholderTextColor={colors.slate400}
        className="flex-1 text-sm text-slate-800"
      />
    </View>
  </View>
);
