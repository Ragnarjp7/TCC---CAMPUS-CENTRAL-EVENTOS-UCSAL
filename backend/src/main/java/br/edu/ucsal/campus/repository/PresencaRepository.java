package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Presenca;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PresencaRepository
        extends JpaRepository<Presenca, Long> {

    boolean existsByEventoIdAndInscricaoId(
            Long eventoId,
            Long inscricaoId);

    boolean existsByEventoIdAndUsuarioId(
            Long eventoId,
            Long usuarioId);

    boolean existsByEventoIdAndEmailParticipanteExternoIgnoreCase(
            Long eventoId,
            String email);

    List<Presenca> findByEventoIdOrderByDataConfirmacaoAsc(
            Long eventoId);

    Optional<Presenca> findByIdAndEventoId(
            Long presencaId,
            Long eventoId);

    long countByEventoId(Long eventoId);

    @Query("""
            SELECT p
            FROM Presenca p
            JOIN FETCH p.evento e
            LEFT JOIN FETCH p.usuario u
            LEFT JOIN FETCH p.inscricao i
            LEFT JOIN FETCH i.aluno a
            LEFT JOIN FETCH a.usuario
            WHERE e.id = :eventoId
            ORDER BY p.dataConfirmacao ASC
            """)
    List<Presenca> buscarListaCompletaPorEvento(
            @Param("eventoId") Long eventoId);
}
