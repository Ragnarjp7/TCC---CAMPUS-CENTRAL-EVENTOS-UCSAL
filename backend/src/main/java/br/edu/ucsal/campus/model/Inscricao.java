package br.edu.ucsal.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes")
public class Inscricao {

    public enum StatusInscricao {
        ATIVA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(name = "data_inscricao", nullable = false)
    private LocalDateTime dataInscricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusInscricao status = StatusInscricao.ATIVA;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    public Inscricao() {
    }

    public Inscricao(Aluno aluno, Evento evento) {
        this.aluno = aluno;
        this.evento = evento;
        this.dataInscricao = LocalDateTime.now();
        this.status = StatusInscricao.ATIVA;
    }

    public void cancelar() {
        if (this.status == StatusInscricao.ATIVA) {
            this.status = StatusInscricao.CANCELADA;
            this.dataCancelamento = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Evento getEvento() {
        return evento;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }
}
