package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigaRepository extends JpaRepository<Liga, Long> {

    boolean existsByNomeIgnoreCase(String nome);
    List<Liga> findByStatus(StatusLiga status);
    List<Liga> findByStatusAndCursoId(StatusLiga status, Long cursoId);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
