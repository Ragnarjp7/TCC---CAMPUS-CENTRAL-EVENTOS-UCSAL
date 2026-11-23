/**
 * Modelo, opções e validação do formulário "Proposta de evento" (CAEX/UCSal).
 *
 * RF27: os campos centrais (título, apresentação, local, datas/horários, vagas, categoria, liga)
 * viram as colunas do evento; o formulário completo segue em JSON no campo `proposta`
 * para a análise do CAEX (RF29) e para reabrir o formulário quando o CAEX pede ajustes.
 */
import type { EventoRequestDTO, EventSubmission } from "./types";

export type YesNo = "sim" | "nao";

export const AREAS_TEMATICAS = [
  "Comunicação, Cultura",
  "Educação",
  "Direitos Humanos e Justiça",
  "Meio Ambiente",
  "Saúde",
  "Tecnologia e Produção",
] as const;

export const ABRANGENCIAS = ["Local", "Regional", "Nacional", "Internacional"] as const;

export const TIPOS_EXTENSAO = [
  "Iniciação",
  "Atualização",
  "Formação, treinamento e qualificação profissional",
  "Aperfeiçoamento (mín. 180h, para graduados)",
] as const;

export const LOCAIS = ["Campus da UCSAL - Pituaçu", "Online", "Outro"] as const;

export const ESPACOS = ["Sala de Aula", "Laboratório (especificar)", "Auditório (mín. 80 pessoas)", "Outro"] as const;

export const VAGAS = [
  "Até 15 participantes",
  "Até 30 participantes",
  "Até 50 participantes",
  "Até 80 participantes",
  "Até 160 participantes",
  "Acima de 180 participantes",
] as const;

export const RECURSOS = ["Projetor", "Computador", "Caixa de Som", "Microfone", "Outro"] as const;

export const INVESTIMENTOS = ["Gratuito", "Pago"] as const;

export const APRESENTACAO_MAX = 1000;

/** Limites das colunas do evento no backend (tabela "eventos"). */
export const TITULO_MAX = 150;
const LOCAL_MAX = 150;

export interface EventProposal {
  // 1. Proponente
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  lattes: string;
  miniCurriculo: string;
  isEntidade: YesNo | null;
  /** Liga responsável (obrigatória para estudantes; professores podem deixar vazio). */
  ligaId: number | null;
  entidade: string;
  presidenteNome: string;
  presidenteEmail: string;
  presidenteTelefone: string;
  presidenteCpf: string;
  // 2. Evento
  titulo: string;
  categoriaId: number | null;
  caracterizacao: string;
  area: string | null;
  abrangencia: string | null;
  tipoExtensao: string | null;
  apresentacao: string;
  publicarSite: YesNo | null;
  objetivoGeral: string;
  objetivosEspecificos: string;
  publicoAlvo: string;
  local: string | null;
  localOutro: string;
  espaco: string | null;
  espacoDetalhe: string;
  quantidadeEspacos: string;
  vagas: string | null;
  recursos: string[];
  recursosOutro: string;
  inicio: string;
  horaInicio: string;
  termino: string;
  horaTermino: string;
  diasHorarios: string;
  programacao: string;
  // 3. Certificação
  certificado: YesNo | null;
  cienteCertificado: boolean;
  outrosPapeis: string;
  // 4. Investimento
  investimento: string | null;
  valor: string;
  linkInscricao: string;
  // 5–7
  linkPecas: string;
  observacoes: string;
  lgpd: boolean;
}

export type ProposalField = keyof EventProposal;
export type ProposalErrors = Partial<Record<ProposalField, string>>;

export const emptyProposal = (prefill: Partial<EventProposal> = {}): EventProposal => ({
  nome: "",
  cpf: "",
  email: "",
  telefone: "",
  lattes: "",
  miniCurriculo: "",
  isEntidade: null,
  ligaId: null,
  entidade: "",
  presidenteNome: "",
  presidenteEmail: "",
  presidenteTelefone: "",
  presidenteCpf: "",
  titulo: "",
  categoriaId: null,
  caracterizacao: "",
  area: null,
  abrangencia: null,
  tipoExtensao: null,
  apresentacao: "",
  publicarSite: null,
  objetivoGeral: "",
  objetivosEspecificos: "",
  publicoAlvo: "",
  local: null,
  localOutro: "",
  espaco: null,
  espacoDetalhe: "",
  quantidadeEspacos: "",
  vagas: null,
  recursos: [],
  recursosOutro: "",
  inicio: "",
  horaInicio: "",
  termino: "",
  horaTermino: "",
  diasHorarios: "",
  programacao: "",
  certificado: null,
  cienteCertificado: false,
  outrosPapeis: "",
  investimento: null,
  valor: "",
  linkInscricao: "",
  linkPecas: "",
  observacoes: "",
  lgpd: false,
  ...prefill,
});

