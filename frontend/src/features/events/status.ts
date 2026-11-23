import type { EventStatus } from "./types";

interface StatusStyle {
  label: string;
  /** Classes do fundo e do texto do selo. */
  box: string;
  text: string;
  /** Frase curta para quem acompanha o próprio evento. */
  hint: string;
}

export const EVENT_STATUS: Record<EventStatus, StatusStyle> = {
  AGUARDANDO_ANALISE: {
    label: "Aguardando análise",
    box: "bg-amber-100",
    text: "text-amber-700",
    hint: "A CAEX ainda vai avaliar este evento.",
  },
  AJUSTES_SOLICITADOS: {
    label: "Ajustes solicitados",
    box: "bg-orange-100",
    text: "text-orange-700",
    hint: "A CAEX pediu ajustes. Edite a proposta e reenvie.",
  },
  APROVADO: {
    label: "Aprovado",
    box: "bg-emerald-100",
    text: "text-emerald-700",
    hint: "Evento publicado para a comunidade.",
  },
  REJEITADO: {
    label: "Rejeitado",
    box: "bg-red-100",
    text: "text-red-700",
    hint: "A CAEX não aprovou este evento.",
  },
  CANCELADO: { label: "Cancelado", box: "bg-slate-200", text: "text-slate-500", hint: "Evento cancelado." },
  ENCERRADO: { label: "Encerrado", box: "bg-slate-200", text: "text-slate-500", hint: "Evento encerrado." },
};
