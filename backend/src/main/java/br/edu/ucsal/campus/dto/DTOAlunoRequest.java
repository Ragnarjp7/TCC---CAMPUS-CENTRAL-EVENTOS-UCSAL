package br.edu.ucsal.campus.dto;

public record DTOAlunoRequest(String nome, String email, String matricula,
                              Long cursoId, String senha) {
}