// ---------- Máscaras ----------

const digits = (v: string, max: number) => v.replace(/\D/g, "").slice(0, max);

/** dd/mm/aaaa */
export const maskDate = (v: string) => {
  const d = digits(v, 8);
  return [d.slice(0, 2), d.slice(2, 4), d.slice(4, 8)].filter(Boolean).join("/");
};

/** hh:mm */
export const maskTime = (v: string) => {
  const d = digits(v, 4);
  return d.length > 2 ? `${d.slice(0, 2)}:${d.slice(2)}` : d;
};

/** 000.000.000-00 */
export const maskCpf = (v: string) => {
  const d = digits(v, 11);
  let out = d.slice(0, 3);
  if (d.length > 3) out += `.${d.slice(3, 6)}`;
  if (d.length > 6) out += `.${d.slice(6, 9)}`;
  if (d.length > 9) out += `-${d.slice(9, 11)}`;
  return out;
};

/** (00) 00000-0000 ou (00) 0000-0000 */
export const maskPhone = (v: string) => {
  const d = digits(v, 11);
  if (d.length <= 2) return d.length ? `(${d}` : "";
  const split = d.length === 11 ? 7 : 6;
  const rest = d.length > split ? `${d.slice(2, split)}-${d.slice(split)}` : d.slice(2);
  return `(${d.slice(0, 2)}) ${rest}`;
};

