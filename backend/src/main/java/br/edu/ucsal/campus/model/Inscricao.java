package br.edu.ucsal.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "evento_id"}))
public class Inscricao {

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
    private LocalDateTime dataInscricao = LocalDateTime.now();

    public Inscricao() {
    }

    public Inscricao(Aluno aluno, Evento evento) {
        this.aluno = aluno;
        this.evento = evento;
    }

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public Evento getEvento() { return evento; }
    public LocalDateTime getDataInscricao() { return dataInscricao; }
}