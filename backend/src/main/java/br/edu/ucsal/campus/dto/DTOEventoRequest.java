package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Dados para cadastro e reenvio de proposta de evento.
 * Mantém os campos legados usados pela listagem e os campos completos
 * do formulário de submissão ao CAEX.
 */
public record DTOEventoRequest(
        Long ligaId,
        Long categoriaId,
        String proposta,

        // Dados básicos/compatibilidade com o fluxo de eventos
        String titulo,
        String descricao,
        String local,
        LocalDate dataInicio,
        LocalDate dataFim,
        String modalidade,
        Integer capacidade,

        // Dados do proponente
        String proponenteNome,
        String proponenteCpf,
        String proponenteEmail,
        String proponenteTelefone,
        String proponenteLattes,
        String proponenteMiniCurriculo,

        // Formulário CAEX
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
        LocalTime horaInicio,
        LocalTime horaFim,
        String diasHorarios,
        String programacao,
        Boolean emiteCertificado,
        Evento.TipoInvestimento tipoInvestimento,
        BigDecimal valor,
        String linkPecasPublicitarias,
        String observacoes,
        Boolean autorizacaoLgpd
) {
    public Evento toEvento() {
        Evento e = new Evento();

        e.setTitulo(titulo);
        e.setDescricao(descricao);
        e.setLocal(local);
        e.setDataInicio(dataInicio);
        e.setDataFim(dataFim);
        e.setModalidade(modalidade);
        e.setCapacidade(capacidade);
        e.setProposta(proposta);

        e.setProponenteNome(proponenteNome);
        e.setProponenteCpf(proponenteCpf);
        e.setProponenteEmail(proponenteEmail);
        e.setProponenteTelefone(proponenteTelefone);
        e.setProponenteLattes(proponenteLattes);
        e.setProponenteMiniCurriculo(proponenteMiniCurriculo);

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
        e.setRecursos(recursos);
        e.setRecursosOutro(recursosOutro);
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
}