/** R$ 0,00 a partir dos dígitos digitados (centavos). */
export const maskCurrency = (v: string) => {
  const d = digits(v, 9);
  if (!d) return "";
  const cents = Number(d);
  return `R$ ${(cents / 100).toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

// ---------- Datas e horários ----------

/** Converte dd/mm/aaaa em Date, ou null se for inválida. */
export const parseDate = (v: string): Date | null => {
  const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(v);
  if (!m) return null;
  const [day, month, year] = [Number(m[1]), Number(m[2]), Number(m[3])];
  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day ? date : null;
};

/** Converte hh:mm em minutos desde 00:00, ou null se for inválido. */
export const parseTime = (v: string): number | null => {
  const m = /^(\d{2}):(\d{2})$/.exec(v);
  if (!m) return null;
  const [h, min] = [Number(m[1]), Number(m[2])];
  return h <= 23 && min <= 59 ? h * 60 + min : null;
};

/** dd/mm/aaaa + hh:mm → Date local, ou null. */
const toDateTime = (date: string, time: string): Date | null => {
  const d = parseDate(date);
  const t = parseTime(time);
  if (!d || t === null) return null;
  d.setHours(Math.floor(t / 60), t % 60, 0, 0);
  return d;
};

/** dd/mm/aaaa + hh:mm → "aaaa-mm-ddThh:mm:00" (LocalDateTime do backend, sem fuso). */
const toLocalIso = (date: string, time: string) => {
  const [dd, mm, yyyy] = date.split("/");
  return `${yyyy}-${mm}-${dd}T${time}:00`;
};

/** "aaaa-mm-ddThh:mm..." → { date: "dd/mm/aaaa", time: "hh:mm" } */
export const splitLocalIso = (iso: string) => {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(iso);
  return m ? { date: `${m[3]}/${m[2]}/${m[1]}`, time: `${m[4]}:${m[5]}` } : { date: "", time: "" };
};

// ---------- Validação ----------

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const URL_RE = /^(https?:\/\/)?[^\s.]+\.[^\s]{2,}$/i;

const isValidCpf = (v: string) => {
  const d = v.replace(/\D/g, "");
  if (d.length !== 11 || /^(\d)\1{10}$/.test(d)) return false;
  const check = (len: number) => {
    let sum = 0;
    for (let i = 0; i < len; i++) sum += Number(d[i]) * (len + 1 - i);
    const r = (sum * 10) % 11;
    return (r === 10 ? 0 : r) === Number(d[len]);
  };
  return check(9) && check(10);
};

const REQUIRED = "Campo obrigatório";

export interface ProposalRules {
  /** Estudantes só criam eventos pela liga (RF27); professores e coordenadores podem criar sem liga. */
  requiresLeague: boolean;
  /** Referência para "data futura"; injetável para testes. */
  now?: Date;
}

export function validateProposal(p: EventProposal, { requiresLeague, now = new Date() }: ProposalRules): ProposalErrors {
  const e: ProposalErrors = {};
  const req = (field: ProposalField, ok: boolean, msg = REQUIRED) => {
    if (!ok && !e[field]) e[field] = msg;
  };
  const filled = (v: string) => v.trim() !== "";

  // 1. Proponente
  req("nome", filled(p.nome));
  req("cpf", filled(p.cpf));
  req("cpf", isValidCpf(p.cpf), "CPF inválido");
  req("email", filled(p.email));
  req("email", EMAIL_RE.test(p.email.trim()), "E-mail inválido");
  req("telefone", filled(p.telefone));
  req("telefone", p.telefone.replace(/\D/g, "").length >= 10, "Telefone inválido");
  req("lattes", filled(p.lattes));
  req("lattes", URL_RE.test(p.lattes.trim()), "Link inválido");
  req("miniCurriculo", filled(p.miniCurriculo));
  req("isEntidade", p.isEntidade !== null, "Selecione uma opção");
  if (requiresLeague) req("ligaId", p.ligaId !== null, "Selecione a liga responsável pelo evento");
  if (p.isEntidade === "sim") {
    req("entidade", filled(p.entidade), "Informe a entidade estudantil");
    if (filled(p.presidenteEmail)) req("presidenteEmail", EMAIL_RE.test(p.presidenteEmail.trim()), "E-mail inválido");
    if (filled(p.presidenteCpf)) req("presidenteCpf", isValidCpf(p.presidenteCpf), "CPF inválido");
  }

  // 2. Evento
  req("titulo", filled(p.titulo));
  req("titulo", p.titulo.trim().length <= TITULO_MAX, `Use no máximo ${TITULO_MAX} caracteres`);
  req("categoriaId", p.categoriaId !== null, "Selecione a categoria");
  req("caracterizacao", filled(p.caracterizacao));
  req("area", p.area !== null, "Selecione uma área");
  req("abrangencia", p.abrangencia !== null, "Selecione a abrangência");
  req("tipoExtensao", p.tipoExtensao !== null, "Selecione o tipo");
  req("apresentacao", filled(p.apresentacao));
  req("publicarSite", p.publicarSite !== null, "Selecione uma opção");
  req("objetivoGeral", filled(p.objetivoGeral));
  req("objetivosEspecificos", filled(p.objetivosEspecificos));
  req("publicoAlvo", filled(p.publicoAlvo));
  req("local", p.local !== null, "Selecione o local");
  if (p.local === "Outro") req("localOutro", filled(p.localOutro), "Especifique o local");
  req("espaco", p.espaco !== null, "Selecione o espaço");
  if (p.espaco === "Laboratório (especificar)" || p.espaco === "Outro")
    req("espacoDetalhe", filled(p.espacoDetalhe), "Especifique o espaço");
  req("vagas", p.vagas !== null, "Selecione a quantidade de vagas");
  req("recursos", p.recursos.length > 0, "Selecione ao menos um recurso");
  if (p.recursos.includes("Outro")) req("recursosOutro", filled(p.recursosOutro), "Especifique o recurso");

  req("inicio", filled(p.inicio));
  req("inicio", parseDate(p.inicio) !== null, "Data inválida");
  req("horaInicio", filled(p.horaInicio));
  req("horaInicio", parseTime(p.horaInicio) !== null, "Horário inválido");
  req("termino", filled(p.termino));
  req("termino", parseDate(p.termino) !== null, "Data inválida");
  req("horaTermino", filled(p.horaTermino));
  req("horaTermino", parseTime(p.horaTermino) !== null, "Horário inválido");

  const start = toDateTime(p.inicio, p.horaInicio);
  const end = toDateTime(p.termino, p.horaTermino);
  if (start) req("inicio", start > now, "O início precisa ser uma data futura");
  if (start && end) req("horaTermino", end > start, "O término precisa ser depois do início");

  req("diasHorarios", filled(p.diasHorarios));
  req("programacao", filled(p.programacao));

  // 3. Certificação
  req("certificado", p.certificado !== null, "Selecione uma opção");
  req("cienteCertificado", p.cienteCertificado, "Confirme a ciência");

  // 4. Investimento
  req("investimento", p.investimento !== null, "Selecione uma opção");
  if (p.investimento === "Pago") req("valor", p.valor.replace(/\D/g, "").replace(/^0+/, "") !== "", "Informe o valor");
  req("linkInscricao", filled(p.linkInscricao));
  req("linkInscricao", URL_RE.test(p.linkInscricao.trim()), "Link inválido");

  // 5. Peças (opcional, mas se preenchido precisa ser link)
  if (filled(p.linkPecas)) req("linkPecas", URL_RE.test(p.linkPecas.trim()), "Link inválido");

  // 7. LGPD
  req("lgpd", p.lgpd, "É necessário concordar com a LGPD");

  return e;
}

// ---------- Conversão para a API (RF27) ----------

/** "Até 50 participantes" → 50; "Acima de ..." → sem limite (null). */
export const capacityFromVagas = (vagas: string | null): number | null => {
  const m = vagas ? /^Até (\d+)/.exec(vagas) : null;
  return m ? Number(m[1]) : null;
};

const stripParenthetical = (v: string) => v.replace(/\s*\(.*\)\s*$/, "");

/** Local exibido no evento: local + espaço preferencial (ex.: "Campus da UCSAL - Pituaçu · Auditório"). */
export function buildPlace(p: EventProposal): string {
  if (p.local === "Online") return "Online";
  const base = p.local === "Outro" ? p.localOutro.trim() : (p.local ?? "");
  const space =
    p.espaco === "Laboratório (especificar)" || p.espaco === "Outro"
      ? p.espacoDetalhe.trim()
      : p.espaco
        ? stripParenthetical(p.espaco)
        : "";
  const place = space ? `${base} · ${space}` : base;
  return place.length > LOCAL_MAX ? `${place.slice(0, LOCAL_MAX - 1)}…` : place;
}

/** Versão do JSON salvo em `proposta`, para conseguir migrar o formato no futuro. */
const PROPOSAL_VERSION = 1;

/** Corpo de POST /eventos e PUT /eventos/{id}. Supõe uma proposta já validada. */
export function toEventoRequest(p: EventProposal): EventoRequestDTO {
  return {
    titulo: p.titulo.trim(),
    descricao: p.apresentacao.trim(),
    local: buildPlace(p),
    dataInicio: toLocalIso(p.inicio, p.horaInicio),
    dataFim: toLocalIso(p.termino, p.horaTermino),
    modalidade: p.local === "Online" ? "online" : "presencial",
    capacidade: capacityFromVagas(p.vagas),
    categoriaId: p.categoriaId as number,
    ligaId: p.ligaId,
    proposta: JSON.stringify({ versao: PROPOSAL_VERSION, ...p }),
  };
}

/**
 * Lê o JSON salvo em `proposta`. Só aproveita os campos conhecidos e com o tipo certo,
 * completando o restante com os valores vazios. Retorna null se não houver proposta legível.
 */
export function parseStoredProposal(json: string | null): EventProposal | null {
  if (!json) return null;
  let data: unknown;
  try {
    data = JSON.parse(json);
  } catch {
    return null;
  }
  if (!data || typeof data !== "object") return null;

  const base = emptyProposal();
  const raw = data as Record<string, unknown>;
  const out: Record<string, unknown> = { ...base };
  for (const key of Object.keys(base) as ProposalField[]) {
    const value = raw[key];
    const expected = base[key];
    if (value === undefined) continue;
    if (Array.isArray(expected)) {
      if (Array.isArray(value)) out[key] = value.filter((v) => typeof v === "string");
    } else if (expected === null) {
      // campos opcionais (null até escolher): aceita string ou número
      if (value === null || typeof value === "string" || typeof value === "number") out[key] = value;
    } else if (typeof value === typeof expected) {
      out[key] = value;
    }
  }
  return out as unknown as EventProposal;
}

/**
 * Formulário para reenviar um evento após ajustes: a proposta salva, com os dados centrais
 * do evento por cima (são o que vale no backend). Serve também para eventos sem proposta salva.
 */
export function proposalFromSubmission(s: EventSubmission, prefill: Partial<EventProposal> = {}): EventProposal {
  const stored = parseStoredProposal(s.proposal);
  const start = splitLocalIso(s.startsAt);
  const end = splitLocalIso(s.endsAt);
  return {
    ...(stored ?? emptyProposal(prefill)),
    titulo: s.title,
    apresentacao: s.description,
    categoriaId: s.categoryId,
    ligaId: s.leagueId,
    inicio: start.date,
    horaInicio: start.time,
    termino: end.date,
    horaTermino: end.time,
    // a ciência da LGPD é confirmada de novo a cada envio
    lgpd: false,
  };
}

// ---------- Resumo para a análise do CAEX (RF29) ----------

export interface ProposalSection {
  title: string;
  rows: { label: string; value: string }[];
}

const yesNo = (v: YesNo | null) => (v === "sim" ? "Sim" : v === "nao" ? "Não" : "");

/** Proposta organizada nas seções do formulário, só com os campos preenchidos. */
export function describeProposal(p: EventProposal): ProposalSection[] {
  const sections: ProposalSection[] = [
    {
      title: "Proponente",
      rows: [
        { label: "Nome", value: p.nome },
        { label: "CPF", value: p.cpf },
        { label: "E-mail", value: p.email },
        { label: "Telefone", value: p.telefone },
        { label: "Lattes", value: p.lattes },
        { label: "Mini currículo", value: p.miniCurriculo },
        { label: "Evento de entidade estudantil", value: yesNo(p.isEntidade) },
        { label: "Entidade", value: p.isEntidade === "sim" ? p.entidade : "" },
        { label: "Presidente da entidade", value: p.presidenteNome },
        { label: "E-mail do presidente", value: p.presidenteEmail },
        { label: "Telefone do presidente", value: p.presidenteTelefone },
        { label: "CPF do presidente", value: p.presidenteCpf },
      ],
    },
    {
      title: "Evento",
      rows: [
        { label: "Caracterização", value: p.caracterizacao },
        { label: "Área temática", value: p.area ?? "" },
        { label: "Abrangência", value: p.abrangencia ?? "" },
        { label: "Tipo de extensão", value: p.tipoExtensao ?? "" },
        { label: "Publicar texto no site", value: yesNo(p.publicarSite) },
        { label: "Objetivo geral", value: p.objetivoGeral },
        { label: "Objetivos específicos", value: p.objetivosEspecificos },
        { label: "Público-alvo", value: p.publicoAlvo },
        { label: "Espaço", value: p.espaco ?? "" },
        { label: "Detalhe do espaço", value: p.espacoDetalhe },
        { label: "Quantidade de espaços", value: p.quantidadeEspacos },
        { label: "Vagas", value: p.vagas ?? "" },
        {
          label: "Recursos",
          value: p.recursos.map((r) => (r === "Outro" && p.recursosOutro.trim() ? p.recursosOutro.trim() : r)).join(", "),
        },
        { label: "Dias e horários", value: p.diasHorarios },
        { label: "Programação", value: p.programacao },
      ],
    },
    {
      title: "Certificação",
      rows: [
        { label: "Emite certificado", value: yesNo(p.certificado) },
        { label: "Outros papéis certificados", value: p.outrosPapeis },
      ],
    },
    {
      title: "Investimento e divulgação",
      rows: [
        { label: "O evento será", value: p.investimento ?? "" },
        { label: "Valor", value: p.investimento === "Pago" ? p.valor : "" },
        { label: "Link de inscrição", value: p.linkInscricao },
        { label: "Peças de divulgação", value: p.linkPecas },
      ],
    },
    { title: "Observações", rows: [{ label: "Observações", value: p.observacoes }] },
  ];

  return sections
    .map((s) => ({ ...s, rows: s.rows.filter((r) => r.value.trim() !== "") }))
    .filter((s) => s.rows.length > 0);
}
