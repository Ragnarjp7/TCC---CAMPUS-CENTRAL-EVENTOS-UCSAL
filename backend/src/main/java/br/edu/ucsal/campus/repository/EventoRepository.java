package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Evento;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Detalhe do evento com categoria e liga já carregadas
    @EntityGraph(attributePaths = {"categoria", "liga"})
    @Query("SELECT e FROM Evento e WHERE e.id = :id")
    Optional<Evento> findDetalhadoById(@Param("id") Long id);

    // RF31/RF33 - Consulta paginada de eventos com filtros opcionais.
    // Os filtros "vazios" chegam como '' (texto) ou null (categoria) e não restringem nada.
    @EntityGraph(attributePaths = {"categoria", "liga"})
    @Query(value = """
            SELECT e FROM Evento e
            LEFT JOIN e.liga l
            WHERE e.status = :status
            AND e.dataFim >= :inicio
            AND e.dataInicio < :fim
            AND (:categoriaId IS NULL OR e.categoria.id = :categoriaId)
            AND (:modalidade = '' OR LOWER(e.modalidade) = :modalidade)
            AND (:busca = ''
                 OR LOWER(e.titulo) LIKE :busca
                 OR LOWER(e.descricao) LIKE :busca
                 OR LOWER(e.local) LIKE :busca
                 OR LOWER(l.nome) LIKE :busca)
            """,
            countQuery = """
            SELECT COUNT(e) FROM Evento e
            LEFT JOIN e.liga l
            WHERE e.status = :status
            AND e.dataFim >= :inicio
            AND e.dataInicio < :fim
            AND (:categoriaId IS NULL OR e.categoria.id = :categoriaId)
            AND (:modalidade = '' OR LOWER(e.modalidade) = :modalidade)
            AND (:busca = ''
                 OR LOWER(e.titulo) LIKE :busca
                 OR LOWER(e.descricao) LIKE :busca
                 OR LOWER(e.local) LIKE :busca
                 OR LOWER(l.nome) LIKE :busca)
            """)
    Page<Evento> buscarComFiltros(
            @Param("status") Evento.StatusEvento status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("categoriaId") Long categoriaId,
            @Param("modalidade") String modalidade,
            @Param("busca") String busca,
            Pageable pageable);

    // Lista todos os eventos com categoria (consulta administrativa)
    // LEFT JOIN: a categoria agora é opcional
    @Query("SELECT e FROM Evento e LEFT JOIN FETCH e.categoria")
    List<Evento> findAllComCategoria();

    // Lista eventos por status
    List<Evento> findByStatus(Evento.StatusEvento status);

    // Lista eventos por categoria
    List<Evento> findByCategoriaId(Long categoriaId);

    // Lista eventos aprovados de uma categoria
    @Query("""
            SELECT e FROM Evento e
            JOIN FETCH e.categoria
            WHERE e.categoria.id = :categoriaId
            AND e.status = :status
            ORDER BY e.dataInicio, e.horaInicio
            """)
    List<Evento> findByCategoriaIdAndStatus(
            @Param("categoriaId") Long categoriaId,
            @Param("status") Evento.StatusEvento status);

    // Lista eventos dentro de um período (datas inclusivas)
    @Query("""
            SELECT e FROM Evento e
            LEFT JOIN FETCH e.categoria
            WHERE e.dataInicio <= :fim
            AND e.dataFim >= :inicio
            ORDER BY e.dataInicio, e.horaInicio
            """)
    List<Evento> findByPedido(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    // Lista eventos aprovados dentro de um período (datas inclusivas)
    @Query("""
            SELECT e FROM Evento e
            LEFT JOIN FETCH e.categoria
            WHERE e.dataInicio <= :fim
            AND e.dataFim >= :inicio
            AND e.status = :status
            ORDER BY e.dataInicio, e.horaInicio
            """)
    List<Evento> findByPeriodoAndStatus(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Evento.StatusEvento status);

    // Lista eventos por status com categoria carregada
    @Query("""
            SELECT e FROM Evento e
            LEFT JOIN FETCH e.categoria
            WHERE e.status = :status
            ORDER BY e.dataInicio, e.horaInicio
            """)
    List<Evento> findByStatusComCategoria(
            @Param("status") Evento.StatusEvento status);

    Long countById(Long id);

    // Trava a linha do evento durante a inscrição
    // para evitar que inscrições simultâneas ultrapassem a capacidade
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evento e WHERE e.id = :id")
    Optional<Evento> findByIdParaInscricao(@Param("id") Long id);

    // RF29 - Evento com tudo que a análise do CAEX exibe (categoria, liga e responsável)
    @EntityGraph(attributePaths = {"categoria", "liga", "criadoPor"})
    @Query("SELECT e FROM Evento e WHERE e.id = :id")
    Optional<Evento> findParaAnaliseById(@Param("id") Long id);

    // RF29 - Fila de análise do CAEX: os enviados há mais tempo primeiro
    @EntityGraph(attributePaths = {"categoria", "liga", "criadoPor"})
    @Query("""
            SELECT e FROM Evento e
            WHERE e.status = :status
            ORDER BY e.enviadoEm ASC NULLS FIRST, e.id ASC
            """)
    List<Evento> buscarParaAnalise(@Param("status") Evento.StatusEvento status);

    // RF27 - Eventos pelos quais o usuário responde: os que ele criou
    // e os das ligas em que ele pode criar/editar eventos
    @EntityGraph(attributePaths = {"categoria", "liga", "criadoPor"})
    @Query("""
            SELECT e FROM Evento e
            WHERE e.criadoPor.id = :usuarioId
            OR e.liga.id IN :ligaIds
            ORDER BY e.id DESC
            """)
    List<Evento> buscarDoResponsavel(
            @Param("usuarioId") Long usuarioId,
            @Param("ligaIds") List<Long> ligaIds);
}
