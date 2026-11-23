package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Certificado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificadoRepository
        extends JpaRepository<Certificado, Long> {

    boolean existsByPresencaId(Long presencaId);

    Optional<Certificado> findByIdAndPresencaUsuarioId(
            Long certificadoId,
            Long usuarioId);

    List<Certificado> findByPresencaUsuarioIdOrderByDataGeracaoDesc(
            Long usuarioId);

    List<Certificado> findByPresencaEventoId(Long eventoId);
}