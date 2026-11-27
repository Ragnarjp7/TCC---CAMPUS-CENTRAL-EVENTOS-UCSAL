package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOCodigoPresencaResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.EventoNaoEncontradoException;
import br.edu.ucsal.campus.model.CodigoPresenca;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.CodigoPresencaRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class CodigoPresencaService {

    private final CodigoPresencaRepository codigoRepository;
    private final EventoRepository eventoRepository;
    private final MembroLigaRepository membroLigaRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public CodigoPresencaService(
            CodigoPresencaRepository codigoRepository,
            EventoRepository eventoRepository,
            MembroLigaRepository membroLigaRepository) {

        this.codigoRepository = codigoRepository;
        this.eventoRepository = eventoRepository;
        this.membroLigaRepository = membroLigaRepository;
    }

    /**
     * Gera o QR Code uma única vez por evento.
     *
     * O código só pode ser criado em uma data compreendida entre
     * dataInicio e dataFim do evento. Se já existir, o mesmo código
     * é retornado, sem criar outro token ou substituir o anterior.
     */
    @Transactional
    public DTOCodigoPresencaResponse gerar(
            Long eventoId,
            UsuarioAutenticado usuario) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException(
                                "Evento não encontrado"));

        validarPermissao(evento, usuario);
        validarEventoParaGerarCodigo(evento);

        CodigoPresenca codigoExistente = codigoRepository
                .findByEventoId(eventoId)
                .orElse(null);

        if (codigoExistente != null) {
            return DTOCodigoPresencaResponse.de(
                    codigoExistente,
                    gerarImagemQrCode(codigoExistente.getToken()));
        }

        CodigoPresenca codigo = new CodigoPresenca();
        codigo.setEvento(evento);
        codigo.setToken(gerarTokenUnico());
        codigo.setAtivo(true);
        codigo.setDataCriacao(LocalDateTime.now());

        CodigoPresenca codigoSalvo = codigoRepository.save(codigo);

        String imagemQrCode = gerarImagemQrCode(codigoSalvo.getToken());

        return DTOCodigoPresencaResponse.de(
                codigoSalvo,
                imagemQrCode);
    }

    private void validarEventoParaGerarCodigo(Evento evento) {

        if (evento.getStatus() != Evento.StatusEvento.APROVADO) {
            throw new ConflitoException(
                    "O evento precisa estar aprovado para gerar o QR Code");
        }

        if (evento.getDataInicio() == null
                || evento.getDataFim() == null
                || evento.getHoraInicio() == null
                || evento.getHoraFim() == null) {

            throw new ConflitoException(
                    "O evento não possui período definido");
        }

        LocalDateTime inicio = evento.getDataInicio()
                .atTime(evento.getHoraInicio());

        LocalDateTime fim = evento.getDataFim()
                .atTime(evento.getHoraFim());

        if (fim.isBefore(inicio)) {
            throw new ConflitoException(
                    "O período do evento está inválido");
        }

        LocalDate hoje = LocalDate.now();

        if (hoje.isBefore(evento.getDataInicio())
                || hoje.isAfter(evento.getDataFim())) {
            throw new ConflitoException(
                    "O QR Code só pode ser gerado em uma data de realização do evento");
        }
    }

    private void validarPermissao(
            Evento evento,
            UsuarioAutenticado usuario) {

        if (usuario == null || usuario.id() == null) {
            throw new ConflitoException(
                    "É necessário estar autenticado");
        }

        if (usuario.papeis() != null
                && (usuario.papeis().contains(Papel.ADMIN)
                || usuario.papeis().contains(Papel.CAEX))) {
            return;
        }

        // O criador do evento pode gerar o QR Code mesmo que
        // não seja membro da liga responsável.
        if (evento.getCriadoPor() != null
                && evento.getCriadoPor().getId().equals(usuario.id())) {
            return;
        }

        if (evento.getLiga() == null) {
            throw new ConflitoException(
                    "O evento não possui uma liga responsável");
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                evento.getLiga().getId(),
                StatusMembro.ATIVO
        ).orElseThrow(() ->
                new ConflitoException(
                        "Você não é membro ativo da liga responsável"));

        if (membro.getCargo() == null
                || membro.getCargo().getPermissoes() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.REGISTRAR_PRESENCA)) {

            throw new ConflitoException(
                    "Seu cargo não possui permissão para registrar presença");
        }
    }

    private String gerarTokenUnico() {

        String token;

        do {
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);

            token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes);

        } while (codigoRepository.existsByToken(token));

        return token;
    }

    private String gerarImagemQrCode(String conteudo) {

        try {
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix matriz = writer.encode(
                    conteudo,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matriz,
                    "PNG",
                    outputStream
            );

            return Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

        } catch (WriterException | IOException e) {
            throw new IllegalStateException(
                    "Erro ao gerar imagem do QR Code",
                    e
            );
        }
    }
}
