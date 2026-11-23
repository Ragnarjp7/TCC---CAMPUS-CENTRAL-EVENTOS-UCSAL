import { useEffect, useState } from "react";
import { ActivityIndicator, Pressable, Text, View, type LayoutChangeEvent, type ViewStyle } from "react-native";
import Animated, { Easing, interpolate, useAnimatedStyle, useSharedValue, withTiming } from "react-native-reanimated";
import { ChevronDown } from "lucide-react-native";
import { colors } from "@/shared/theme/colors";
import { monthNumber } from "../dates";
import type { CampusEvent } from "../types";

/** Mesma curva para a área abrir/fechar, o conteúdo aparecer e a seta girar. */
const TIMING = { duration: 280, easing: Easing.bezier(0.4, 0, 0.2, 1) };

/** "29" + "NOV" -> "29/11" */
const formatShortDate = (event: CampusEvent) => `${event.day.padStart(2, "0")}/${monthNumber(event.month)}`;

const quickInfo = (event: CampusEvent): { label: string; value: string }[] => [
  { label: "Data e horário", value: `${formatShortDate(event)} · ${event.time}` },
  { label: "Carga horária", value: event.complementaryHours ? `${event.complementaryHours}h complementares` : "Não concede" },
  { label: "Certificado", value: event.certificate ? "Sim" : "Não" },
  { label: "Inscrição", value: event.price ? `R$ ${event.price.toFixed(2).replace(".", ",")}` : "Gratuita" },
];

/** Estado e animações do "Ver mais / Ver menos" compartilhados entre o painel e o botão. */
export function useQuickInfoDisclosure() {
  const [expanded, setExpanded] = useState(false);
  // o conteúdo só é montado na primeira abertura e continua montado para a animação de fechar
  const [mounted, setMounted] = useState(false);

  const progress = useSharedValue(0);
  const contentHeight = useSharedValue(0);

  useEffect(() => {
    progress.value = withTiming(expanded ? 1 : 0, TIMING);
  }, [expanded, progress]);

  const toggle = () => {
    setMounted(true);
    setExpanded((v) => !v);
  };

  // mede a altura natural do conteúdo; se ela mudar (carregando -> dados), anima até a nova altura
  const onContentLayout = (e: LayoutChangeEvent) => {
    const h = e.nativeEvent.layout.height;
    contentHeight.value = contentHeight.value === 0 ? h : withTiming(h, TIMING);
  };

  // só a altura da área colapsável muda: o que vem abaixo acompanha pelo layout normal,
  // sem transformar/esticar o resto do card
  const collapsibleStyle = useAnimatedStyle(() => ({ height: contentHeight.value * progress.value }));
  const contentStyle = useAnimatedStyle(() => ({ opacity: interpolate(progress.value, [0.3, 1], [0, 1], "clamp") }));
  const chevronStyle = useAnimatedStyle(() => ({ transform: [{ rotate: `${progress.value * 180}deg` }] }));

  return { expanded, mounted, toggle, onContentLayout, collapsibleStyle, contentStyle, chevronStyle };
}

export type QuickInfoDisclosure = ReturnType<typeof useQuickInfoDisclosure>;

interface QuickInfoPanelProps {
  testID: string;
  disclosure: QuickInfoDisclosure;
  /** Ausente enquanto carrega. */
  event?: CampusEvent;
  isError?: boolean;
  /** Espaçamento em volta do quadro; fica dentro da área colapsável para sumir junto quando fechada. */
  spacing?: ViewStyle;
}

/** Área colapsável com data/horário, carga horária, certificado e valor da inscrição. */
export const QuickInfoPanel = ({ testID, disclosure, event, isError, spacing = { paddingBottom: 12 } }: QuickInfoPanelProps) => {
  const { expanded, mounted, onContentLayout, collapsibleStyle, contentStyle } = disclosure;
  return (
    <Animated.View style={[{ overflow: "hidden" }, collapsibleStyle]} pointerEvents={expanded ? "auto" : "none"}>
      {mounted && (
        // absoluto para ser medido com a altura natural, independente da altura animada do pai
        <Animated.View
          testID={testID}
          onLayout={onContentLayout}
          accessibilityElementsHidden={!expanded}
          importantForAccessibility={expanded ? "auto" : "no-hide-descendants"}
          style={[{ position: "absolute", top: 0, left: 0, right: 0 }, spacing, contentStyle]}
        >
          <View className="gap-1.5 rounded-lg bg-slate-50 px-3 pb-3 pt-1.5">
            {event ? (
              quickInfo(event).map(({ label, value }, index) => (
                <View key={label} className={`flex-row justify-between gap-3 pt-1.5 ${index > 0 ? "border-t border-slate-300" : ""}`}>
                  <Text className="text-[11px] text-slate-400">{label}</Text>
                  <Text numberOfLines={1} className="flex-1 text-right text-[11px] font-semibold text-slate-700">{value}</Text>
                </View>
              ))
            ) : isError ? (
              <Text className="text-center text-[11px] text-slate-500">Não foi possível carregar os detalhes.</Text>
            ) : (
              <ActivityIndicator testID={`${testID}-loading`} color={colors.brand} />
            )}
          </View>
        </Animated.View>
      )}
    </Animated.View>
  );
};

interface QuickInfoButtonProps {
  testID: string;
  disclosure: QuickInfoDisclosure;
  /** Alinhamento no pai; por padrão fica no canto direito de uma coluna. */
  alignClass?: string;
}

/** Botão "Ver mais ⌄" / "Ver menos ⌃" que abre e fecha o {@link QuickInfoPanel}. */
export const QuickInfoButton = ({ testID, disclosure, alignClass = "self-end" }: QuickInfoButtonProps) => {
  const { expanded, toggle, chevronStyle } = disclosure;
  return (
    <Pressable
      testID={testID}
      accessibilityRole="button"
      accessibilityState={{ expanded }}
      accessibilityLabel={expanded ? "Ocultar detalhes rápidos" : "Mostrar detalhes rápidos"}
      onPress={toggle}
      hitSlop={6}
      className={`h-9 flex-row items-center justify-center gap-1 rounded-lg bg-brand pl-4 pr-3 active:opacity-80 ${alignClass}`}
    >
      <Text className="text-xs font-semibold text-white">{expanded ? "Ver menos" : "Ver mais"}</Text>
      <Animated.View style={chevronStyle}>
        <ChevronDown testID={`${testID}-chevron`} size={16} color={colors.white} />
      </Animated.View>
    </Pressable>
  );
};
