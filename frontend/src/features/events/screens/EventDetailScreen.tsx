import type { ReactNode } from "react";
import { ActivityIndicator, Alert, Pressable, RefreshControl, ScrollView, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import {
  Award,
  Calendar,
  ChevronLeft,
  CircleCheck,
  Clock,
  GraduationCap,
  MapPin,
  MonitorPlay,
  Tag,
  Ticket,
  Users,
} from "lucide-react-native";
import type { LucideIcon } from "lucide-react-native";
import { useAuth } from "@/features/auth/context/AuthContext";
import { useCan } from "@/features/auth/hooks/useCan";
import { getErrorMessage } from "@/shared/api/errors";
import { StateView } from "@/shared/components/StateView";
import { Button, ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";
import { DateBadge } from "../components/DateBadge";
import { StatusBadge } from "../components/StatusBadge";
import { EVENT_STATUS } from "../status";
import { useEnrollment } from "../hooks/useEnrollment";
import { useEvent } from "../hooks/useEvent";
import type { CampusEvent, EventMonth } from "../types";

const MONTH_NAMES: Record<EventMonth, string> = {
  JAN: "janeiro",
  FEV: "fevereiro",
  MAR: "março",
  ABR: "abril",
  MAI: "maio",
  JUN: "junho",
  JUL: "julho",
  AGO: "agosto",
  SET: "setembro",
  OUT: "outubro",
  NOV: "novembro",
  DEZ: "dezembro",
};
// Eventos do modo mock não têm data completa
const FALLBACK_YEAR = 2026;

const eventYear = (e: CampusEvent) => (e.startsAt ? Number(e.startsAt.slice(0, 4)) : FALLBACK_YEAR);

/** Compara a data de fim (horário local, sem fuso) com agora. */
const hasEnded = (e: CampusEvent) => {
  if (!e.endsAt) return false;
  const [date, time = "00:00"] = e.endsAt.split("T");
  const [y, m, d] = date.split("-").map(Number);
  const [hh, mm] = time.split(":").map(Number);
  return new Date(y, m - 1, d, hh, mm).getTime() < Date.now();
};

interface InfoRowProps {
  Icon: LucideIcon;
  children: ReactNode;
  testID: string;
}

const InfoRow = ({ Icon, children, testID }: InfoRowProps) => (
  <View testID={testID} className="flex-row items-center gap-3">
    <View className="size-8 items-center justify-center rounded-lg bg-brand-soft">
      <Icon size={16} color={colors.brand} />
    </View>
    <Text testID={`${testID}-text`} className="flex-1 text-sm text-slate-600">{children}</Text>
  </View>
);

const BackButton = ({ onPress, top }: { onPress: () => void; top: number }) => (
  <Pressable
    testID="event-detail-back-button"
    onPress={onPress}
    accessibilityRole="button"
    accessibilityLabel="Voltar"
    style={{ top }}
    className="absolute left-4 z-10 size-9 items-center justify-center rounded-full bg-white/90 shadow"
  >
    <ChevronLeft size={20} color={colors.slate800} />
  </Pressable>
);

const Capacity = ({ capacity, enrolled }: { capacity: number; enrolled: number }) => {
  const left = Math.max(0, capacity - enrolled);
  const ratio = Math.min(1, enrolled / capacity);
  return (
    <View testID="event-detail-capacity" className="mt-5 rounded-xl border border-slate-100 p-3">
      <View className="flex-row items-center justify-between">
        <View className="flex-row items-center gap-1.5">
          <Users size={14} color={colors.slate500} />
          <Text className="text-xs font-semibold text-slate-600">Vagas</Text>
        </View>
        <Text testID="event-detail-capacity-text" className={`text-xs font-semibold ${left === 0 ? "text-red-500" : "text-brand"}`}>
          {left === 0 ? "Esgotado" : `${left} de ${capacity} disponíveis`}
        </Text>
      </View>
      <View className="mt-2 h-1.5 overflow-hidden rounded-full bg-slate-100">
        <View
          testID="event-detail-capacity-bar"
          style={{ width: `${ratio * 100}%` }}
          className={`h-full rounded-full ${left === 0 ? "bg-red-400" : "bg-brand"}`}
        />
      </View>
    </View>
  );
};

const Chip = ({ Icon, label, testID }: { Icon: LucideIcon; label: string; testID: string }) => (
  <View testID={testID} className="flex-row items-center gap-1 rounded-full bg-brand-soft px-3 py-1">
    <Icon size={12} color={colors.brand} />
    <Text className="text-[11px] font-semibold text-brand">{label}</Text>
  </View>
);

const formatPrice = (price?: number) =>
  !price ? "Gratuito" : price.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

const isSoldOut = (e: CampusEvent) => e.capacity != null && (e.enrolled ?? 0) >= e.capacity;

interface EnrollFooterProps {
  event: CampusEvent;
  onSignIn: () => void;
  /** Abre a proposta (análise da CAEX ou acompanhamento do responsável). */
  onOpenReview: () => void;
}

/** Botão de inscrição conforme o perfil (visitante, aluno, demais) e o estado do evento. */
const EnrollFooter = ({ event, onSignIn, onOpenReview }: EnrollFooterProps) => {
  const { user } = useAuth();
  const canEnroll = useCan("event:enroll");
  const canReview = useCan("event:review");
  const enrollment = useEnrollment(event.id);

  const confirmCancel = () =>
    Alert.alert("Cancelar inscrição", `Deseja cancelar sua inscrição em "${event.title}"?`, [
      { text: "Voltar", style: "cancel" },
      { text: "Cancelar inscrição", style: "destructive", onPress: enrollment.cancel },
    ]);

  // Só eventos aprovados recebem inscrições; os demais só chegam aqui para a CAEX e os responsáveis
  if (event.status && event.status !== "APROVADO") {
    return (
      <View testID="event-detail-not-approved" className="gap-2">
        <View className="flex-row items-center justify-center gap-2">
          <StatusBadge testID="event-detail-status" status={event.status} />
          <Text className="flex-shrink text-xs text-slate-500">{EVENT_STATUS[event.status].hint}</Text>
        </View>
        <Button
          testID="event-detail-review-button"
          title={canReview && event.status === "AGUARDANDO_ANALISE" ? "Analisar evento" : "Ver proposta"}
          onPress={onOpenReview}
        />
      </View>
    );
  }

  if (hasEnded(event)) {
    return <Button testID="event-detail-ended-button" title="Evento encerrado" disabled className="opacity-50" />;
  }

  // Visitante: RF33 permite ver o evento; a inscrição exige login
  if (!user) {
    return <Button testID="event-detail-signin-button" title="Entrar para se inscrever" onPress={onSignIn} />;
  }

  // Logado, mas sem papel de aluno (CAEX, professor...): backend exige ALUNO
  if (!canEnroll) {
    return (
      <Button testID="event-detail-enroll-unavailable-button" title="Inscrições exclusivas para estudantes" disabled className="opacity-50" />
    );
  }

  if (event.isEnrolled) {
    return (
      <View testID="event-detail-enrolled" className="gap-2">
        <View className="flex-row items-center justify-center gap-1.5">
          <CircleCheck size={16} color={colors.brand} />
          <Text testID="event-detail-enrolled-text" className="text-sm font-semibold text-brand">Você está inscrito</Text>
        </View>
        <Button
          testID="event-detail-cancel-button"
          variant="outline"
          title={enrollment.pendingAction === "cancel" ? "Cancelando..." : "Cancelar inscrição"}
          disabled={enrollment.isPending}
          onPress={confirmCancel}
          icon={enrollment.pendingAction === "cancel" ? <ActivityIndicator size="small" color={colors.slate500} /> : undefined}
        />
      </View>
    );
  }

  if (isSoldOut(event)) {
    return <Button testID="event-detail-soldout-button" title="Vagas esgotadas" disabled className="opacity-50" />;
  }

  return (
    <Button
      testID="event-detail-enroll-button"
      title={enrollment.pendingAction === "enroll" ? "Inscrevendo..." : "Inscrever-se"}
      disabled={enrollment.isPending}
      onPress={enrollment.enroll}
      icon={enrollment.pendingAction === "enroll" ? <ActivityIndicator size="small" color={colors.white} /> : undefined}
    />
  );
};

export default function EventDetailScreen({ navigation, route }: RootScreenProps<"EventDetail">) {
  const { eventId, event: preview } = route.params;
  const insets = useSafeAreaInsets();
  const query = useEvent(eventId, { initialData: preview });
  const event = query.data;
  const goBack = () => navigation.goBack();

  if (!event) {
    return (
      <View testID="event-detail-screen" className="flex-1 bg-white">
        <StatusBar style="dark" />
        <BackButton onPress={goBack} top={insets.top + 8} />
        <StateView
          testID="event-detail-state-view"
          state={query.isError ? "error" : "loading"}
          errorMessage={query.isError ? getErrorMessage(query.error) : undefined}
          emptyMessage="Evento não encontrado."
          onRetry={() => void query.refetch()}
        />
      </View>
    );
  }

  const online = event.mode === "Online";

  return (
    <View testID="event-detail-screen" className="flex-1 bg-white">
      <StatusBar style="dark" />
      <ScrollView
        testID="event-detail-scroll"
        contentContainerClassName="pb-4"
        refreshControl={<RefreshControl refreshing={query.isRefetching} onRefresh={() => void query.refetch()} />}
      >
        <View>
          <ImgSlot testID="event-detail-cover" className="h-56 w-full rounded-none" />
          <BackButton onPress={goBack} top={insets.top + 8} />
          <View
            testID="event-detail-mode"
            style={{ top: insets.top + 12 }}
            className="absolute right-4 flex-row items-center gap-1 rounded-full bg-white/90 px-3 py-1 shadow"
          >
            {online ? <MonitorPlay size={12} color={colors.brand} /> : <MapPin size={12} color={colors.brand} />}
            <Text className="text-[11px] font-semibold text-brand">{online ? "Online" : "Presencial"}</Text>
          </View>
        </View>

        <View className="-mt-4 rounded-t-3xl bg-white px-5 pt-6">
          <View className="flex-row items-center gap-3">
            <DateBadge testID="event-detail-date-badge" day={event.day} month={event.month} className="size-12" />
            <Text testID="event-detail-title" className="flex-1 text-xl font-extrabold text-slate-900">{event.title}</Text>
          </View>

          <View testID="event-detail-organizer" className="mt-4 flex-row items-center gap-3 rounded-xl bg-slate-50 p-3">
            <ImgSlot testID="event-detail-organizer-avatar" className="size-10 rounded-full" />
            <View>
              <Text className="text-[11px] text-slate-400">Organizado por</Text>
              <Text testID="event-detail-organizer-name" className="text-sm font-semibold text-slate-800">{event.organizer}</Text>
            </View>
          </View>

          <View testID="event-detail-highlights" className="mt-4 flex-row flex-wrap gap-2">
            {(event.kind ?? event.categoryName) && (
              <Chip testID="event-detail-kind" Icon={Tag} label={(event.kind ?? event.categoryName) as string} />
            )}
            {event.complementaryHours != null && event.complementaryHours > 0 && (
              <Chip
                testID="event-detail-hours"
                Icon={GraduationCap}
                label={`${event.complementaryHours}h complementares`}
              />
            )}
            {event.certificate && <Chip testID="event-detail-certificate" Icon={Award} label="Com certificado" />}
            <Chip testID="event-detail-price" Icon={Ticket} label={formatPrice(event.price)} />
          </View>

          <View testID="event-detail-info" className="mt-5 gap-3">
            <InfoRow testID="event-detail-date" Icon={Calendar}>
              {event.day} de {MONTH_NAMES[event.month]} de {eventYear(event)}
            </InfoRow>
            <InfoRow testID="event-detail-time" Icon={Clock}>{event.time}</InfoRow>
            <InfoRow testID="event-detail-place" Icon={online ? MonitorPlay : MapPin}>{event.place}</InfoRow>
            {event.audience && <InfoRow testID="event-detail-audience" Icon={Users}>{event.audience}</InfoRow>}
          </View>

          {event.capacity != null && <Capacity capacity={event.capacity} enrolled={event.enrolled ?? 0} />}

          <Text testID="event-detail-about-title" className="mt-6 text-sm font-bold text-slate-900">Sobre o evento</Text>
          <Text testID="event-detail-about-text" className="mt-1.5 text-sm leading-relaxed text-slate-500">
            {event.description ?? "O organizador ainda não adicionou uma descrição para este evento."}
          </Text>

          {event.requirements && (
            <>
              <Text testID="event-detail-requirements-title" className="mt-5 text-sm font-bold text-slate-900">O que levar</Text>
              <Text testID="event-detail-requirements-text" className="mt-1.5 text-sm leading-relaxed text-slate-500">{event.requirements}</Text>
            </>
          )}

          {event.speakers && event.speakers.length > 0 && (
            <>
              <Text className="mt-5 text-sm font-bold text-slate-900">
                {event.speakers.length > 1 ? "Palestrantes" : "Palestrante"}
              </Text>
              <View testID="event-detail-speakers" className="mt-2 gap-3">
                {event.speakers.map((s, i) => (
                  <View key={`${s.name}-${i}`} testID={`event-detail-speaker-${i}`} className="flex-row items-center gap-3">
                    <ImgSlot className="size-10 rounded-full" />
                    <View className="flex-1">
                      <Text className="text-sm font-semibold text-slate-800">{s.name}</Text>
                      {s.role && <Text className="text-xs text-slate-400">{s.role}</Text>}
                    </View>
                  </View>
                ))}
              </View>
            </>
          )}
        </View>
      </ScrollView>

      <View testID="event-detail-footer" style={{ paddingBottom: Math.max(insets.bottom, 16) }} className="border-t border-slate-100 bg-white px-5 pt-3">
        <EnrollFooter
          event={event}
          onSignIn={() => navigation.navigate("Login")}
          onOpenReview={() => navigation.navigate("ReviewEvent", { eventId: event.id })}
        />
      </View>
    </View>
  );
}
