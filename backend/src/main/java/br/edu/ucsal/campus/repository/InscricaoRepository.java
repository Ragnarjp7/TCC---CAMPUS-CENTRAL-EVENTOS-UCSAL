package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByAlunoIdAndEventoId(Long alunoId, Long eventoId);

    long countByEventoId(Long eventoId);

    Optional<Inscricao> findByAlunoIdAndEventoId(Long alunoId, Long eventoId);

    @Query("SELECT i FROM Inscricao i JOIN FETCH i.aluno JOIN FETCH i.evento " +
           "WHERE i.evento.id = :eventoId ORDER BY i.dataInscricao")
    List<Inscricao> findByEventoIdCompleto(@Param("eventoId") Long eventoId);

    @Query("SELECT i FROM Inscricao i JOIN FETCH i.aluno JOIN FETCH i.evento " +
           "WHERE i.aluno.id = :alunoId ORDER BY i.evento.dataInicio")
    List<Inscricao> findByAlunoIdCompleto(@Param("alunoId") Long alunoId);
}