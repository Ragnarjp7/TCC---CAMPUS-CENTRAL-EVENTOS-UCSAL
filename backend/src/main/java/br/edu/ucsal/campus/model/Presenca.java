package br.edu.ucsal.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "presencas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_presenca_evento_inscricao",
            columnNames = {"evento_id", "inscricao_id"}
        ),
        @UniqueConstraint(
            name = "uk_presenca_evento_usuario",
            columnNames = {"evento_id", "usuario_id"}
        ),
        @UniqueConstraint(
            name = "uk_presenca_evento_email_externo",
            columnNames = {"evento_id", "email_participante_externo"}
        )
    }
)
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscricao_id")
    private Inscricao inscricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "nome_participante_externo", length = 150)
    private String nomeParticipanteExterno;

    @Column(name = "email_participante_externo", length = 150)
    private String emailParticipanteExterno;

    @Column(name = "data_confirmacao", nullable = false)
    private LocalDateTime dataConfirmacao;

    public Presenca() {
    }

    @PrePersist
    protected void prePersist() {
        if (dataConfirmacao == null) {
            dataConfirmacao = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public void setInscricao(Inscricao inscricao) {
        this.inscricao = inscricao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNomeParticipanteExterno() {
        return nomeParticipanteExterno;
    }

    public void setNomeParticipanteExterno(String nomeParticipanteExterno) {
        this.nomeParticipanteExterno = nomeParticipanteExterno;
    }

    public String getEmailParticipanteExterno() {
        return emailParticipanteExterno;
    }

    public void setEmailParticipanteExterno(String emailParticipanteExterno) {
        this.emailParticipanteExterno = emailParticipanteExterno;
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }
}
