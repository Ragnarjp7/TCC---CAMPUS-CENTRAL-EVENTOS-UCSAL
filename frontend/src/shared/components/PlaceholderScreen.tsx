import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import type { ParamListBase, RouteProp } from "@react-navigation/native";

interface PlaceholderScreenProps {
  route: RouteProp<ParamListBase>;
}

export default function PlaceholderScreen({ route }: PlaceholderScreenProps) {
  return (
    <SafeAreaView testID="placeholder-screen" edges={["top"]} className="flex-1 bg-slate-50">
      <View testID="placeholder-content" className="flex-1 items-center justify-center px-8">
        <Text testID="placeholder-title" className="text-lg font-bold text-slate-900">{(route.params as { title?: string } | undefined)?.title ?? route.name}</Text>
        <Text testID="placeholder-message" className="mt-1 text-center text-sm text-slate-500">Esta tela ainda será construída.</Text>
      </View>
    </SafeAreaView>
  );
}
