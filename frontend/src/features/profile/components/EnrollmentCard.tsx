import { Pressable, Text, View } from "react-native";
import { Clock, MapPin } from "lucide-react-native";
import { ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import { monthNumber } from "@/features/events/dates";
import type { Enrollment, EnrollmentStatus } from "../types";

const STATUS_STYLE: Record<EnrollmentStatus, { label: string; box: string; text: string }> = {
  INSCRITO: { label: "Inscrito", box: "bg-brand-soft", text: "text-brand" },
  PARTICIPOU: { label: "Participou", box: "bg-emerald-100", text: "text-emerald-700" },
  AUSENTE: { label: "Ausente", box: "bg-slate-200", text: "text-slate-500" },
};


interface EnrollmentCardProps {
  enrollment: Enrollment;
  onPressDetails: () => void;
  /** Só é usado quando o usuário participou de um evento que emite certificado. */
  onPressCertificate: () => void;
}

export const EnrollmentCard = ({ enrollment, onPressDetails, onPressCertificate }: EnrollmentCardProps) => {
  const { event, status } = enrollment;
  const style = STATUS_STYLE[status];
  const hasCertificate = status === "PARTICIPOU" && !!event.certificate;
  const id = `enrollment-card-${enrollment.id}`;

  return (
    <Pressable
      testID={id}
      accessibilityRole="button"
      accessibilityLabel={`${event.title}, ${style.label}. Ver detalhes`}
      onPress={onPressDetails}
      className="overflow-hidden rounded-2xl bg-white shadow-sm active:opacity-90"
    >
      <ImgSlot testID={`${id}-cover`} className="h-28 w-full rounded-none bg-slate-800" />
      <View className="gap-2 p-4">
        <Text testID={`${id}-title`} numberOfLines={2} className="text-base font-bold text-slate-900">
          {event.title}
        </Text>
        <View className="gap-1">
          <View className="flex-row items-center gap-1.5">
            <Clock size={14} color={colors.brand} />
            <Text testID={`${id}-time`} className="text-xs font-medium text-slate-600">
              {event.day.padStart(2, "0")}/{monthNumber(event.month)} · {event.time}
            </Text>
          </View>
          <View className="flex-row items-center gap-1.5">
            <MapPin size={14} color={colors.brand} />
            <Text testID={`${id}-place`} numberOfLines={1} className="flex-1 text-xs font-medium text-slate-600">
              {event.place}
            </Text>
          </View>
        </View>
        <View className="mt-1 flex-row items-center justify-between">
          <View testID={`${id}-status`} className={`rounded-full px-3 py-1 ${style.box}`}>
            <Text className={`text-[11px] font-semibold ${style.text}`}>{style.label}</Text>
          </View>
          <Pressable
            testID={`${id}-action`}
            accessibilityRole="button"
            onPress={hasCertificate ? onPressCertificate : onPressDetails}
            hitSlop={6}
            className="rounded-full bg-brand px-4 py-1.5 active:opacity-80"
          >
            <Text className="text-xs font-semibold text-white">{hasCertificate ? "Certificado" : "Detalhes"}</Text>
          </Pressable>
        </View>
      </View>
    </Pressable>
  );
};
