package br.edu.ucsal.campus.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(nullable = false, length = 150)
    private String local;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(nullable = false, length = 30)
    private String modalidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEvento status = StatusEvento.AGUARDANDO_ANALISE;

    public enum StatusEvento {
        AGUARDANDO_ANALISE,
        APROVADO,
        AJUSTES_SOLICITADOS,
        REJEITADO,
        CANCELADO,
        ENCERRADO
    }

    private Integer capacidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "observacao_caex", length = 1000)
    private String observacaoCaex;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liga_id")
    private Liga liga;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    public Evento() {}

    public void setId(Long id) { this.id = id;}

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }

    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) {this.descricao = descricao;}

    public String getLocal() { return local; }

    public void setLocal(String local) { this.local = local; }

    public LocalDateTime getDataInicio() { return dataInicio; }

    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }

    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

    public String getModalidade() { return modalidade; }

    public void setModalidade(String modalidade) { this.modalidade = modalidade; }
    
    public Integer getCapacidade() { return capacidade;
    }
    public void setCapacidade(Integer capacidade) {this.capacidade = capacidade;
    }
    public Categoria getCategoria() { return categoria;
    }
    public void setCategoria(Categoria categoria) {this.categoria = categoria;
    }
    public StatusEvento getStatus() { return status;
    }
    public void setStatus(StatusEvento status) { this.status = status;
    }

    public String getObservacaoCaex() { return observacaoCaex; }

    public void setObservacaoCaex(String observacaoCaex) { this.observacaoCaex = observacaoCaex; }

    public Liga getLiga() { return liga; }

    public void setLiga(Liga liga) { this.liga = liga; }

    public Usuario getCriadoPor() { return criadoPor; }
    
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
}
