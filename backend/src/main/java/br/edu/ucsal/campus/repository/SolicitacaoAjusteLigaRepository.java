package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.SolicitacaoAjusteLiga;
import br.edu.ucsal.campus.model.enums.StatusAjusteLiga;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoAjusteLigaRepository extends JpaRepository<SolicitacaoAjusteLiga, Long> {

    @EntityGraph(attributePaths = {
            "liga",
            "solicitante",
            "responsavelResposta"
    })
    List<SolicitacaoAjusteLiga> findByLigaIdOrderByDataSolicitacaoDesc(
            Long ligaId
    );

    @EntityGraph(attributePaths = {
            "liga",
            "solicitante",
            "responsavelResposta"
    })
    List<SolicitacaoAjusteLiga> findByStatusOrderByDataSolicitacaoAsc(
            StatusAjusteLiga status
    );

    boolean existsByLigaIdAndStatusNot(
            Long ligaId,
            StatusAjusteLiga status
    );

    @Override
    @EntityGraph(attributePaths = {
            "liga",
            "solicitante",
            "responsavelResposta"
    })
    Optional<SolicitacaoAjusteLiga> findById(Long id);
    
}
