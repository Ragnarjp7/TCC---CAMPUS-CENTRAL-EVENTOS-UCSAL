import type { CampusEvent, EventCategory } from "../types";

export const EVENTS: CampusEvent[] = [
  {
    id: 1, day: "22", month: "NOV", title: "Semana Acadêmica UCSAL", place: "Auditório Principal", mode: "Presenciais", organizer: "Liga de Computação", time: "19h às 22h",
    description: "Uma semana repleta de palestras, workshops e atividades para toda a comunidade acadêmica da UCSAL. Participe, troque experiências e garanta horas complementares.",
    capacity: 300, enrolled: 214,
    kind: "Semana acadêmica", complementaryHours: 20, certificate: true, price: 0, audience: "Todos os cursos",
    speakers: [
      { name: "Profa. Marina Souza", role: "Coordenação de Computação" },
      { name: "Rafael Lima", role: "Engenheiro de Software" },
    ],
  },
  {
    id: 2, day: "14", month: "NOV", title: "Oficina de Python", place: "Lab. de Informática", mode: "Presenciais", organizer: "Liga de Computação", time: "14h às 17h",
    description: "Oficina prática para quem está começando: variáveis, estruturas de controle, funções e um mini projeto no final. Traga seu notebook se puder.",
    capacity: 30, enrolled: 30,
    kind: "Oficina", complementaryHours: 3, certificate: true, price: 0, audience: "Iniciantes em programação",
    requirements: "Notebook com Python 3 instalado (opcional).",
    speakers: [{ name: "Lucas Andrade", role: "Monitor de Algoritmos" }],
  },
  {
    id: 3, day: "25", month: "NOV", title: "Encontro de Gestão", place: "Sala 204 - Bloco B", mode: "Presenciais", organizer: "Atlética de Gestão", time: "18h às 20h",
    description: "Bate-papo com egressos e profissionais do mercado sobre carreira, empreendedorismo e liderança.",
    capacity: 60, enrolled: 18,
    kind: "Encontro", certificate: false, price: 10, audience: "Alunos de Administração e áreas afins",
  },
  {
    id: 4, day: "05", month: "DEZ", title: "Workshop de UX", place: "Google Meet", mode: "Online", organizer: "Liga de Design", time: "19h às 21h",
    description: "Aprenda os fundamentos de pesquisa com usuários, prototipação e testes de usabilidade. O link da sala é liberado 15 minutos antes do início.",
    kind: "Workshop", complementaryHours: 2, certificate: true, price: 0, audience: "Todos os cursos",
    requirements: "Conta no Figma (gratuita).",
    speakers: [{ name: "Beatriz Nunes", role: "Product Designer" }],
  },
  {
    id: 5, day: "29", month: "NOV", title: "AWS Community Day", place: "Auditório Principal", mode: "Presenciais", organizer: "Aws Community", time: "9h às 17h",
    description: "Participe do AWS Community Day: palestras, workshops e networking sobre computação em nuvem, com a comunidade AWS Salvador e convidados.",
    capacity: 250, enrolled: 132,
    kind: "Palestra", complementaryHours: 8, certificate: true, price: 0, audience: "Todos os cursos",
    requirements: "Documento com foto para o credenciamento.",
    speakers: [
      { name: "Camila Rocha", role: "AWS Community Builder" },
      { name: "Diego Santana", role: "Arquiteto de Soluções Cloud" },
    ],
  },
  {
    id: 6, day: "02", month: "DEZ", title: "Processo seletivo UCSAL Júnior", place: "Sala 108 - Bloco A", mode: "Presenciais", organizer: "UCSAL Júnior", time: "14h às 18h",
    description: "Dinâmica de grupo e entrevistas para novos membros da empresa júnior. Conheça os projetos reais com clientes e as áreas disponíveis.",
    capacity: 40, enrolled: 27,
    kind: "Encontro", certificate: false, price: 0, audience: "Alunos a partir do 2º semestre",
    requirements: "Currículo impresso ou em PDF.",
  },
  {
    id: 7, day: "28", month: "NOV", title: "Seletiva de futsal", place: "Quadra Poliesportiva", mode: "Presenciais", organizer: "Atlética UCSAL", time: "16h às 19h",
    description: "Seletiva aberta para as equipes de futsal masculino e feminino da Atlética.",
    capacity: 60, enrolled: 41,
    kind: "Encontro", certificate: false, price: 0, audience: "Todos os cursos",
    requirements: "Tênis, camisa leve e garrafa de água.",
  },
  {
    id: 8, day: "12", month: "DEZ", title: "Sarau de fim de ano", place: "Centro Cultural UCSAL", mode: "Presenciais", organizer: "Centro Cultural UCSAL", time: "18h às 22h",
    description: "Música, poesia e artes visuais produzidas por alunos. Entrada gratuita e aberta à comunidade.",
    kind: "Encontro", complementaryHours: 4, certificate: true, price: 0, audience: "Comunidade acadêmica e externa",
  },
];

/** Categorias do modo mock: os tipos de atividade presentes em EVENTS. */
export const EVENT_CATEGORIES: EventCategory[] = [...new Set(EVENTS.map((e) => e.kind).filter((k) => k != null))].map(
  (name, i) => ({ id: i + 1, name }),
);
