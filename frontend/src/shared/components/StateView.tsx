import { ActivityIndicator, Pressable, Text, View } from "react-native";
import type { ViewState } from "../state/viewState";
import { colors } from "../theme/colors";

interface StateViewProps {
  state: Exclude<ViewState, "ready">;
  errorMessage?: string;
  emptyMessage: string;
  onRetry: () => void;
  inverted?: boolean;
  testID?: string;
}

export const StateView = ({
  state,
  errorMessage,
  emptyMessage,
  onRetry,
  inverted = false,
  testID = "state-view",
}: StateViewProps) => {
  if (state === "loading") {
    return (
      <View testID={`${testID}-loading`} className="flex-1 items-center justify-center">
        <ActivityIndicator testID={`${testID}-loading-indicator`} color={inverted ? colors.white : colors.brand} />
      </View>
    );
  }

  if (state === "error") {
    return (
      <View testID={`${testID}-error`} className="flex-1 items-center justify-center gap-3 px-8">
        <Text testID={`${testID}-error-message`} className={`text-center text-sm ${inverted ? "text-white" : "text-slate-600"}`}>
          {errorMessage}
        </Text>
        <Pressable
          testID={`${testID}-retry-button`}
          accessibilityRole="button"
          onPress={onRetry}
          className="rounded-lg border border-slate-200 bg-white px-4 py-2 active:opacity-80"
        >
          <Text className="text-xs font-semibold text-brand">Tentar novamente</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <View testID={`${testID}-empty`} className="flex-1 items-center px-8 pt-10">
      <Text testID={`${testID}-empty-message`} className={`text-center text-sm ${inverted ? "text-white/80" : "text-slate-400"}`}>{emptyMessage}</Text>
    </View>
  );
};
