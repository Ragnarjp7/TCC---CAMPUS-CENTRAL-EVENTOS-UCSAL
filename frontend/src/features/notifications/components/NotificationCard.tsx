import { ActivityIndicator, Pressable, Text, View } from "react-native";
import Animated, { FadeIn, FadeOut, LinearTransition } from "react-native-reanimated";
import { useNavigation } from "@react-navigation/native";
import { Award, Calendar, ChevronDown, ChevronRight, MapPin, Users } from "lucide-react-native";
import type { LucideIcon } from "lucide-react-native";
import { useEvent } from "@/features/events/hooks/useEvent";
import { colors } from "@/shared/theme/colors";
import { getNotificationType } from "../types";
import type { AppNotification } from "../types";

/**
 * Paleta pastel em tons suaves e levemente acinzentados, alternando frio/quente
 * para que cartões vizinhos sempre contrastem. Ela se repete a cada 6 itens sem
 * cansar a vista, já que nenhuma cor é saturada.
 */
const PASTEL_PALETTE = [
  "#C9D6F5", // azul pervinca (puxa a cor da marca)
  "#F5D5C6", // pêssego
  "#CBE5D6", // menta
  "#F1CFDB", // rosa antigo
  "#DCD3F0", // lavanda
  "#F3E4B8", // amarelo manteiga
] as const;

/** Quanto cada cartão avança sobre o anterior. */
export const CARD_OVERLAP = 28;

/** Mesma transição para o cartão que cresce e para os vizinhos que ele empurra. */
const LAYOUT_TRANSITION = LinearTransition.duration(240);

interface NotificationCardProps {
  notification: AppNotification;
  index: number;
  expanded: boolean;
  onToggle: () => void;
}

const formatDate = (iso: string) => {
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
};

const InfoLine = ({ Icon, text, testID }: { Icon: LucideIcon; text: string; testID: string }) => (
  <View testID={testID} className="flex-row items-center gap-2">
    <Icon size={14} color={colors.slate500} />
    <Text className="flex-1 text-xs text-slate-600">{text}</Text>
  </View>
);

const ActionButton = ({ label, onPress, disabled, testID }: { label: string; onPress: () => void; disabled?: boolean; testID: string }) => (
  <Pressable
    testID={testID}
    accessibilityRole="button"
    disabled={disabled}
    onPress={onPress}
    className={`mt-3 flex-row items-center justify-center gap-1 self-start rounded-full bg-white/80 px-4 py-2 active:opacity-70 ${disabled ? "opacity-50" : ""}`}
  >
    <Text className="text-xs font-bold text-brand">{label}</Text>
    <ChevronRight size={14} color={colors.brand} />
  </Pressable>
);

/** Conteúdo que aparece só com o cartão aberto: texto de apoio + atalho conforme o tipo. */
const NotificationDetails = ({ notification }: { notification: AppNotification }) => {
  const navigation = useNavigation();
  const type = getNotificationType(notification);
  const { id, descricao, eventoId } = notification;
  const eventQuery = useEvent(eventoId);
  const event = eventQuery.data;

  return (
    <Animated.View entering={FadeIn.duration(200).delay(80)} exiting={FadeOut.duration(120)} style={{ marginTop: 12, gap: 6 }}>
      {descricao ? (
        <Text testID={`notification-card-${id}-description`} className="mb-1 text-[13px] leading-5 text-slate-700">
          {descricao}
        </Text>
      ) : null}

      {eventoId != null &&
        (eventQuery.isPending ? (
          <ActivityIndicator testID={`notification-card-${id}-loading`} size="small" color={colors.slate500} style={{ alignSelf: "flex-start" }} />
        ) : event ? (
          <>
            <InfoLine testID={`notification-card-${id}-event-title`} Icon={type === "CERTIFICADO" ? Award : Calendar} text={`${event.title} · ${event.day} ${event.month}, ${event.time}`} />
            <InfoLine testID={`notification-card-${id}-event-place`} Icon={MapPin} text={event.place} />
            <InfoLine testID={`notification-card-${id}-event-organizer`} Icon={Users} text={event.organizer} />
          </>
        ) : (
          <Text className="text-xs text-slate-500">Não foi possível carregar os detalhes do evento.</Text>
        ))}

      {!descricao && eventoId == null && <Text className="text-xs text-slate-500">Sem mais detalhes.</Text>}

      {type === "EVENTO" && eventoId != null && (
        <ActionButton
          testID={`notification-card-${id}-action`}
          label="Ver evento"
          onPress={() => navigation.navigate("EventDetail", { eventId: eventoId, event })}
        />
      )}
      {type === "CERTIFICADO" && (
        <ActionButton testID={`notification-card-${id}-action`} label="Ver certificados" onPress={() => navigation.navigate("Certificates")} />
      )}
    </Animated.View>
  );
};

export const NotificationCard = ({ notification, index, expanded, onToggle }: NotificationCardProps) => {
  const { id, mensagem, dataCriacao, lida } = notification;

  return (
    <Animated.View
      layout={LAYOUT_TRANSITION}
      style={{
        backgroundColor: PASTEL_PALETTE[index % PASTEL_PALETTE.length],
        borderRadius: 28,
        marginTop: index === 0 ? 0 : -CARD_OVERLAP,
        zIndex: index,
        // sombra projetada para cima, separando o cartão do que está por baixo
        shadowColor: "#000",
        shadowOffset: { width: 0, height: -3 },
        shadowOpacity: 0.12,
        shadowRadius: 6,
        elevation: index + 1,
      }}
    >
      <Pressable
        testID={`notification-card-${id}`}
        accessibilityRole="button"
        accessibilityState={{ selected: !lida, expanded }}
        onPress={onToggle}
        className="px-5 pt-4 active:opacity-95"
        style={{ paddingBottom: CARD_OVERLAP + 16 }}
      >
        <View className="flex-row items-start justify-between gap-3">
          <View className="flex-1 flex-row items-start gap-2">
            {!lida && (
              <View
                testID={`notification-card-${id}-unread`}
                className="mt-1.5 size-2 rounded-full bg-brand"
                // brilho branco curto em volta do ponto para destacá-lo sem espalhar
                style={{ boxShadow: "0 0 3px 1.5px rgba(255, 255, 255, 0.9)" }}
              />
            )}
            <Text
              testID={`notification-card-${id}-message`}
              numberOfLines={expanded ? undefined : 3}
              className={`flex-1 text-sm text-slate-800 ${lida ? "font-medium" : "font-bold"}`}
            >
              {mensagem}
            </Text>
          </View>
          <View className="flex-row items-center gap-1.5">
            <View className="rounded-full bg-white/60 px-2.5 py-1">
              <Text testID={`notification-card-${id}-date`} className="text-[10px] font-semibold text-slate-600">
                {formatDate(dataCriacao)}
              </Text>
            </View>
            <View style={{ transform: [{ rotate: expanded ? "180deg" : "0deg" }] }}>
              <ChevronDown size={16} color={colors.slate500} />
            </View>
          </View>
        </View>

        {expanded && <NotificationDetails notification={notification} />}
      </Pressable>
    </Animated.View>
  );
};
