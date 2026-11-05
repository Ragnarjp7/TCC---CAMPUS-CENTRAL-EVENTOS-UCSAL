package br.edu.ucsal.campus.dto;

public record DTOLoginResponse(
    String token,
    String tipo,
    long expiraEmMinutos,
    DTOMeResponse usuario
) {
}
