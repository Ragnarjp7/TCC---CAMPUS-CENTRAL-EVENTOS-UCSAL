import { useLayoutEffect, useState } from "react";
import type { ReactNode } from "react";
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import type { LucideIcon } from "lucide-react-native";
import {
  Calendar,
  CircleAlert,
  Clock,
  FileText,
  MapPin,
  MessageSquareText,
  MonitorPlay,
  Tag,
  UserRound,
  Users,
} from "lucide-react-native";
import { useCan } from "@/features/auth/hooks/useCan";
import { getErrorMessage } from "@/shared/api/errors";
import { StateView } from "@/shared/components/StateView";
import { Button } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";
import { StatusBadge } from "@/features/events/components/StatusBadge";
import { formatDateTime, formatEventSchedule, parseLocalDateTime } from "@/features/events/dates";
import { useReviewDecision, useSubmission } from "@/features/events/hooks/useSubmissions";
import { describeProposal, parseStoredProposal } from "@/features/events/proposal";
import { EVENT_STATUS } from "@/features/events/status";
import type { EventSubmission, ReviewDecision } from "@/features/events/types";

/** Mesmo limite de DTOStatusRequest.observacao no backend. */
const NOTE_MAX = 1000;

const DECISION_COPY: Record<ReviewDecision, { title: string; action: string; confirm: string; done: string }> = {
  APROVADO: {
    title: "Aprovar evento",
    action: "Aprovar",
    confirm: "O evento será publicado para a comunidade e o responsável será notificado.",
    done: "Evento aprovado e publicado.",
  },
  AJUSTES_SOLICITADOS: {
    title: "Solicitar ajustes",
    action: "Solicitar ajustes",
    confirm: "O responsável será notificado para ajustar e reenviar a proposta.",
    done: "Ajustes solicitados ao responsável.",
  },
  REJEITADO: {
    title: "Rejeitar evento",
    action: "Rejeitar",
    confirm: "A rejeição é definitiva: o evento não poderá ser reenviado.",
    done: "Evento rejeitado.",
  },
};

const hasEnded = (s: EventSubmission) => {
  const end = parseLocalDateTime(s.endsAt);
  return end ? new Date(end.year, end.month - 1, end.day, end.hour, end.minute).getTime() < Date.now() : false;
};

// ---------- Blocos ----------

const Card = ({ title, children, testID }: { title: string; children: ReactNode; testID?: string }) => (
  <View testID={testID} className="gap-3 rounded-2xl bg-white p-4 shadow-sm">
    <Text className="text-xs font-extrabold uppercase tracking-wide text-slate-400">{title}</Text>
    {children}
  </View>
);

interface CriterionProps {
  Icon: LucideIcon;
  label: string;
  children: ReactNode;
  testID?: string;
}

/** Um dos pontos que o RF29 manda considerar: nome, descrição, data, horário, local e direção. */
const Criterion = ({ Icon, label, children, testID }: CriterionProps) => (
  <View testID={testID} className="flex-row gap-3">
    <View className="size-8 items-center justify-center rounded-full bg-brand-soft">
      <Icon size={15} color={colors.brand} />
    </View>
    <View className="flex-1 gap-0.5">
      <Text className="text-[11px] font-semibold text-slate-400">{label}</Text>
      {typeof children === "string" ? <Text className="text-sm text-slate-700">{children}</Text> : children}
    </View>
  </View>
);

// ---------- Tela ----------

/**
 * RF29 - A CAEX analisa o evento (nome, descrição, data, horário, local e quem está na direção)
 * e o aprova, pede ajustes ou rejeita, sempre com justificativa.
 * Quem enviou também abre esta tela para acompanhar a proposta (somente leitura).
 */
