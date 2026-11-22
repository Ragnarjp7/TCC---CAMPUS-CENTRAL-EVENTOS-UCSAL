package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.enums.PublicoPublicacao;

public record DTOPublicacaoRequest(
        String titulo,
        String conteudo,
        PublicoPublicacao publico,
        Long cursoDestinoId,
        Long ligaId
) {
}
