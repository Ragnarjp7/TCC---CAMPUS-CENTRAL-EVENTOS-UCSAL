package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Publicacao;
import br.edu.ucsal.campus.model.enums.PublicoPublicacao;
import br.edu.ucsal.campus.model.enums.TipoAutorPublicacao;

import java.time.LocalDateTime;

public record DTOPublicacaoResponse(
        Long id,
        String titulo,
        String conteudo,
        TipoAutorPublicacao tipoAutor,
        PublicoPublicacao publico,
        Long autorId,
        String autorNome,
        Long ligaId,
        String ligaNome,
        Long cursoDestinoId,
        String cursoDestinoNome,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {

    public static DTOPublicacaoResponse de(Publicacao publicacao) {

        return new DTOPublicacaoResponse(
                publicacao.getId(),
                publicacao.getTitulo(),
                publicacao.getConteudo(),
                publicacao.getTipoAutor(),
                publicacao.getPublico(),
                publicacao.getAutor().getId(),
                publicacao.getAutor().getNome(),
                publicacao.getLigaAutora() != null
                        ? publicacao.getLigaAutora().getId()
                        : null,
                publicacao.getLigaAutora() != null
                        ? publicacao.getLigaAutora().getNome()
                        : null,
                publicacao.getCursoDestino() != null
                        ? publicacao.getCursoDestino().getId()
                        : null,
                publicacao.getCursoDestino() != null
                        ? publicacao.getCursoDestino().getNome()
                        : null,
                publicacao.getDataCriacao(),
                publicacao.getDataAtualizacao()
        );
    }
}
