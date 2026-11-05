package br.edu.ucsal.campus.model;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import br.edu.ucsal.campus.model.enums.Permissao;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cargos",uniqueConstraints = @UniqueConstraint(columnNames = {"liga_id", "nome"}))
    public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liga_id", nullable = false)
    private Liga liga;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cargo_permissoes", joinColumns = @JoinColumn(name = "cargo_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", length = 40)
    private Set<Permissao> permissoes = new HashSet<>();

    public Cargo() {}

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Liga getLiga() { return liga; }
    public void setLiga(Liga liga) { this.liga = liga; }
    public Set<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<Permissao> permissoes) { this.permissoes = permissoes; 
        
    }
}