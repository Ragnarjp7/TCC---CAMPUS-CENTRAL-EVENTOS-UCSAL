package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    List<Cargo> findByLigaId(Long ligaId);
}