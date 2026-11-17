package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;

public record DTOStatusRequest(Evento.StatusEvento status, String observacao) {
}
