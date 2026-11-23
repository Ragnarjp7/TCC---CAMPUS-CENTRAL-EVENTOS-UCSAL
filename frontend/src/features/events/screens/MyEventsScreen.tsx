import { useLayoutEffect } from "react";
import { Pressable, RefreshControl, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Plus } from "lucide-react-native";
import { useCan } from "@/features/auth/hooks/useCan";
import { getErrorMessage } from "@/shared/api/errors";
import { StateView } from "@/shared/components/StateView";
import { resolveViewState } from "@/shared/state/viewState";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";
import { SubmissionCard } from "../components/SubmissionCard";
import { useMyEvents } from "../hooks/useSubmissions";
import { EVENT_STATUS } from "../status";
import type { EventSubmission } from "../types";

/** RF27 - Eventos que o usuário enviou (ou da liga que preside) e a situação de cada um na CAEX. */
export default function MyEventsScreen({ navigation }: RootScreenProps<"MyEvents">) {
  const canCreate = useCan("event:create");
  const query = useMyEvents();
  const items = query.data ?? [];
  const viewState = resolveViewState(query, items.length === 0);

  useLayoutEffect(() => {
    navigation.setOptions({
      headerRight: canCreate
        ? () => (
            <Pressable
              testID="my-events-new-button"
              accessibilityRole="button"
              accessibilityLabel="Nova proposta de evento"
              hitSlop={8}
              onPress={() => navigation.navigate("CreateEvent")}
              className="flex-row items-center gap-1 active:opacity-70"
            >
              <Plus size={18} color={colors.brand} />
              <Text className="text-sm font-semibold text-brand">Nova</Text>
            </Pressable>
          )
        : undefined,
    });
  }, [navigation, canCreate]);

  // Aprovado: página pública do evento. Demais: a proposta como a CAEX vê (com "ajustar" quando for o caso).
  const open = (s: EventSubmission) =>
    s.status === "APROVADO"
      ? navigation.navigate("EventDetail", { eventId: s.id })
      : navigation.navigate("ReviewEvent", { eventId: s.id });

  const actionFor = (s: EventSubmission) =>
    s.status === "AJUSTES_SOLICITADOS" ? "Ver ajustes pedidos" : s.status === "APROVADO" ? "Ver evento" : "Ver proposta";

  const pendingAdjustments = items.filter((s) => s.status === "AJUSTES_SOLICITADOS").length;

  return (
    <SafeAreaView testID="my-events-screen" edges={["bottom"]} className="flex-1 bg-slate-50">
      {viewState === "ready" ? (
        <ScrollView
          testID="my-events-list"
          contentContainerClassName="gap-3 p-4 pb-10"
          refreshControl={<RefreshControl refreshing={query.isRefetching} onRefresh={() => void query.refetch()} />}
        >
          {pendingAdjustments > 0 && (
            <View testID="my-events-adjustments-banner" className="rounded-2xl bg-orange-50 p-4">
              <Text className="text-sm font-semibold text-orange-800">
                {pendingAdjustments === 1
                  ? "1 evento precisa de ajustes."
                  : `${pendingAdjustments} eventos precisam de ajustes.`}
              </Text>
              <Text className="mt-0.5 text-xs text-orange-700">{EVENT_STATUS.AJUSTES_SOLICITADOS.hint}</Text>
            </View>
          )}
          {items.map((s) => (
            <SubmissionCard key={s.id} submission={s} variant="owner" actionLabel={actionFor(s)} onPress={() => open(s)} />
          ))}
        </ScrollView>
      ) : viewState === "empty" ? (
        <View testID="my-events-empty" className="flex-1 items-center gap-4 px-8 pt-10">
          <Text className="text-center text-sm text-slate-400">Você ainda não enviou nenhum evento para a CAEX.</Text>
          {canCreate && (
            <Pressable
              testID="my-events-empty-create"
              accessibilityRole="button"
              onPress={() => navigation.navigate("CreateEvent")}
              className="rounded-full bg-brand px-5 py-2.5 active:opacity-80"
            >
              <Text className="text-sm font-semibold text-white">Criar proposta de evento</Text>
            </Pressable>
          )}
        </View>
      ) : (
        <StateView
          testID="my-events-state-view"
          state={viewState}
          errorMessage={getErrorMessage(query.error)}
          emptyMessage=""
          onRetry={() => void query.refetch()}
        />
      )}
    </SafeAreaView>
  );
}
