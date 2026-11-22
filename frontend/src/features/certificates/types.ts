/** Certificado de participação em um evento. Ainda não existe no backend. */
export interface Certificate {
  id: number;
  /** Nome do certificado (normalmente o título do evento). */
  nome: string;
  eventoId: number | null;
  /** Data em que o evento aconteceu (ISO 8601). */
  dataEvento: string;
  /** Carga horária em horas. */
  cargaHoraria: number;
}
