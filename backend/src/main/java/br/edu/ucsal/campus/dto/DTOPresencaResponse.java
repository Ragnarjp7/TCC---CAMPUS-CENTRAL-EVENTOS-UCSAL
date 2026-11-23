package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Presenca;

import java.time.LocalDateTime;

public record DTOPresencaResponse(
        Long id,
        Long eventoId,
        String tituloEvento,
        String nomeParticipante,
        LocalDateTime dataConfirmacao
) {

    public static DTOPresencaResponse de(Presenca presenca) {

        String nomeParticipante = presenca.getNomeParticipanteExterno();

        if (presenca.getUsuario() != null) {
            nomeParticipante = presenca.getUsuario().getNome();

        } else if (presenca.getInscricao() != null
                && presenca.getInscricao().getAluno() != null) {

            nomeParticipante = presenca.getInscricao()
                    .getAluno()
                    .getUsuario()
                    .getNome();
        }

        return new DTOPresencaResponse(
                presenca.getId(),
                presenca.getEvento().getId(),
                presenca.getEvento().getTitulo(),
                nomeParticipante,
                presenca.getDataConfirmacao()
        );
    }
}
