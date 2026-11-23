package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.CodigoPresenca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoPresencaRepository
        extends JpaRepository<CodigoPresenca, Long> {

    Optional<CodigoPresenca> findByEventoId(Long eventoId);

    Optional<CodigoPresenca> findByEventoIdAndAtivoTrue(
            Long eventoId
    );

    Optional<CodigoPresenca> findByTokenAndAtivoTrue(
            String token
    );

    boolean existsByToken(String token);
}
