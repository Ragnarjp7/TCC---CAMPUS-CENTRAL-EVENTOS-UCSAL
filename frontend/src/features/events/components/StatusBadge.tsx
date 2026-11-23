import { Text, View } from "react-native";
import { EVENT_STATUS } from "../status";
import type { EventStatus } from "../types";

interface StatusBadgeProps {
  status: EventStatus;
  testID?: string;
}

export const StatusBadge = ({ status, testID }: StatusBadgeProps) => {
  const style = EVENT_STATUS[status];
  return (
    <View testID={testID} className={`self-start rounded-full px-3 py-1 ${style.box}`}>
      <Text className={`text-[11px] font-semibold ${style.text}`}>{style.label}</Text>
    </View>
  );
};
