
package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;

public record DTOEventoStatusResponse(
        Long id,
        String titulo,
        Evento.StatusEvento status,
        String observacaoCaex
) {

    public static DTOEventoStatusResponse de(Evento e) {
        return new DTOEventoStatusResponse(
                e.getId(),
                e.getTitulo(),
                e.getStatus(),
                e.getObservacaoCaex()
        );
    }
}
