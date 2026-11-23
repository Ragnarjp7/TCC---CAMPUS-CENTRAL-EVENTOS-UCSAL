package br.edu.ucsal.campus.model;

import br.edu.ucsal.campus.model.enums.PublicoPublicacao;
import br.edu.ucsal.campus.model.enums.TipoAutorPublicacao;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publicacoes")
public class Publicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_autor", nullable = false, length = 30)
    private TipoAutorPublicacao tipoAutor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PublicoPublicacao publico;

    // Usuário autenticado que realizou a publicação.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    // Preenchido quando a publicação pertence a uma liga.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liga_autora_id")
    private Liga ligaAutora;

    // Preenchido quando o público é específico.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_destino_id")
    private Curso cursoDestino;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Exclusão lógica para preservar o histórico.
    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "data_exclusao")
    private LocalDateTime dataExclusao;

    public Publicacao() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public TipoAutorPublicacao getTipoAutor() {
        return tipoAutor;
    }

    public void setTipoAutor(TipoAutorPublicacao tipoAutor) {
        this.tipoAutor = tipoAutor;
    }

    public PublicoPublicacao getPublico() {
        return publico;
    }

    public void setPublico(PublicoPublicacao publico) {
        this.publico = publico;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Liga getLigaAutora() {
        return ligaAutora;
    }

    public void setLigaAutora(Liga ligaAutora) {
        this.ligaAutora = ligaAutora;
    }

    public Curso getCursoDestino() {
        return cursoDestino;
    }

    public void setCursoDestino(Curso cursoDestino) {
        this.cursoDestino = cursoDestino;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(LocalDateTime dataExclusao) {
        this.dataExclusao = dataExclusao;
    }
}
