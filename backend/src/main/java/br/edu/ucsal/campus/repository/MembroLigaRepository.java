package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.MembroLiga;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembroLigaRepository extends JpaRepository<MembroLiga, Long> {
    Optional<MembroLiga> findByAlunoIdAndLigaId(Long alunoId, Long ligaId);
    List<MembroLiga> findByLigaId(Long ligaId);
}