import type { EventMonth } from "./types";

/** Na ordem do calendário: o índice + 1 é o número do mês. */
export const EVENT_MONTHS: readonly EventMonth[] = ["JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"];

/** "NOV" -> "11" */
export const monthNumber = (month: EventMonth) => String(EVENT_MONTHS.indexOf(month) + 1).padStart(2, "0");

/** "2026-11-22T19:00:00" → partes numéricas, sem passar por fuso horário. */
export function parseLocalDateTime(iso: string) {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(iso);
  if (!m) return null;
  return { year: Number(m[1]), month: Number(m[2]), day: Number(m[3]), hour: Number(m[4]), minute: Number(m[5]) };
}

/** 19, 0 → "19h"; 19, 30 → "19h30" */
export const formatHour = (hour: number, minute: number) => `${hour}h${minute ? String(minute).padStart(2, "0") : ""}`;

const pad = (n: number) => String(n).padStart(2, "0");

/**
 * Data e horário de um evento para leitura rápida (análise do CAEX, "Meus eventos").
 * Mesmo dia: "22/11/2026 · 19h às 22h". Vários dias: "22/11/2026 19h até 24/11/2026 22h".
 */
export function formatEventSchedule(startsAt: string, endsAt: string): { date: string; time: string } {
  const s = parseLocalDateTime(startsAt);
  const e = parseLocalDateTime(endsAt);
  if (!s || !e) return { date: "", time: "" };

  const startDate = `${pad(s.day)}/${pad(s.month)}/${s.year}`;
  const endDate = `${pad(e.day)}/${pad(e.month)}/${e.year}`;

  if (startDate === endDate) {
    return { date: startDate, time: `${formatHour(s.hour, s.minute)} às ${formatHour(e.hour, e.minute)}` };
  }
  return {
    date: `${startDate} a ${endDate}`,
    time: `Início ${formatHour(s.hour, s.minute)} · término ${formatHour(e.hour, e.minute)}`,
  };
}

/** "2026-11-20T10:15:00" → "20/11/2026 às 10h15" */
export function formatDateTime(iso: string | null): string {
  const d = iso ? parseLocalDateTime(iso) : null;
  return d ? `${pad(d.day)}/${pad(d.month)}/${d.year} às ${formatHour(d.hour, d.minute)}` : "";
}
