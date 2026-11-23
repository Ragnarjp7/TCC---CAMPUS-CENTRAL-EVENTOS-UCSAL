import { useState } from "react";
import { RefreshControl, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { getErrorMessage } from "@/shared/api/errors";
import { StateView } from "@/shared/components/StateView";
import { Tabs } from "@/shared/components/ui";
import { resolveViewState } from "@/shared/state/viewState";
import type { RootScreenProps } from "@/navigation/types";
import { SubmissionCard } from "@/features/events/components/SubmissionCard";
import { useReviewQueue } from "@/features/events/hooks/useSubmissions";
import type { EventStatus } from "@/features/events/types";

const TABS = ["Pendentes", "Ajustes", "Aprovados", "Rejeitados"] as const;
type Tab = (typeof TABS)[number];

const TAB_STATUS: Record<Tab, EventStatus> = {
  Pendentes: "AGUARDANDO_ANALISE",
  Ajustes: "AJUSTES_SOLICITADOS",
  Aprovados: "APROVADO",
  Rejeitados: "REJEITADO",
};

const EMPTY_MESSAGE: Record<Tab, string> = {
  Pendentes: "Nenhum evento aguardando análise.",
  Ajustes: "Nenhum evento aguardando ajustes do responsável.",
  Aprovados: "Nenhum evento aprovado.",
  Rejeitados: "Nenhum evento rejeitado.",
};

/** RF29 - Fila da CAEX: eventos criados por ligas, professores e coordenadores, por situação. */
export default function ReviewQueueScreen({ navigation }: RootScreenProps<"ReviewQueue">) {
  const [tab, setTab] = useState<Tab>("Pendentes");
  const query = useReviewQueue(TAB_STATUS[tab]);
  const items = query.data ?? [];
  const viewState = resolveViewState(query, items.length === 0);

  return (
    <SafeAreaView testID="review-queue-screen" edges={["bottom"]} className="flex-1 bg-slate-50">
      <Tabs testID="review-queue-tabs" items={TABS} value={tab} onChange={setTab} className="bg-white" />

      {viewState === "ready" ? (
        <ScrollView
          testID="review-queue-list"
          contentContainerClassName="gap-3 p-4 pb-10"
          refreshControl={<RefreshControl refreshing={query.isRefetching} onRefresh={() => void query.refetch()} />}
        >
          {tab === "Pendentes" && (
            <View className="px-1">
              <Text testID="review-queue-count" className="text-xs text-slate-400">
                {items.length === 1 ? "1 evento na fila" : `${items.length} eventos na fila`} · os mais antigos primeiro
              </Text>
            </View>
          )}
          {items.map((s) => (
            <SubmissionCard
              key={s.id}
              submission={s}
              variant="review"
              actionLabel={tab === "Pendentes" ? "Analisar" : "Ver"}
              onPress={() => navigation.navigate("ReviewEvent", { eventId: s.id })}
            />
          ))}
        </ScrollView>
      ) : (
        <StateView
          testID="review-queue-state-view"
          state={viewState}
          errorMessage={getErrorMessage(query.error)}
          emptyMessage={EMPTY_MESSAGE[tab]}
          onRetry={() => void query.refetch()}
        />
      )}
    </SafeAreaView>
  );
}
