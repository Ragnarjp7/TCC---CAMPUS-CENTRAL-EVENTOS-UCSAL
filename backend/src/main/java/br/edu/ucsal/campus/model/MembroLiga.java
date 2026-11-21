package br.edu.ucsal.campus.model;

import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.model.enums.TipoMembroLiga;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "membros_liga",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"aluno_id", "liga_id"}
    )
)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_membro", length = 25)
    private TipoMembroLiga tipoMembro = TipoMembroLiga.LIGANTE_PADRAO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMembro status = StatusMembro.SOLICITADO;

    public MembroLiga() {}

    @PrePersist
    @PreUpdate
    private void sincronizarTipoMembro() {
        if (cargo == null) {
            tipoMembro = TipoMembroLiga.LIGANTE_PADRAO;
        } else {
            tipoMembro = TipoMembroLiga.OCUPANTE_CARGO;
        }
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Liga getLiga() {
        return liga;
    }

    public void setLiga(Liga liga) {
        this.liga = liga;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;

        this.tipoMembro = cargo == null
                ? TipoMembroLiga.LIGANTE_PADRAO
                : TipoMembroLiga.OCUPANTE_CARGO;
    }

    public TipoMembroLiga getTipoMembro() {
        if (tipoMembro == null) {
            return cargo == null
                    ? TipoMembroLiga.LIGANTE_PADRAO
                    : TipoMembroLiga.OCUPANTE_CARGO;
        }

        return tipoMembro;
    }

    public void setTipoMembro(TipoMembroLiga tipoMembro) {
        this.tipoMembro = tipoMembro;
    }

    public StatusMembro getStatus() {
        return status;
    }

    public void setStatus(StatusMembro status) {
        this.status = status;
    }
}
