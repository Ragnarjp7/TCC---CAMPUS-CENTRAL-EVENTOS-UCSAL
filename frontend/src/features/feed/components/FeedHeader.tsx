import { Pressable, Text, TextInput, View } from "react-native";
import { Menu, Search } from "lucide-react-native";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getRoleLabel } from "@/features/auth/roles";
import { Logo } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";

interface FeedHeaderProps {
  query: string;
  onQueryChange: (value: string) => void;
  onMenuPress: () => void;
}

export const FeedHeader = ({ query, onQueryChange, onMenuPress }: FeedHeaderProps) => {
  const { user } = useAuth();

  return (
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
        <View className="mr-3 flex-1">
          <Logo testID="feed-header-logo" light size="sm" />
          {user && (
            <Text testID="feed-header-role" className="text-[11px] font-medium text-white/75" numberOfLines={1}>
              {getRoleLabel(user)}
            </Text>
          )}
        </View>
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
          placeholder="Buscar eventos por nome, local ou organizador"
          placeholderTextColor={colors.slate400}
          className="flex-1 text-sm text-slate-800"
        />
      </View>
    </View>
  );
};
