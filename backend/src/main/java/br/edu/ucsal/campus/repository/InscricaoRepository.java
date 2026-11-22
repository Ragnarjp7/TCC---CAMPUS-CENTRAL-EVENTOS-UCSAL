package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Inscricao;
import br.edu.ucsal.campus.model.Inscricao.StatusInscricao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByAlunoIdAndEventoIdAndStatus(
            Long alunoId,
            Long eventoId,
            StatusInscricao status);

    long countByEventoIdAndStatus(
            Long eventoId,
            StatusInscricao status);

    Optional<Inscricao> findFirstByAlunoIdAndEventoIdAndStatusOrderByDataInscricaoDesc(
            Long alunoId,
            Long eventoId,
            StatusInscricao status);

    @Query("""
            SELECT i FROM Inscricao i
            JOIN FETCH i.aluno a
            JOIN FETCH a.usuario
            JOIN FETCH i.evento e
            WHERE e.id = :eventoId
            ORDER BY i.dataInscricao DESC
            """)
    List<Inscricao> findByEventoIdCompleto(
            @Param("eventoId") Long eventoId);

    @Query("""
            SELECT i FROM Inscricao i
            JOIN FETCH i.aluno a
            JOIN FETCH a.usuario
            JOIN FETCH i.evento e
            WHERE a.id = :alunoId
            ORDER BY e.dataInicio DESC, i.dataInscricao DESC
            """)
    List<Inscricao> findByAlunoIdCompleto(
            @Param("alunoId") Long alunoId);
}
