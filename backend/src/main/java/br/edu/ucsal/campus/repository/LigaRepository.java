package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.enums.StatusLiga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LigaRepository extends JpaRepository<Liga, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    @EntityGraph(attributePaths = {
            "curso",
            "orientador",
            "orientador.usuario",
            "alunoResponsavel",
            "alunoResponsavel.usuario"
    })
    List<Liga> findByStatus(StatusLiga status);

    @EntityGraph(attributePaths = {
            "curso",
            "orientador",
            "orientador.usuario",
            "alunoResponsavel",
            "alunoResponsavel.usuario"
    })
    List<Liga> findByStatusAndCursoId(StatusLiga status, Long cursoId);

    @Override
    @EntityGraph(attributePaths = {
            "curso",
            "orientador",
            "orientador.usuario",
            "alunoResponsavel",
            "alunoResponsavel.usuario"
    })
    Optional<Liga> findById(Long id);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    // Ligas orientadas pelo professor (usuário) informado
    @Query("SELECT l.id FROM Liga l WHERE l.orientador.usuario.id = :usuarioId")
    List<Long> buscarIdsOrientadasPor(@Param("usuarioId") Long usuarioId);
}
