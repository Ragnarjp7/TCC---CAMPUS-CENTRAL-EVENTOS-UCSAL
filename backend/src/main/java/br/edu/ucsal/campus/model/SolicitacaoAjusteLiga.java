package br.edu.ucsal.campus.model;

import br.edu.ucsal.campus.model.enums.StatusAjusteLiga;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_ajuste_liga")
public class SolicitacaoAjusteLiga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liga_id", nullable = false)
    private Liga liga;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_resposta_id")
    private Usuario responsavelResposta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricaoAjustes;

    @Column(columnDefinition = "TEXT")
    private String respostaResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAjusteLiga status = StatusAjusteLiga.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataResposta;

    private LocalDateTime dataConclusao;

    public SolicitacaoAjusteLiga() {}

    @PrePersist
    protected void prePersist() {
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDateTime.now();
        }

        if (status == null) {
            status = StatusAjusteLiga.PENDENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public Liga getLiga() {
        return liga;
    }

    public void setLiga(Liga liga) {
        this.liga = liga;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Usuario getResponsavelResposta() {
        return responsavelResposta;
    }

    public void setResponsavelResposta(Usuario responsavelResposta) {
        this.responsavelResposta = responsavelResposta;
    }

    public String getDescricaoAjustes() {
        return descricaoAjustes;
    }

    public void setDescricaoAjustes(String descricaoAjustes) {
        this.descricaoAjustes = descricaoAjustes;
    }

    public String getRespostaResponsavel() {
        return respostaResponsavel;
    }

    public void setRespostaResponsavel(String respostaResponsavel) {
        this.respostaResponsavel = respostaResponsavel;
    }

    public StatusAjusteLiga getStatus() {
        return status;
    }

    public void setStatus(StatusAjusteLiga status) {
        this.status = status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }
}
