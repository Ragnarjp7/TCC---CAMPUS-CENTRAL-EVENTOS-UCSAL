import { Pressable, Text, View } from "react-native";
import { BlurView } from "expo-blur";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import type { BottomTabBarProps } from "@react-navigation/bottom-tabs";
import type { LucideIcon } from "lucide-react-native";
import { Bell, Home, User } from "lucide-react-native";

const ICONS: Record<string, LucideIcon> = {
  Início: Home,
  Perfil: User,
  Notificações: Bell,
};

export const FLOATING_TAB_BAR_HEIGHT = 68;
export const FLOATING_TAB_BAR_MARGIN = 15;

export function FloatingTabBar({ state, descriptors, navigation }: BottomTabBarProps) {
  const insets = useSafeAreaInsets();

  return (
    <View
      testID="tab-bar"
      pointerEvents="box-none"
      className="absolute inset-x-0 bottom-0"
      style={{ paddingHorizontal: FLOATING_TAB_BAR_MARGIN, paddingBottom: FLOATING_TAB_BAR_MARGIN + insets.bottom }}
    >
      <View className="overflow-hidden rounded-[25px]" style={{ height: FLOATING_TAB_BAR_HEIGHT, borderRadius: 25 }}>
        <BlurView
          intensity={40}
          tint="dark"
          experimentalBlurMethod="dimezisBlurView"
          style={{ flex: 1, backgroundColor: "rgba(0,0,0,0.55)" }}
        >
        <View className="flex-1 flex-row items-center justify-around px-3">
          {state.routes.map((route, index) => {
            const focused = state.index === index;
            const { options } = descriptors[route.key];
            const Icon = ICONS[route.name] ?? Home;
            const label = options.tabBarLabel ?? options.title ?? route.name;

            const onPress = () => {
              const event = navigation.emit({ type: "tabPress", target: route.key, canPreventDefault: true });
              if (!focused && !event.defaultPrevented) navigation.navigate(route.name, route.params);
            };

            return (
              <Pressable
                key={route.key}
                testID={`tab-bar-${route.name.toLowerCase()}`}
                accessibilityRole="tab"
                accessibilityState={{ selected: focused }}
                accessibilityLabel={options.tabBarAccessibilityLabel ?? route.name}
                onPress={onPress}
                onLongPress={() => navigation.emit({ type: "tabLongPress", target: route.key })}
                className={`h-[52px] flex-1 items-center justify-center gap-0.5 rounded-[18px] border ${
                  focused ? "mx-1 border-white bg-white/20" : "mx-1 border-transparent"
                }`}
              >
                <Icon size={21} color={focused ? "#FFFFFF" : "rgba(255,255,255,0.6)"} />
                <Text
                  className={`text-[10px] font-medium ${focused ? "text-white" : "text-white/60"}`}
                  numberOfLines={1}
                >
                  {typeof label === "string" ? label : route.name}
                </Text>
              </Pressable>
            );
          })}
        </View>
        </BlurView>
      </View>
    </View>
  );
}
