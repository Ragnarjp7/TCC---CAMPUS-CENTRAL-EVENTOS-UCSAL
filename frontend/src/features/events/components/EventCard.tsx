import { Pressable, Text, View } from "react-native";
import { GraduationCap, MapPin } from "lucide-react-native";
import { ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { CampusEvent } from "../types";
import { DateBadge } from "./DateBadge";
import { QuickInfoButton, QuickInfoPanel, useQuickInfoDisclosure } from "./QuickInfo";

interface EventCardProps {
  event: CampusEvent;
  onPress: () => void;
  imageClass?: string;
}

export const EventCard = ({ event, onPress, imageClass = "h-36" }: EventCardProps) => {
  // a lista já traz os dados do evento, então os detalhes rápidos abrem sem nova requisição
  const disclosure = useQuickInfoDisclosure();

  return (
    <Pressable
      testID={`event-card-${event.id}`}
      accessibilityRole="button"
      accessibilityLabel={`${event.title}, ${event.place}. Ver detalhes`}
      onPress={onPress}
      className="overflow-hidden rounded-2xl bg-white shadow-sm active:opacity-90"
    >
      <View>
        <ImgSlot testID={`event-card-${event.id}-image`} className={`w-full rounded-none ${imageClass}`} />
        {event.complementaryHours != null && event.complementaryHours > 0 && (
          <View
            testID={`event-card-${event.id}-hours`}
            className="absolute right-2 top-2 flex-row items-center gap-1 rounded-full bg-white/90 px-2.5 py-1"
          >
            <GraduationCap size={12} color={colors.brand} />
            <Text className="text-[11px] font-semibold text-brand">{event.complementaryHours}h complementares</Text>
          </View>
        )}
      </View>
      <View className="flex-row items-center gap-3 p-3">
        <DateBadge testID={`event-card-${event.id}-date`} day={event.day} month={event.month} />
        <View className="flex-1">
          <Text testID={`event-card-${event.id}-title`} numberOfLines={1} className="text-sm font-bold text-slate-900">{event.title}</Text>
          <View className="flex-row items-center gap-1">
            <MapPin size={12} color={colors.slate500} />
            <Text testID={`event-card-${event.id}-place`} numberOfLines={1} className="flex-1 text-xs text-slate-500">{event.place}</Text>
          </View>
        </View>
        <QuickInfoButton testID={`event-card-${event.id}-details-button`} disclosure={disclosure} alignClass="self-center" />
      </View>
      <QuickInfoPanel
        testID={`event-card-${event.id}-quick-info`}
        disclosure={disclosure}
        event={event}
        spacing={{ paddingHorizontal: 12, paddingBottom: 12 }}
      />
    </Pressable>
  );
};
