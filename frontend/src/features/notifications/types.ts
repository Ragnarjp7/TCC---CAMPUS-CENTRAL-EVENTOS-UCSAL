/**
 * Categoria da notificação — define o texto de apoio e para onde o cartão leva.
 * - EVENTO: algo sobre um evento (novo, alterado, lembrete) → tela do evento.
 * - CERTIFICADO: certificado emitido/disponível → tela de certificados.
 * - GERAL: aviso sem destino.
 */
export type NotificationType = "EVENTO" | "CERTIFICADO" | "GERAL";

/** Espelha o DTONotificacaoResponse do backend (GET /notificacoes). */
export interface AppNotification {
  id: number;
  mensagem: string;
  dataCriacao: string;
  lida: boolean;
  eventoId: number | null;
  /** Ainda não existe no backend; quando ausente é inferido por `getNotificationType`. */
  tipo?: NotificationType;
  /** Texto complementar exibido com o cartão aberto. Ainda não existe no backend. */
  descricao?: string | null;
}

export const getNotificationType = (n: AppNotification): NotificationType =>
  n.tipo ?? (n.eventoId != null ? "EVENTO" : "GERAL");
