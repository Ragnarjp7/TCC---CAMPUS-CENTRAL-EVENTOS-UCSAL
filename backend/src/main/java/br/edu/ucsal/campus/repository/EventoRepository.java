package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Evento;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

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
}