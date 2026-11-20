package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    List<Cargo> findByLigaId(Long ligaId);

    // Verifica se já existe um cargo com esse nome na liga
    boolean existsByLigaIdAndNomeIgnoreCase(
            Long ligaId,
            String nome
    );

    // Verifica duplicidade ao editar, ignorando o próprio cargo
    boolean existsByLigaIdAndNomeIgnoreCaseAndIdNot(
            Long ligaId,
            String nome,
            Long id
    );
}
