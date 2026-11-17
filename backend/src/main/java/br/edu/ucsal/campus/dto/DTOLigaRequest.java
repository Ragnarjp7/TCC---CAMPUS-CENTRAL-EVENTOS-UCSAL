package br.edu.ucsal.campus.dto;

public record DTOLigaRequest(
        String nome,
        String descricao,
        Long orientadorId
) {
}
