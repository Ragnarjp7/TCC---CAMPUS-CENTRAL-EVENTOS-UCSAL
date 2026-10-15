package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;

public record DTOEventoStatusResponse (Long id,String titulo,Evento.StatusEvento status) {

    public static DTOEventoStatusResponse de(Evento e) {
        
        return new DTOEventoStatusResponse(e.getId(), e.getTitulo(), e.getStatus());
    }
}
