import type { ReactNode } from "react";
import { Pressable, Text, TextInput, View } from "react-native";
import type { PressableProps, TextInputProps } from "react-native";
import { Bell, Image as ImageIcon } from "lucide-react-native";
import { colors } from "../theme/colors";

interface ImgSlotProps {
  className?: string;
  children?: ReactNode;
  testID?: string;
}

/** Espaço reservado para imagem. Troque o filho por <Image className="h-full w-full" source={...} /> */
export const ImgSlot = ({ className = "", children, testID }: ImgSlotProps) => (
  <View testID={testID} className={`items-center justify-center overflow-hidden rounded-xl bg-slate-200 ${className}`}>
    {children ?? <ImageIcon size={28} color={colors.slate400} strokeWidth={1.5} />}
  </View>
);

type LogoSize = "sm" | "md" | "xl";

interface LogoProps {
  light?: boolean;
  size?: LogoSize;
  testID?: string;
}

const LOGO_SIZES: Record<LogoSize, string> = {
  sm: "text-xl",
  md: "text-3xl",
  xl: "text-5xl",
};

export const Logo = ({ light = false, size = "md", testID = "logo" }: LogoProps) => (
  // TODO: substituir pelo logotipo oficial
  <Text testID={testID} className={`${LOGO_SIZES[size]} font-extrabold tracking-tight ${light ? "text-white" : "text-brand"}`}>
    CAMPUS
  </Text>
);

interface ButtonProps extends Omit<PressableProps, "children"> {
  title: string;
  variant?: "primary" | "outline";
  icon?: ReactNode;
  className?: string;
}

export const Button = ({ title, variant = "primary", icon, className = "", ...props }: ButtonProps) => {
  const primary = variant === "primary";
  return (
    <Pressable
      accessibilityRole="button"
      className={`h-12 w-full flex-row items-center justify-center gap-2 rounded-xl active:opacity-80 ${
        primary ? "bg-brand" : "border border-slate-200 bg-white"
      } ${className}`}
      {...props}
    >
      {icon}
      <Text className={`text-sm font-semibold ${primary ? "text-white" : "text-slate-700"}`}>{title}</Text>
    </Pressable>
  );
};

interface FieldProps extends TextInputProps {
  className?: string;
}

export const Field = ({ className = "", ...props }: FieldProps) => (
  <TextInput
    placeholderTextColor={colors.slate400}
    className={`h-12 w-full rounded-xl border border-slate-200 bg-white px-4 text-sm text-slate-800 focus:border-brand ${className}`}
    {...props}
  />
);

interface TabsProps<T extends string> {
  items: readonly T[];
  value: T;
  onChange: (value: T) => void;
  className?: string;
  testID?: string;
}

export const Tabs = <T extends string>({ items, value, onChange, className = "", testID = "tabs" }: TabsProps<T>) => (
  <View testID={testID} accessibilityRole="tablist" className={`flex-row border-b border-slate-200 ${className}`}>
    {items.map((t) => {
      const on = value === t;
      return (
        <Pressable
          key={t}
          testID={`${testID}-${t.toLowerCase()}`}
          accessibilityRole="tab"
          accessibilityState={{ selected: on }}
          onPress={() => onChange(t)}
          className={`flex-1 items-center border-b-2 py-2.5 ${on ? "border-brand" : "border-transparent"}`}
        >
          <Text className={`text-sm font-semibold ${on ? "text-brand" : "text-slate-400"}`}>{t}</Text>
        </Pressable>
      );
    })}
  </View>
);

interface DotsProps {
  total: number;
  active: number;
  testID?: string;
}

export const Dots = ({ total, active, testID = "dots" }: DotsProps) => (
  <View testID={testID} className="flex-row items-center justify-center gap-1.5">
    {Array.from({ length: total }, (_, i) => (
      <View key={i} testID={`${testID}-${i}`} className={`h-1.5 rounded-full ${i === active ? "w-5 bg-brand" : "w-1.5 bg-slate-300"}`} />
    ))}
  </View>
);

interface AppHeaderProps {
  children?: ReactNode;
  testID?: string;
}

export const AppHeader = ({ children, testID = "app-header" }: AppHeaderProps) => (
  <View testID={testID} className="flex-row items-center justify-between bg-white px-5 py-3.5">
    {children}
    <Pressable testID={`${testID}-notifications-button`} accessibilityLabel="Notificações" className="p-1.5">
      <Bell size={20} color={colors.brand} />
      <View testID={`${testID}-notifications-badge`} className="absolute right-1.5 top-1.5 size-2 rounded-full bg-red-500" />
    </Pressable>
  </View>
);

interface SectionTitleProps {
  title: string;
  action?: string;
  testID?: string;
}

export const SectionTitle = ({ title, action, testID = "section-title" }: SectionTitleProps) => (
  <View testID={testID} className="mb-3 flex-row items-center justify-between">
    <Text testID={`${testID}-text`} className="text-base font-bold text-slate-900">{title}</Text>
    {action && <Text testID={`${testID}-action`} className="text-xs font-semibold text-brand">{action}</Text>}
  </View>
);
