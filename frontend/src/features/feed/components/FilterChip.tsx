import { Pressable, Text } from "react-native";

interface FilterChipProps {
  label: string;
  selected: boolean;
  onPress: () => void;
  onDark?: boolean;
}

const chipStyles = {
  dark: {
    selected: "border-white bg-white",
    idle: "border-white/40 bg-transparent",
    selectedText: "text-brand",
    idleText: "text-white",
  },
  light: {
    selected: "border-brand bg-brand",
    idle: "border-brand/40 bg-transparent",
    selectedText: "text-white",
    idleText: "text-brand",
  },
} as const;

export const FilterChip = ({ label, selected, onPress, onDark = true }: FilterChipProps) => {
  const styles = onDark ? chipStyles.dark : chipStyles.light;
  return (
    <Pressable
      testID={`filter-chip-${label.toLowerCase()}`}
      accessibilityRole="button"
      accessibilityState={{ selected }}
      onPress={onPress}
      className={`rounded-full border px-4 py-1.5 active:opacity-80 ${selected ? styles.selected : styles.idle}`}
    >
      <Text className={`text-xs font-semibold ${selected ? styles.selectedText : styles.idleText}`}>{label}</Text>
    </Pressable>
  );
};
