import { Pressable, Text, View } from "react-native";
import { MapPin } from "lucide-react-native";
import { ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { CampusEvent } from "../types";
import { DateBadge } from "./DateBadge";

interface EventCardProps {
  event: CampusEvent;
  onPress: () => void;
  imageClass?: string;
}

export const EventCard = ({ event, onPress, imageClass = "h-36" }: EventCardProps) => (
  <Pressable
    testID={`event-card-${event.id}`}
    onPress={onPress}
    className="overflow-hidden rounded-2xl bg-white shadow-sm active:opacity-90"
  >
    <ImgSlot testID={`event-card-${event.id}-image`} className={`w-full rounded-none ${imageClass}`} />
    <View className="flex-row items-center gap-3 p-3">
      <DateBadge testID={`event-card-${event.id}-date`} day={event.day} month={event.month} />
      <View className="flex-1">
        <Text testID={`event-card-${event.id}-title`} numberOfLines={1} className="text-sm font-bold text-slate-900">{event.title}</Text>
        <View className="flex-row items-center gap-1">
          <MapPin size={12} color={colors.slate500} />
          <Text testID={`event-card-${event.id}-place`} className="text-xs text-slate-500">{event.place}</Text>
        </View>
      </View>
    </View>
  </Pressable>
);
