package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public record DTOEventoRequest(

        // Vínculos
        Long ligaId,
        Long categoriaId,

        // Proponente
        String proponenteNome,
        String proponenteCpf,
        String proponenteEmail,
        String proponenteTelefone,
        String proponenteLattes,
        String proponenteMiniCurriculo,

        // Informações do evento
        String titulo,
        String caracterizacaoAtividade,
        Evento.AreaTematica areaTematica,
        Evento.Abrangencia abrangencia,
        Evento.TipoExtensao tipoExtensao,
        String apresentacao,
        Boolean publicarNoCampus,
        String objetivoGeral,
        String objetivosEspecificos,
        String publicoAlvo,
        Evento.TipoLocal tipoLocal,
        String localDetalhe,
        Evento.EspacoRealizacao espacoRealizacao,
        String especificacaoEspaco,
        Evento.FaixaVagas faixaVagas,
        Set<Evento.RecursoAudiovisual> recursos,
        String recursosOutro,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horaInicio,
        LocalTime horaFim,
        String diasHorarios,
        String programacao,

        // Certificação
        Boolean emiteCertificado,

        // Investimento
        Evento.TipoInvestimento tipoInvestimento,
        BigDecimal valor,

        // Divulgação e observações
        String linkPecasPublicitarias,
        String observacoes,

        // LGPD
        Boolean autorizacaoLgpd
) {

    // Converte o DTO em entidade (liga, categoria e criador são definidos no service)
    public Evento toEvento() {

        Evento e = new Evento();

        e.setProponenteNome(proponenteNome);
        e.setProponenteCpf(proponenteCpf);
        e.setProponenteEmail(proponenteEmail);
        e.setProponenteTelefone(proponenteTelefone);
        e.setProponenteLattes(proponenteLattes);
        e.setProponenteMiniCurriculo(proponenteMiniCurriculo);

        e.setTitulo(titulo);
        e.setCaracterizacaoAtividade(caracterizacaoAtividade);
        e.setAreaTematica(areaTematica);
        e.setAbrangencia(abrangencia);
        e.setTipoExtensao(tipoExtensao);
        e.setApresentacao(apresentacao);
        e.setPublicarNoCampus(Boolean.TRUE.equals(publicarNoCampus));
        e.setObjetivoGeral(objetivoGeral);
        e.setObjetivosEspecificos(objetivosEspecificos);
        e.setPublicoAlvo(publicoAlvo);
        e.setTipoLocal(tipoLocal);
        e.setLocalDetalhe(localDetalhe);
        e.setEspacoRealizacao(espacoRealizacao);
        e.setEspecificacaoEspaco(especificacaoEspaco);
        e.setFaixaVagas(faixaVagas);
        e.setRecursos(recursos == null ? new HashSet<>() : new HashSet<>(recursos));
        e.setRecursosOutro(recursosOutro);
        e.setDataInicio(dataInicio);
        e.setDataFim(dataFim);
        e.setHoraInicio(horaInicio);
        e.setHoraFim(horaFim);
        e.setDiasHorarios(diasHorarios);
        e.setProgramacao(programacao);

        e.setEmiteCertificado(Boolean.TRUE.equals(emiteCertificado));

        e.setTipoInvestimento(tipoInvestimento);
        e.setValor(valor);

        e.setLinkPecasPublicitarias(linkPecasPublicitarias);
        e.setObservacoes(observacoes);
        e.setAutorizacaoLgpd(Boolean.TRUE.equals(autorizacaoLgpd));

        return e;
    }

    public Object descricao() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'descricao'");
    }
}