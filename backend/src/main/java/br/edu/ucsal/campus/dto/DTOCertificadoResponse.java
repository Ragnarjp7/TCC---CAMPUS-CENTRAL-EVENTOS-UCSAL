package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Certificado;

import java.time.LocalDateTime;

public record DTOCertificadoResponse(
        Long id,
        Long eventoId,
        String nomeEvento,
        String nomeParticipante,
        String emailDestinatario,
        LocalDateTime dataGeracao,
        boolean emailEnviado
) {

    public static DTOCertificadoResponse de(Certificado certificado) {
        return new DTOCertificadoResponse(
                certificado.getId(),
                certificado.getPresenca().getEvento().getId(),
                certificado.getPresenca().getEvento().getTitulo(),
                certificado.getNomeParticipante(),
                certificado.getEmailDestinatario(),
                certificado.getDataGeracao(),
                certificado.isEmailEnviado()
        );
    }
}