export default function ReviewEventScreen({ navigation, route }: RootScreenProps<"ReviewEvent">) {
  const { eventId } = route.params;
  const canReview = useCan("event:review");
  const query = useSubmission(eventId);
  const decision = useReviewDecision(eventId);
  const [note, setNote] = useState("");
  const [noteError, setNoteError] = useState<string | null>(null);

  useLayoutEffect(() => {
    navigation.setOptions({ title: canReview ? "Análise do evento" : "Proposta do evento" });
  }, [navigation, canReview]);

  const s = query.data;

  if (!s) {
    return (
      <SafeAreaView testID="review-event-screen" edges={["bottom"]} className="flex-1 bg-slate-50">
        <StateView
          testID="review-event-state-view"
          state={query.isError ? "error" : "loading"}
          errorMessage={getErrorMessage(query.error)}
          emptyMessage=""
          onRetry={() => void query.refetch()}
        />
      </SafeAreaView>
    );
  }

  const schedule = formatEventSchedule(s.startsAt, s.endsAt);
  const proposal = parseStoredProposal(s.proposal);
  const sections = proposal ? describeProposal(proposal) : [];
  const ended = hasEnded(s);
  const canDecide = canReview && s.status === "AGUARDANDO_ANALISE";

  const decide = (choice: ReviewDecision) => {
    if (!note.trim()) {
      setNoteError("Escreva a justificativa da decisão. Ela será enviada ao responsável.");
      return;
    }
    setNoteError(null);
    const copy = DECISION_COPY[choice];
    Alert.alert(copy.title, copy.confirm, [
      { text: "Voltar", style: "cancel" },
      {
        text: copy.action,
        style: choice === "REJEITADO" ? "destructive" : "default",
        onPress: () =>
          decision.mutate(
            { decision: choice, note },
            {
              onSuccess: () => Alert.alert("Decisão registrada", copy.done, [{ text: "OK", onPress: () => navigation.goBack() }]),
              onError: (error) => Alert.alert("Não foi possível registrar a decisão", getErrorMessage(error)),
            },
          ),
      },
    ]);
  };

  const pending = decision.isPending ? decision.variables?.decision : undefined;

  return (
    <SafeAreaView testID="review-event-screen" edges={["bottom"]} className="flex-1 bg-slate-50">
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} keyboardVerticalOffset={90} className="flex-1">
        <ScrollView
          testID="review-event-scroll"
          contentContainerClassName="gap-4 p-4 pb-10"
          keyboardShouldPersistTaps="handled"
          refreshControl={<RefreshControl refreshing={query.isRefetching} onRefresh={() => void query.refetch()} />}
        >
          <View className="gap-2 px-1">
            <StatusBadge testID="review-event-status" status={s.status} />
            <Text testID="review-event-title" className="text-xl font-extrabold text-slate-900">{s.title}</Text>
            {s.submittedAt && (
              <Text className="text-xs text-slate-400">Enviado para análise em {formatDateTime(s.submittedAt)}</Text>
            )}
            {!canReview && <Text className="text-xs text-slate-500">{EVENT_STATUS[s.status].hint}</Text>}
          </View>

          {s.caexNote && (
            <View testID="review-event-last-note" className="flex-row gap-3 rounded-2xl bg-white p-4 shadow-sm">
              <MessageSquareText size={18} color={colors.slate500} />
              <View className="flex-1 gap-1">
                <Text className="text-[11px] font-semibold text-slate-400">
                  {s.status === "AGUARDANDO_ANALISE" ? "Observação da análise anterior" : "Justificativa da CAEX"}
                </Text>
                <Text className="text-sm text-slate-700">{s.caexNote}</Text>
              </View>
            </View>
          )}

          <Card title="Dados para análise" testID="review-event-criteria">
            <Criterion testID="review-event-name" Icon={Tag} label="Nome do evento">
              <Text className="text-sm font-semibold text-slate-800">{s.title}</Text>
              {s.categoryName && <Text className="text-xs text-slate-400">{s.categoryName}</Text>}
            </Criterion>
            <Criterion testID="review-event-description" Icon={FileText} label="Descrição">{s.description}</Criterion>
            <Criterion testID="review-event-date" Icon={Calendar} label="Data">{schedule.date}</Criterion>
            <Criterion testID="review-event-time" Icon={Clock} label="Horário">{schedule.time}</Criterion>
            <Criterion testID="review-event-place" Icon={s.mode === "Online" ? MonitorPlay : MapPin} label="Local">
              <Text className="text-sm text-slate-700">{s.place}</Text>
              <Text className="text-xs text-slate-400">
                {s.mode === "Online" ? "Online" : "Presencial"}
                {s.capacity != null ? ` · até ${s.capacity} participantes` : " · sem limite de vagas"}
              </Text>
            </Criterion>
            <Criterion testID="review-event-direction" Icon={UserRound} label="Direção do evento">
              <Text className="text-sm font-semibold text-slate-800">{s.responsible.name}</Text>
              {s.responsible.email && <Text className="text-xs text-slate-500">{s.responsible.email}</Text>}
              <Text className="text-xs text-slate-400">
                {s.leagueName ? `Pela ${s.leagueName}` : "Evento institucional (professor ou coordenador)"}
              </Text>
              {proposal?.isEntidade === "sim" && proposal.presidenteNome.trim() !== "" && (
                <Text className="text-xs text-slate-400">Presidente da entidade: {proposal.presidenteNome.trim()}</Text>
              )}
            </Criterion>
          </Card>

          {sections.length > 0 ? (
            sections.map((section) => (
              <Card key={section.title} title={section.title} testID={`review-event-proposal-${section.title}`}>
                {section.rows.map((row) => (
                  <View key={row.label} className="gap-0.5">
                    <Text className="text-[11px] font-semibold text-slate-400">{row.label}</Text>
                    <Text selectable className="text-sm text-slate-700">{row.value}</Text>
                  </View>
                ))}
              </Card>
            ))
          ) : (
            <Card title="Proposta completa" testID="review-event-no-proposal">
              <Text className="text-sm text-slate-400">Este evento foi cadastrado sem o formulário completo de proposta.</Text>
            </Card>
          )}

          {canDecide && (
            <Card title="Decisão da CAEX" testID="review-event-decision">
              {ended && (
                <View className="flex-row items-center gap-2 rounded-xl bg-red-50 p-3">
                  <CircleAlert size={16} color="#b91c1c" />
                  <Text className="flex-1 text-xs text-red-700">Este evento já terminou e não pode mais ser aprovado.</Text>
                </View>
              )}
              <View className="gap-1.5">
                <Text className="text-xs font-semibold text-slate-400">
                  Justificativa<Text className="text-red-500"> *</Text>
                </Text>
                <TextInput
                  testID="review-event-note"
                  value={note}
                  onChangeText={(v) => {
                    setNote(v);
                    if (noteError && v.trim()) setNoteError(null);
                  }}
                  maxLength={NOTE_MAX}
                  multiline
                  textAlignVertical="top"
                  placeholder="Explique a decisão ou descreva os ajustes necessários"
                  placeholderTextColor={colors.slate300}
                  className={`min-h-28 rounded-xl border bg-slate-50 px-3.5 py-3 text-sm text-slate-600 focus:border-brand ${
                    noteError ? "border-red-400" : "border-slate-200"
                  }`}
                />
                {noteError ? (
                  <Text className="text-[11px] font-medium text-red-500">{noteError}</Text>
                ) : (
                  <Text className="text-[11px] text-slate-400">{note.length}/{NOTE_MAX} caracteres · enviada ao responsável</Text>
                )}
              </View>

              <View className="gap-2">
                <Button
                  testID="review-event-approve"
                  title={pending === "APROVADO" ? "Aprovando..." : "Aprovar"}
                  disabled={decision.isPending || ended}
                  className={ended ? "opacity-50" : ""}
                  icon={pending === "APROVADO" ? <ActivityIndicator size="small" color={colors.white} /> : undefined}
                  onPress={() => decide("APROVADO")}
                />
                <Button
                  testID="review-event-request-changes"
                  variant="outline"
                  title={pending === "AJUSTES_SOLICITADOS" ? "Enviando..." : "Solicitar ajustes"}
                  disabled={decision.isPending}
                  onPress={() => decide("AJUSTES_SOLICITADOS")}
                />
                <Pressable
                  testID="review-event-reject"
                  accessibilityRole="button"
                  disabled={decision.isPending}
                  onPress={() => decide("REJEITADO")}
                  className="h-12 items-center justify-center rounded-xl active:bg-red-50"
                >
                  <Text className="text-sm font-semibold text-red-600">
                    {pending === "REJEITADO" ? "Rejeitando..." : "Rejeitar"}
                  </Text>
                </Pressable>
              </View>
            </Card>
          )}

          {!canReview && s.status === "AJUSTES_SOLICITADOS" && (
            <Button
              testID="review-event-edit"
              title="Ajustar e reenviar"
              onPress={() => navigation.navigate("CreateEvent", { eventId: s.id })}
            />
          )}

          {canReview && s.status !== "AGUARDANDO_ANALISE" && (
            <View testID="review-event-decided" className="flex-row items-center gap-2 px-1">
              <Users size={14} color={colors.slate400} />
              <Text className="flex-1 text-xs text-slate-400">
                {s.status === "AJUSTES_SOLICITADOS"
                  ? "Aguardando o responsável reenviar com os ajustes."
                  : "A análise deste evento já foi concluída."}
              </Text>
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
