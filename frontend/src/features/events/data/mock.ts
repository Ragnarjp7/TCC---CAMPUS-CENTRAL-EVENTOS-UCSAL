import type { CampusEvent } from "../types";

export const EVENTS: CampusEvent[] = [
  { id: 1, day: "22", month: "NOV", title: "Semana Acadêmica UCSAL", place: "Auditório Principal", mode: "Presenciais", organizer: "Liga de Computação", time: "19h às 22h" },
  { id: 2, day: "14", month: "NOV", title: "Oficina de Python", place: "Lab. de Informática", mode: "Presenciais", organizer: "Liga de Computação", time: "14h às 17h" },
  { id: 3, day: "25", month: "NOV", title: "Encontro de Gestão", place: "Sala 204 - Bloco B", mode: "Presenciais", organizer: "Atlética de Gestão", time: "18h às 20h" },
  { id: 4, day: "05", month: "DEZ", title: "Workshop de UX", place: "Google Meet", mode: "Online", organizer: "Liga de Design", time: "19h às 21h" },
];
