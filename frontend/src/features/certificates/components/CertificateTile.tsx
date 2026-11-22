import { Pressable, Text, View } from "react-native";
import { Award, CalendarDays, Clock } from "lucide-react-native";
import { colors } from "@/shared/theme/colors";
import type { Certificate } from "../types";

const MONTHS = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];

/** "2026-11-14" → "14 nov 2026" (sem depender do Intl do Hermes). */
export const formatEventDate = (iso: string) => {
  const [year, month, day] = iso.slice(0, 10).split("-").map(Number);
  return `${String(day).padStart(2, "0")} ${MONTHS[month - 1]} ${year}`;
};

export const formatWorkload = (hours: number) => `${hours}h`;

interface CertificateTileProps {
  certificate: Certificate;
  onPress: () => void;
}

/** Bloco quadrado de tamanho fixo: título no topo, data/carga e ícone na base. */
export const CertificateTile = ({ certificate, onPress }: CertificateTileProps) => {
  const date = formatEventDate(certificate.dataEvento);
  const workload = formatWorkload(certificate.cargaHoraria);

  return (
    <Pressable
      testID={`certificate-tile-${certificate.id}`}
      accessibilityRole="button"
      accessibilityLabel={`${certificate.nome}, evento em ${date}, carga horária de ${certificate.cargaHoraria} horas`}
      onPress={onPress}
      className="aspect-square flex-1 justify-between rounded-3xl border border-blue-100 bg-brand-soft p-4 active:bg-blue-100"
    >
      <Text
        testID={`certificate-name-${certificate.id}`}
        className="text-base font-bold leading-5 text-slate-800"
        numberOfLines={3}
      >
        {certificate.nome}
      </Text>

      <View className="flex-row items-end justify-between">
        <View className="gap-1">
          <View className="flex-row items-center gap-1.5">
            <CalendarDays size={12} color={colors.slate500} />
            <Text testID={`certificate-date-${certificate.id}`} className="text-xs text-slate-500">{date}</Text>
          </View>
          <View className="flex-row items-center gap-1.5">
            <Clock size={12} color={colors.slate500} />
            <Text testID={`certificate-workload-${certificate.id}`} className="text-xs text-slate-500">{workload}</Text>
          </View>
        </View>
        <View className="size-10 items-center justify-center rounded-full bg-brand">
          <Award size={20} color={colors.white} />
        </View>
      </View>
    </Pressable>
  );
};
