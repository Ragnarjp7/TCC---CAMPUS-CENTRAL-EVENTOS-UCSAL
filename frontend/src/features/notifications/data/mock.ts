import type { AppNotification } from "../types";

const hoursAgo = (h: number) => new Date(Date.now() - h * 3_600_000).toISOString();

export const NOTIFICATIONS: AppNotification[] = [
  { id: 1, mensagem: "Inscrições abertas para a Semana Acadêmica UCSAL.", dataCriacao: hoursAgo(120), lida: true, eventoId: 1, tipo: "EVENTO", descricao: "Garanta sua vaga: as inscrições ficam abertas até o dia do evento ou até esgotarem as vagas." },
  { id: 2, mensagem: "Oficina de Python foi movida para o Lab. de Informática 2.", dataCriacao: hoursAgo(96), lida: true, eventoId: 2, tipo: "EVENTO", descricao: "O horário continua o mesmo. Só o local mudou." },
  { id: 3, mensagem: "Novo evento da Atlética de Gestão: Encontro de Gestão.", dataCriacao: hoursAgo(72), lida: true, eventoId: 3, tipo: "EVENTO" },
  { id: 8, mensagem: "Seu certificado da Oficina de Python está disponível.", dataCriacao: hoursAgo(48), lida: false, eventoId: 2, tipo: "CERTIFICADO", descricao: "Você já pode visualizar e baixar o certificado de participação na página de certificados." },
  { id: 4, mensagem: "Workshop de UX acontece amanhã às 19h pelo Google Meet.", dataCriacao: hoursAgo(30), lida: false, eventoId: 4, tipo: "EVENTO", descricao: "O link da sala será liberado 15 minutos antes do início." },
  { id: 5, mensagem: "Hackathon Liga de Computação: inscrições até sexta.", dataCriacao: hoursAgo(20), lida: false, eventoId: null, tipo: "GERAL", descricao: "Monte sua equipe de até 4 pessoas e fique de olho no canal da Liga para mais detalhes." },
  { id: 6, mensagem: "Palestra de Inovação Design confirmada no Auditório.", dataCriacao: hoursAgo(5), lida: false, eventoId: null, tipo: "GERAL" },
  { id: 7, mensagem: "AWS Community Day: confira a programação completa.", dataCriacao: hoursAgo(1), lida: false, eventoId: null, tipo: "GERAL" },
];
