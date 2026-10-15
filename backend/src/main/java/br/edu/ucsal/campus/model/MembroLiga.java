package br.edu.ucsal.campus.model;

import br.edu.ucsal.campus.model.enums.StatusMembro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "membros_liga",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "liga_id"}))
public class MembroLiga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liga_id", nullable = false)
    private Liga liga;

    // null = ligante padrão (RF25)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMembro status = StatusMembro.SOLICITADO;

    public MembroLiga() {}

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Liga getLiga() { return liga; }
    public void setLiga(Liga liga) { this.liga = liga; }
    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }
    public StatusMembro getStatus() { return status; }
    public void setStatus(StatusMembro status) { this.status = status; }
}