package br.edu.ucsal.campus.dto;

public record DTOConfirmarPresencaExternoRequest(
        String token,
        String nome,
        String email
) {
}
