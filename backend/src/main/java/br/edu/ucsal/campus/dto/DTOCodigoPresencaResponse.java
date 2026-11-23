package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.CodigoPresenca;

import java.time.LocalDateTime;

public record DTOCodigoPresencaResponse(
        Long id,
        Long eventoId,
        String token,
        boolean ativo,
        LocalDateTime dataCriacao,
        String qrCodeBase64
) {

    public static DTOCodigoPresencaResponse de(
            CodigoPresenca codigo,
            String qrCodeBase64) {

        return new DTOCodigoPresencaResponse(
                codigo.getId(),
                codigo.getEvento().getId(),
                codigo.getToken(),
                codigo.isAtivo(),
                codigo.getDataCriacao(),
                qrCodeBase64
        );
    }
}
