package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    List<Auditoria> findAllByOrderByDataHoraDesc();

    @EntityGraph(attributePaths = {"usuario"})
    List<Auditoria> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Auditoria> findByEntidadeAndRegistroIdOrderByDataHoraDesc(
            String entidade,
            Long registroId
    );
}
