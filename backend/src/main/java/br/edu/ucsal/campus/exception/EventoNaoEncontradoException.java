package br.edu.ucsal.campus.exception;

public class EventoNaoEncontradoException extends RuntimeException {
    public EventoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}