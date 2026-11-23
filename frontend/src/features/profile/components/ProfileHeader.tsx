import { Pressable, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { MoreHorizontal } from "lucide-react-native";
import { ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";

export const PROFILE_TABS = ["Posts", "Membros", "Eventos"] as const;
export type ProfileTab = (typeof PROFILE_TABS)[number];

interface ProfileHeaderProps {
  name: string;
  handle: string;
  subtitle: string;
  tab: ProfileTab;
  onTabChange: (tab: ProfileTab) => void;
  onMenuPress: () => void;
}

export const ProfileHeader = ({ name, handle, subtitle, tab, onTabChange, onMenuPress }: ProfileHeaderProps) => {
  const insets = useSafeAreaInsets();

  return (
    <View testID="profile-header" className="bg-brand px-5" style={{ paddingTop: insets.top + 12 }}>
      <View className="flex-row justify-end">
        <Pressable
          testID="profile-header-menu-button"
          accessibilityRole="button"
          accessibilityLabel="Menu"
          onPress={onMenuPress}
          hitSlop={8}
          className="size-8 items-center justify-center rounded-full bg-white active:opacity-80"
        >
          <MoreHorizontal size={18} color={colors.brand} />
        </Pressable>
      </View>

      <View className="mt-2 flex-row items-center gap-4">
        <View className="flex-1">
          <Text testID="profile-header-name" numberOfLines={1} className="text-2xl font-extrabold text-white">
            {name}
          </Text>
          <Text testID="profile-header-handle" numberOfLines={1} className="text-xs text-white/60">
            @{handle}
          </Text>
          <Text testID="profile-header-subtitle" numberOfLines={2} className="mt-1.5 text-sm font-semibold text-white">
            {subtitle}
          </Text>
        </View>
        <ImgSlot testID="profile-header-avatar" className="size-16 rounded-full border-2 border-white/40" />
      </View>

      <View testID="profile-tabs" accessibilityRole="tablist" className="mt-5 flex-row">
        {PROFILE_TABS.map((t) => {
          const on = t === tab;
          return (
            <Pressable
              key={t}
              testID={`profile-tabs-${t.toLowerCase()}`}
              accessibilityRole="tab"
              accessibilityState={{ selected: on }}
              onPress={() => onTabChange(t)}
              className="flex-1 items-center pb-2.5 pt-1"
            >
              <Text className={`text-sm font-semibold ${on ? "text-white" : "text-white/60"}`}>{t}</Text>
              <View className={`mt-1.5 h-0.5 w-14 rounded-full ${on ? "bg-white" : "bg-transparent"}`} />
            </Pressable>
          );
        })}
      </View>
    </View>
  );
};
