import { apiRequest } from "@/shared/api/client";
import { isMockApi } from "@/shared/api/config";
import { NOTIFICATIONS } from "../data/mock";
import type { AppNotification } from "../types";

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export async function fetchNotifications(): Promise<AppNotification[]> {
  if (!isMockApi) return apiRequest<AppNotification[]>("/notificacoes");

  await delay(400);
  return NOTIFICATIONS.map((n) => ({ ...n }));
}

export async function markNotificationAsRead(id: number): Promise<void> {
  if (!isMockApi) {
    await apiRequest<void>(`/notificacoes/${id}/lida`, { method: "PATCH" });
    return;
  }

  const item = NOTIFICATIONS.find((n) => n.id === id);
  if (item) item.lida = true;
}
