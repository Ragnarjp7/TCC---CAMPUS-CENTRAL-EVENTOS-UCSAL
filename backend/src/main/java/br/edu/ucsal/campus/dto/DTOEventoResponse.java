package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;

import java.time.LocalDateTime;

// Evento como é exibido nas consultas (lista, filtros e detalhe).
// "inscrito" só é preenchido para aluno logado; nos demais casos vem nulo.
public record DTOEventoResponse(
        Long id,
        String titulo,
        String descricao,
        String local,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String modalidade,
        Evento.StatusEvento status,
        Integer capacidade,
        long inscritos,
        Long categoriaId,
        String categoriaNome,
        Long ligaId,
        String ligaNome,
        Boolean inscrito
) {

    public static DTOEventoResponse de(Evento e, long inscritos, Boolean inscrito) {
        boolean temLiga = e.getLiga() != null;
        boolean temCategoria = e.getCategoria() != null;

        return new DTOEventoResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescricao(),
                e.getLocal(),
                e.getDataInicio(),
                e.getDataFim(),
                e.getModalidade(),
                e.getStatus(),
                e.getCapacidade(),
                inscritos,
                temCategoria ? e.getCategoria().getId() : null,
                temCategoria ? e.getCategoria().getNome() : null,
                temLiga ? e.getLiga().getId() : null,
                temLiga ? e.getLiga().getNome() : null,
                inscrito
        );
    }
}
