package br.edu.ucsal.campus.model;

import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "eventos")
public class Evento {

    // ===================== ENUMS =====================

    public enum StatusEvento {
        AGUARDANDO_ANALISE, APROVADO, AJUSTES_SOLICITADOS, REJEITADO, CANCELADO, ENCERRADO
    }

    public enum AreaTematica {
        COMUNICACAO_CULTURA, EDUCACAO, DIREITOS_HUMANOS_JUSTICA, MEIO_AMBIENTE, SAUDE, TECNOLOGIA_PRODUCAO
    }

    public enum Abrangencia {
        LOCAL, REGIONAL, NACIONAL, INTERNACIONAL
    }

    public enum TipoExtensao {
        INICIACAO, ATUALIZACAO, FORMACAO_TREINAMENTO_QUALIFICACAO, APERFEICOAMENTO
    }

    public enum TipoLocal {
        CAMPUS_PITUACU, ONLINE, OUTRO
    }

    public enum EspacoRealizacao {
        SALA_AULA, LABORATORIO, AUDITORIO, OUTRO
    }

    public enum FaixaVagas {
        ATE_15(15),
        ATE_30(30),
        ATE_50(50),
        ATE_80(80),
        ATE_160(160),
        ACIMA_DE_180(null); // sem limite definido

        private final Integer limite;

        FaixaVagas(Integer limite) {
            this.limite = limite;
        }

        // Máximo de inscrições; null = sem limite
        public Integer getLimite() {
            return limite;
        }
    }

    // Atalho para o service: limite de vagas do evento (null = sem limite)
    public Integer getLimiteVagas() {
        return faixaVagas != null ? faixaVagas.getLimite() : null;
    }

    public enum RecursoAudiovisual {
        PROJETOR, COMPUTADOR, CAIXA_DE_SOM, MICROFONE, OUTRO
    }

    public enum TipoInvestimento {
        GRATUITO, PAGO
    }

    // ===================== CONTROLE =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEvento status = StatusEvento.AGUARDANDO_ANALISE;

    @Column(name = "observacao_caex", length = 1000)
    private String observacaoCaex;

    // ===================== PROPONENTE =====================

    @Column(name = "proponente_nome", nullable = false, length = 150)
    private String proponenteNome;

    @Column(name = "proponente_cpf", nullable = false, length = 14)
    private String proponenteCpf;

    @Column(name = "proponente_email", nullable = false, length = 150)
    private String proponenteEmail;

    @Column(name = "proponente_telefone", nullable = false, length = 20)
    private String proponenteTelefone;

    // Opcional
    @Column(name = "proponente_lattes", length = 300)
    private String proponenteLattes;

    @Column(name = "proponente_mini_curriculo", nullable = false, length = 2000)
    private String proponenteMiniCurriculo;

    // Marcado automaticamente quando o evento é criado por uma entidade (liga etc.)
    @Column(name = "evento_entidade_estudantil", nullable = false)
    private boolean eventoEntidadeEstudantil = false;

    // Dados do presidente são obtidos a partir da liga, não ficam duplicados aqui
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liga_id")
    private Liga liga;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    // Formulário completo da proposta (JSON montado pelo app) para análise do CAEX.
    // Não é exposto nas consultas públicas.
    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String proposta;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    // Última vez que o evento foi enviado (ou reenviado) para análise
    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    public Evento() {}

