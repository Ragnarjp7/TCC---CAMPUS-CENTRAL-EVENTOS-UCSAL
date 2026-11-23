package br.edu.ucsal.campus.model;

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
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    public void setId(Long id) { this.id = id;}

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

    // ===================== CATEGORIA (legado) =====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public Evento() {}

    // ===================== GETTERS / SETTERS =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusEvento getStatus() { return status; }
    public void setStatus(StatusEvento status) { this.status = status; }

    public String getObservacaoCaex() { return observacaoCaex; }
    public void setObservacaoCaex(String observacaoCaex) { this.observacaoCaex = observacaoCaex; }

    public String getProponenteNome() { return proponenteNome; }
    public void setProponenteNome(String proponenteNome) { this.proponenteNome = proponenteNome; }

    public String getProponenteCpf() { return proponenteCpf; }
    public void setProponenteCpf(String proponenteCpf) { this.proponenteCpf = proponenteCpf; }

    public String getProponenteEmail() { return proponenteEmail; }
    public void setProponenteEmail(String proponenteEmail) { this.proponenteEmail = proponenteEmail; }

    public String getProponenteTelefone() { return proponenteTelefone; }
    public void setProponenteTelefone(String proponenteTelefone) { this.proponenteTelefone = proponenteTelefone; }

    public String getProponenteLattes() { return proponenteLattes; }
    public void setProponenteLattes(String proponenteLattes) { this.proponenteLattes = proponenteLattes; }

    public String getProponenteMiniCurriculo() { return proponenteMiniCurriculo; }
    public void setProponenteMiniCurriculo(String proponenteMiniCurriculo) { this.proponenteMiniCurriculo = proponenteMiniCurriculo; }

    public boolean isEventoEntidadeEstudantil() { return eventoEntidadeEstudantil; }
    public void setEventoEntidadeEstudantil(boolean eventoEntidadeEstudantil) { this.eventoEntidadeEstudantil = eventoEntidadeEstudantil; }

    public Liga getLiga() { return liga; }
    public void setLiga(Liga liga) { this.liga = liga; }

    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }

    public String getProposta() { return proposta; }

    public void setProposta(String proposta) { this.proposta = proposta; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public LocalDateTime getEnviadoEm() { return enviadoEm; }

    public void setEnviadoEm(LocalDateTime enviadoEm) { this.enviadoEm = enviadoEm; }
}
