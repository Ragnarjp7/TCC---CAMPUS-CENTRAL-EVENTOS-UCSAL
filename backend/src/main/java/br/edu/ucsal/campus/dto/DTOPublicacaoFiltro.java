package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.enums.PublicoPublicacao;
import br.edu.ucsal.campus.model.enums.TipoAutorPublicacao;

public record DTOPublicacaoFiltro(
        String texto,
        TipoAutorPublicacao tipoAutor,
        PublicoPublicacao publico,
        Long cursoId,
        Long autorId
) {
}
