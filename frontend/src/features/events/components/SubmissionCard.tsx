import { Pressable, Text, View } from "react-native";
import { ArrowRight, Calendar, Clock, MapPin, MessageSquareText, MonitorPlay, UserRound } from "lucide-react-native";
import { colors } from "@/shared/theme/colors";
import { formatDateTime, formatEventSchedule } from "../dates";
import type { EventSubmission } from "../types";
import { StatusBadge } from "./StatusBadge";

interface SubmissionCardProps {
  submission: EventSubmission;
  /** "owner": quem enviou acompanha; "review": a CAEX vê quem dirige o evento e quando chegou. */
  variant: "owner" | "review";
  actionLabel?: string;
  onPress: () => void;
}

const Line = ({ Icon, text, testID }: { Icon: typeof Calendar; text: string; testID?: string }) => (
  <View className="flex-row items-center gap-1.5">
    <Icon size={14} color={colors.brand} />
    <Text testID={testID} numberOfLines={1} className="flex-1 text-xs font-medium text-slate-600">
      {text}
    </Text>
  </View>
);

export const SubmissionCard = ({ submission: s, variant, actionLabel, onPress }: SubmissionCardProps) => {
  const id = `submission-card-${s.id}`;
  const schedule = formatEventSchedule(s.startsAt, s.endsAt);
  const direction = [s.responsible.name, s.leagueName].filter(Boolean).join(" · ");
  const showNote = variant === "owner" && s.caexNote && s.status !== "AGUARDANDO_ANALISE";

  return (
    <Pressable
      testID={id}
      accessibilityRole="button"
      accessibilityLabel={`${s.title}. ${actionLabel ?? "Ver detalhes"}`}
      onPress={onPress}
      className="gap-3 rounded-2xl bg-white p-4 shadow-sm active:opacity-90"
    >
      <View className="flex-row items-start gap-3">
        <Text testID={`${id}-title`} numberOfLines={2} className="flex-1 text-base font-bold text-slate-900">
          {s.title}
        </Text>
        <StatusBadge testID={`${id}-status`} status={s.status} />
      </View>

      <View className="gap-1">
        <Line Icon={Calendar} text={schedule.date} testID={`${id}-date`} />
        <Line Icon={Clock} text={schedule.time} testID={`${id}-time`} />
        <Line Icon={s.mode === "Online" ? MonitorPlay : MapPin} text={s.place} testID={`${id}-place`} />
        {variant === "review" && <Line Icon={UserRound} text={direction} testID={`${id}-direction`} />}
      </View>

      {showNote && (
        <View testID={`${id}-note`} className="flex-row gap-2 rounded-xl bg-slate-50 p-3">
          <MessageSquareText size={14} color={colors.slate500} />
          <Text numberOfLines={3} className="flex-1 text-xs text-slate-500">
            <Text className="font-semibold">CAEX: </Text>
            {s.caexNote}
          </Text>
        </View>
      )}

      <View className="flex-row items-center justify-between">
        <Text className="text-[11px] text-slate-400">
          {s.submittedAt ? `Enviado em ${formatDateTime(s.submittedAt)}` : ""}
        </Text>
        {actionLabel && (
          <View className="flex-row items-center gap-1">
            <Text className="text-xs font-semibold text-brand">{actionLabel}</Text>
            <ArrowRight size={14} color={colors.brand} />
          </View>
        )}
      </View>
    </Pressable>
  );
};
