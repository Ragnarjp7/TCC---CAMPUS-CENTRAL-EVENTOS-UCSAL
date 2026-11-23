import { Alert } from "react-native";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getErrorMessage } from "@/shared/api/errors";
import { cancelEnrollment, enrollInEvent } from "../services/eventsService";
import type { CampusEvent } from "../types";
import { eventDetailKey } from "./useEvent";

type EnrollmentAction = "enroll" | "cancel";

/** Inscrição do aluno logado em um evento: inscrever-se e cancelar. */
export function useEnrollment(eventId: number) {
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const detailKey = eventDetailKey(eventId, user?.id ?? null);

  const mutation = useMutation({
    mutationFn: (action: EnrollmentAction) =>
      action === "enroll" ? enrollInEvent(eventId) : cancelEnrollment(eventId),
    onSuccess: (_data, action) => {
      // resposta imediata na tela; o refetch abaixo confirma com o servidor
      queryClient.setQueryData<CampusEvent>(detailKey, (current) =>
        current
          ? {
              ...current,
              isEnrolled: action === "enroll",
              enrolled: Math.max(0, (current.enrolled ?? 0) + (action === "enroll" ? 1 : -1)),
            }
          : current,
      );
      void queryClient.invalidateQueries({ queryKey: detailKey });
      void queryClient.invalidateQueries({ queryKey: ["events", "feed"] });
      void queryClient.invalidateQueries({ queryKey: ["profile", "enrollments"] });
    },
    onError: (error, action) => {
      Alert.alert(
        action === "enroll" ? "Não foi possível se inscrever" : "Não foi possível cancelar a inscrição",
        getErrorMessage(error),
      );
      // o estado pode ter mudado no servidor (ex.: vagas esgotadas)
      void queryClient.invalidateQueries({ queryKey: detailKey });
    },
  });

  return {
    enroll: () => mutation.mutate("enroll"),
    cancel: () => mutation.mutate("cancel"),
    isPending: mutation.isPending,
    pendingAction: mutation.isPending ? mutation.variables : undefined,
  };
}
