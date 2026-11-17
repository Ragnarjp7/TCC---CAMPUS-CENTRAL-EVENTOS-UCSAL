package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.enums.StatusLiga;

public record DTOLigaResponse(
        Long id,
        String nome,
        String descricao,
        StatusLiga status,
        Long cursoId,
        String cursoNome,
        Long orientadorId,
        String orientadorNome
) {

    public static DTOLigaResponse de(Liga liga) {
        return new DTOLigaResponse(
                liga.getId(),
                liga.getNome(),
                liga.getDescricao(),
                liga.getStatus(),
                liga.getCurso().getId(),
                liga.getCurso().getNome(),
                liga.getOrientador() != null
                        ? liga.getOrientador().getId()
                        : null,
                liga.getOrientador() != null
                        ? liga.getOrientador().getUsuario().getNome()
                        : null
        );
    }
}