    @PrePersist
    private void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (enviadoEm == null) {
            enviadoEm = criadoEm;
        }
    }

    @Column(name = "caracterizacao_atividade", nullable = false, length = 4000)
    private String caracterizacaoAtividade;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_tematica", length = 40)
    private AreaTematica areaTematica;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Abrangencia abrangencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_extensao", length = 50)
    private TipoExtensao tipoExtensao;

    // Breve descrição (até 10 linhas)
    @Column(nullable = false, length = 1500)
    private String apresentacao;

    @Column(name = "publicar_no_campus", nullable = false)
    private boolean publicarNoCampus;

    @Column(name = "objetivo_geral", nullable = false, length = 2000)
    private String objetivoGeral;

    @Column(name = "objetivos_especificos", nullable = false, length = 3000)
    private String objetivosEspecificos;

    @Column(name = "publico_alvo", nullable = false, length = 1000)
    private String publicoAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_local", nullable = false, length = 30)
    private TipoLocal tipoLocal;

    // Detalhe quando tipoLocal = OUTRO (ou link se ONLINE)
    @Column(name = "local_detalhe", length = 300)
    private String localDetalhe;

    @Enumerated(EnumType.STRING)
    @Column(name = "espaco_realizacao", nullable = false, length = 30)
    private EspacoRealizacao espacoRealizacao;

    @Column(name = "especificacao_espaco", nullable = false, length = 1000)
    private String especificacaoEspaco;

    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_vagas", nullable = false, length = 30)
    private FaixaVagas faixaVagas;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "evento_recursos", joinColumns = @JoinColumn(name = "evento_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "recurso", length = 30)
    private Set<RecursoAudiovisual> recursos = new HashSet<>();

    @Column(name = "recursos_outro", length = 300)
    private String recursosOutro;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    // Ex.: "Dia 23/02 das 08h às 12h"
    @Column(name = "dias_horarios", nullable = false, length = 1000)
    private String diasHorarios;

    @Column(nullable = false, length = 4000)
    private String programacao;

    // ===================== CERTIFICAÇÃO =====================

    @Column(name = "emite_certificado", nullable = false)
    private boolean emiteCertificado;

    // ===================== INVESTIMENTO =====================

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_investimento", nullable = false, length = 20)
    private TipoInvestimento tipoInvestimento = TipoInvestimento.GRATUITO;

    // Obrigatório apenas se PAGO (validar no service)
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    // ===================== DIVULGAÇÃO E OBSERVAÇÕES =====================

    @Column(name = "link_pecas_publicitarias", length = 500)
    private String linkPecasPublicitarias;

    @Column(length = 2000)
    private String observacoes;

    // ===================== 7. LGPD =====================

    @Column(name = "autorizacao_lgpd", nullable = false)
    private boolean autorizacaoLgpd;

    // Campos de compatibilidade com os fluxos anteriores de eventos
    @Column(name = "titulo", length = 150)
    private String titulo;

    @Column(name = "descricao", length = 2000)
    private String descricao;

    @Column(name = "local", length = 300)
    private String local;

    @Column(name = "modalidade", length = 50)
    private String modalidade;

    @Column(name = "capacidade")
    private Integer capacidade;

    // ===================== CATEGORIA (legado) =====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // ===================== GETTERS / SETTERS =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StatusEvento getStatus() { return status; }
    public void setStatus(StatusEvento status) { this.status = status; }
    public String getObservacaoCaex() { return observacaoCaex; }
    public void setObservacaoCaex(String value) { this.observacaoCaex = value; }

    public String getProponenteNome() { return proponenteNome; }
    public void setProponenteNome(String value) { this.proponenteNome = value; }
    public String getProponenteCpf() { return proponenteCpf; }
    public void setProponenteCpf(String value) { this.proponenteCpf = value; }
    public String getProponenteEmail() { return proponenteEmail; }
    public void setProponenteEmail(String value) { this.proponenteEmail = value; }
    public String getProponenteTelefone() { return proponenteTelefone; }
    public void setProponenteTelefone(String value) { this.proponenteTelefone = value; }
    public String getProponenteLattes() { return proponenteLattes; }
    public void setProponenteLattes(String value) { this.proponenteLattes = value; }
    public String getProponenteMiniCurriculo() { return proponenteMiniCurriculo; }
    public void setProponenteMiniCurriculo(String value) { this.proponenteMiniCurriculo = value; }
    public boolean isEventoEntidadeEstudantil() { return eventoEntidadeEstudantil; }
    public void setEventoEntidadeEstudantil(boolean value) { this.eventoEntidadeEstudantil = value; }
    public Liga getLiga() { return liga; }
    public void setLiga(Liga value) { this.liga = value; }
    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario value) { this.criadoPor = value; }
    public String getProposta() { return proposta; }
    public void setProposta(String value) { this.proposta = value; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public void setEnviadoEm(LocalDateTime value) { this.enviadoEm = value; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String value) { this.titulo = value; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String value) { this.descricao = value; }
    public String getLocal() { return local; }
    public void setLocal(String value) { this.local = value; }
    public String getModalidade() { return modalidade; }
    public void setModalidade(String value) { this.modalidade = value; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer value) { this.capacidade = value; }

    public String getCaracterizacaoAtividade() { return caracterizacaoAtividade; }
    public void setCaracterizacaoAtividade(String value) { this.caracterizacaoAtividade = value; }
    public AreaTematica getAreaTematica() { return areaTematica; }
    public void setAreaTematica(AreaTematica value) { this.areaTematica = value; }
    public Abrangencia getAbrangencia() { return abrangencia; }
    public void setAbrangencia(Abrangencia value) { this.abrangencia = value; }
    public TipoExtensao getTipoExtensao() { return tipoExtensao; }
    public void setTipoExtensao(TipoExtensao value) { this.tipoExtensao = value; }
    public String getApresentacao() { return apresentacao; }
    public void setApresentacao(String value) { this.apresentacao = value; }
    public boolean isPublicarNoCampus() { return publicarNoCampus; }
    public void setPublicarNoCampus(boolean value) { this.publicarNoCampus = value; }
    public String getObjetivoGeral() { return objetivoGeral; }
    public void setObjetivoGeral(String value) { this.objetivoGeral = value; }
    public String getObjetivosEspecificos() { return objetivosEspecificos; }
    public void setObjetivosEspecificos(String value) { this.objetivosEspecificos = value; }
    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String value) { this.publicoAlvo = value; }
    public TipoLocal getTipoLocal() { return tipoLocal; }
    public void setTipoLocal(TipoLocal value) { this.tipoLocal = value; }
    public String getLocalDetalhe() { return localDetalhe; }
    public void setLocalDetalhe(String value) { this.localDetalhe = value; }
    public EspacoRealizacao getEspacoRealizacao() { return espacoRealizacao; }
    public void setEspacoRealizacao(EspacoRealizacao value) { this.espacoRealizacao = value; }
    public String getEspecificacaoEspaco() { return especificacaoEspaco; }
    public void setEspecificacaoEspaco(String value) { this.especificacaoEspaco = value; }
    public FaixaVagas getFaixaVagas() { return faixaVagas; }
    public void setFaixaVagas(FaixaVagas value) { this.faixaVagas = value; }
    public Set<RecursoAudiovisual> getRecursos() { return recursos; }
    public void setRecursos(Set<RecursoAudiovisual> value) { this.recursos = value == null ? new HashSet<>() : new HashSet<>(value); }
    public String getRecursosOutro() { return recursosOutro; }
    public void setRecursosOutro(String value) { this.recursosOutro = value; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate value) { this.dataInicio = value; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate value) { this.dataFim = value; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime value) { this.horaInicio = value; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime value) { this.horaFim = value; }
    public String getDiasHorarios() { return diasHorarios; }
    public void setDiasHorarios(String value) { this.diasHorarios = value; }
    public String getProgramacao() { return programacao; }
    public void setProgramacao(String value) { this.programacao = value; }
    public boolean isEmiteCertificado() { return emiteCertificado; }
    public void setEmiteCertificado(boolean value) { this.emiteCertificado = value; }
    public TipoInvestimento getTipoInvestimento() { return tipoInvestimento; }
    public void setTipoInvestimento(TipoInvestimento value) { this.tipoInvestimento = value; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal value) { this.valor = value; }
    public String getLinkPecasPublicitarias() { return linkPecasPublicitarias; }
    public void setLinkPecasPublicitarias(String value) { this.linkPecasPublicitarias = value; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String value) { this.observacoes = value; }
    public boolean isAutorizacaoLgpd() { return autorizacaoLgpd; }
    public void setAutorizacaoLgpd(boolean value) { this.autorizacaoLgpd = value; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria value) { this.categoria = value; }
}
