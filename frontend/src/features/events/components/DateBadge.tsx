import { Text, View } from "react-native";

interface DateBadgeProps {
  day: string;
  month: string;
  className?: string;
  testID?: string;
}

export const DateBadge = ({ day, month, className = "size-11", testID = "date-badge" }: DateBadgeProps) => (
  <View testID={testID} className={`items-center justify-center rounded-lg bg-brand ${className}`}>
    <Text testID={`${testID}-day`} className="text-base font-bold leading-none text-white">{day}</Text>
    <Text testID={`${testID}-month`} className="mt-0.5 text-[9px] font-semibold tracking-wider text-white">{month}</Text>
  </View>
);
