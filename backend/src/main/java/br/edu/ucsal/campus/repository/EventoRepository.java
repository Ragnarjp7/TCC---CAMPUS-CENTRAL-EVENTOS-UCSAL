package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Evento;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository
        extends JpaRepository<Evento, Long> {


    @Query("SELECT e FROM Evento e JOIN FETCH e.categoria") 
    List<Evento> findAllComCategoria();
    
    List<Evento> findByStatus(Evento.StatusEvento status);

    List<Evento> findByCategoriaId(Long categoriaId);

    @Query("""
            SELECT e FROM Evento e JOIN FETCH e.categoria WHERE e.dataInicio < :fim AND e.dataFim >= :inicio
            ORDER BY e.dataInicio
            """)
    List<Evento> findByPedido(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    Long countById(Long id);

    // Trava a linha do evento durante a inscrição para evitar que duas
    // inscrições simultâneas estourem a capacidade
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evento e WHERE e.id = :id")
    Optional<Evento> findByIdParaInscricao(@Param("id") Long id);
}
