package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.CodigoPresenca;

import java.time.LocalDateTime;

public record DTOCodigoPresencaResponse(
        Long id,
        Long eventoId,
        String token,
        boolean ativo,
        LocalDateTime dataCriacao
) {

    public static DTOCodigoPresencaResponse de(
            CodigoPresenca codigo) {

        return new DTOCodigoPresencaResponse(
                codigo.getId(),
                codigo.getEvento().getId(),
                codigo.getToken(),
                codigo.isAtivo(),
                codigo.getDataCriacao()
        );
    }
}
