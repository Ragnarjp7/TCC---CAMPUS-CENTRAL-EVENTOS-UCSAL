
package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Publicacao;
import br.edu.ucsal.campus.model.enums.TipoAutorPublicacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublicacaoRepository
        extends JpaRepository<Publicacao, Long> {

    // Feed institucional:
    // publicações gerais e publicações direcionadas ao curso do aluno.
    // Coordenadores possuem prioridade de exibição.
    @Query("""
        SELECT p
        FROM Publicacao p
        WHERE p.ativa = true
          AND (
              p.publico = br.edu.ucsal.campus.model.enums.PublicoPublicacao.GERAL
              OR (
                  p.publico = br.edu.ucsal.campus.model.enums.PublicoPublicacao.CURSO_ESPECIFICO
                  AND p.cursoDestino.id = :cursoId
              )
          )
        ORDER BY
            CASE
                WHEN p.tipoAutor = :tipoCoordenador THEN 0
                ELSE 1
            END ASC,
            p.dataCriacao DESC
        """)
    Page<Publicacao> buscarFeedPorCurso(
            @Param("cursoId") Long cursoId,
            @Param("tipoCoordenador") TipoAutorPublicacao tipoCoordenador,
            Pageable pageable
    );

    // Busca somente publicações ativas.
    Optional<Publicacao> findByIdAndAtivaTrue(Long id);

    // Consulta publicações ativas de um autor.
    Page<Publicacao> findByAutorIdAndAtivaTrueOrderByDataCriacaoDesc(
            Long autorId,
            Pageable pageable
    );

    // Consulta publicações ativas de uma liga.
    Page<Publicacao> findByLigaAutoraIdAndAtivaTrueOrderByDataCriacaoDesc(
            Long ligaId,
            Pageable pageable
    );

    // Consulta administrativa de todas as publicações.
    Page<Publicacao> findAllByOrderByDataCriacaoDesc(
            Pageable pageable
    );
}
