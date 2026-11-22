import type { Certificate } from "../types";

export const CERTIFICATES: Certificate[] = [
  { id: 1, nome: "Oficina de Python", eventoId: 2, dataEvento: "2026-11-14", cargaHoraria: 3 },
  { id: 2, nome: "Semana Acadêmica UCSAL", eventoId: 1, dataEvento: "2026-10-20", cargaHoraria: 20 },
  { id: 3, nome: "Introdução ao Git e GitHub", eventoId: null, dataEvento: "2026-09-12", cargaHoraria: 4 },
  { id: 4, nome: "Palestra de Inovação Design", eventoId: null, dataEvento: "2026-08-28", cargaHoraria: 2 },
  { id: 5, nome: "AWS Community Day", eventoId: null, dataEvento: "2026-06-07", cargaHoraria: 8 },
  { id: 6, nome: "Hackathon Liga de Computação", eventoId: null, dataEvento: "2026-05-16", cargaHoraria: 24 },
];
