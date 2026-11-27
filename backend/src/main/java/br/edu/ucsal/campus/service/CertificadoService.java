package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOCertificadoResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Certificado;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.Presenca;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.CertificadoRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.PresencaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final PresencaRepository presencaRepository;
    private final EventoRepository eventoRepository;
    private final AlunoRepository alunoRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final JavaMailSender mailSender;

    public CertificadoService(
            CertificadoRepository certificadoRepository,
            PresencaRepository presencaRepository,
            EventoRepository eventoRepository,
            AlunoRepository alunoRepository,
            MembroLigaRepository membroLigaRepository,
            JavaMailSender mailSender) {

        this.certificadoRepository = certificadoRepository;
        this.presencaRepository = presencaRepository;
        this.eventoRepository = eventoRepository;
        this.alunoRepository = alunoRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public int gerarCertificadosEvento(
            Long eventoId,
            UsuarioAutenticado usuario) {

        Evento evento = buscarEvento(eventoId);
        validarPermissaoEvento(evento, usuario, Permissao.EMITIR_CERTIFICADO);

        if (evento.getStatus() != Evento.StatusEvento.APROVADO
                && evento.getStatus() != Evento.StatusEvento.ENCERRADO) {
            throw new ConflitoException(
                    "Os certificados só podem ser gerados para eventos aprovados ou encerrados");
        }

        if (!evento.isEmiteCertificado()) {
            throw new ConflitoException(
                    "Este evento não está configurado para emissão de certificados");
        }

        List<Presenca> presencas =
                presencaRepository.buscarListaCompletaPorEvento(eventoId);

        int quantidadeGerada = 0;

        for (Presenca presenca : presencas) {
            if (certificadoRepository.existsByPresencaId(presenca.getId())) {
                continue;
            }

            String nome = obterNomeParticipante(presenca);
            String email = obterEmailParticipante(presenca);

            if (nome == null || nome.isBlank()
                    || email == null || email.isBlank()) {
                continue;
            }

            Certificado certificado = new Certificado();
            certificado.setPresenca(presenca);
            certificado.setNomeParticipante(nome);
            certificado.setEmailDestinatario(email);
            certificado.setArquivoPdf(gerarPdf(nome, evento));
            certificado.setEmailEnviado(false);

            certificado = certificadoRepository.save(certificado);
            quantidadeGerada++;

            tentarEnviar(certificado);
        }

        return quantidadeGerada;
    }

    @Transactional(readOnly = true)
    public List<DTOCertificadoResponse> listarMeusCertificados(
            UsuarioAutenticado usuario) {

        validarUsuarioAutenticado(usuario);

        return certificadoRepository
                .findByPresencaUsuarioIdOrderByDataGeracaoDesc(usuario.id())
                .stream()
                .map(DTOCertificadoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DTOCertificadoResponse> listarCertificadosEvento(
            Long eventoId,
            UsuarioAutenticado usuario) {

        Evento evento = buscarEvento(eventoId);
        validarPermissaoEvento(evento, usuario, Permissao.EMITIR_CERTIFICADO);

        return certificadoRepository.findByPresencaEventoId(eventoId)
                .stream()
                .map(DTOCertificadoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> baixarCertificado(
            Long certificadoId,
            UsuarioAutenticado usuario) {

        validarUsuarioAutenticado(usuario);

        Certificado certificado = certificadoRepository.findById(certificadoId)
                .orElseThrow(() -> new ConflitoException(
                        "Certificado não encontrado"));

        boolean eDono = certificado.getPresenca().getUsuario() != null
                && certificado.getPresenca().getUsuario()
                        .getId().equals(usuario.id());

        Evento evento = certificado.getPresenca().getEvento();

        boolean administrativo = possuiPapelAdministrativo(usuario);

        boolean responsavel = eResponsavelPeloEvento(
                evento, usuario, Permissao.EMITIR_CERTIFICADO);

        if (!eDono && !administrativo && !responsavel) {
            throw new ConflitoException(
                    "Você não tem permissão para baixar este certificado");
        }

        ByteArrayResource arquivo =
                new ByteArrayResource(certificado.getArquivoPdf());

        String nomeArquivo = "certificado-ucsal-" + certificado.getId() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(certificado.getArquivoPdf().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo + "\"")
                .body(arquivo);
    }

    @Transactional
    public int reenviarCertificadosPendentes(
            Long eventoId,
            UsuarioAutenticado usuario) {

        Evento evento = buscarEvento(eventoId);
        validarPermissaoEvento(evento, usuario, Permissao.EMITIR_CERTIFICADO);

        List<Certificado> pendentes =
                certificadoRepository
                        .findByPresencaEventoIdAndEmailEnviadoFalse(eventoId);

        int enviados = 0;

        for (Certificado certificado : pendentes) {
            if (tentarEnviar(certificado)) {
                enviados++;
            }
        }

        return enviados;
    }

    private boolean tentarEnviar(Certificado certificado) {
        try {
            enviarCertificado(certificado);
            certificado.setEmailEnviado(true);
            certificadoRepository.save(certificado);
            return true;
        } catch (Exception e) {
            certificado.setEmailEnviado(false);
            certificadoRepository.save(certificado);
            return false;
        }
    }

    private Evento buscarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ConflitoException(
                        "Evento não encontrado"));
    }

    private void validarUsuarioAutenticado(UsuarioAutenticado usuario) {
        if (usuario == null) {
            throw new ConflitoException(
                    "É necessário estar autenticado");
        }
    }

    private boolean possuiPapelAdministrativo(
            UsuarioAutenticado usuario) {

        return usuario.papeis() != null
                && (usuario.papeis().contains(Papel.ADMIN)
                || usuario.papeis().contains(Papel.CAEX));
    }

    private void validarPermissaoEvento(
            Evento evento,
            UsuarioAutenticado usuario,
            Permissao permissao) {

        validarUsuarioAutenticado(usuario);

        if (!possuiPapelAdministrativo(usuario)
                && !eResponsavelPeloEvento(evento, usuario, permissao)) {
            throw new ConflitoException(
                    "Você não tem permissão para executar esta operação neste evento");
        }
    }

    private boolean eResponsavelPeloEvento(
            Evento evento,
            UsuarioAutenticado usuario,
            Permissao permissao) {

        if (evento.getCriadoPor() != null
                && evento.getCriadoPor().getId().equals(usuario.id())) {
            return true;
        }

        if (evento.getLiga() == null) {
            return false;
        }

        Aluno aluno = alunoRepository.findByUsuarioId(usuario.id())
                .orElse(null);

        if (aluno == null) {
            return false;
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                evento.getLiga().getId(),
                StatusMembro.ATIVO
        ).orElse(null);

        return membro != null
                && membro.getCargo() != null
                && membro.getCargo().getPermissoes() != null
                && membro.getCargo().getPermissoes().contains(permissao);
    }

    private String obterNomeParticipante(Presenca presenca) {
        if (presenca.getUsuario() != null) {
            return presenca.getUsuario().getNome();
        }

        if (presenca.getInscricao() != null
                && presenca.getInscricao().getAluno() != null
                && presenca.getInscricao().getAluno().getUsuario() != null) {
            return presenca.getInscricao()
                    .getAluno()
                    .getUsuario()
                    .getNome();
        }

        return presenca.getNomeParticipanteExterno();
    }

    private String obterEmailParticipante(Presenca presenca) {
        if (presenca.getUsuario() != null) {
            return presenca.getUsuario().getEmail();
        }

        if (presenca.getInscricao() != null
                && presenca.getInscricao().getAluno() != null
                && presenca.getInscricao().getAluno().getUsuario() != null) {
            return presenca.getInscricao()
                    .getAluno()
                    .getUsuario()
                    .getEmail();
        }

        return presenca.getEmailParticipanteExterno();
    }

    private byte[] gerarPdf(String nome, Evento evento) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(documento, output);
            documento.open();

            Font titulo = new Font(Font.HELVETICA, 26, Font.BOLD);
            Font texto = new Font(Font.HELVETICA, 16);
            Font destaque = new Font(Font.HELVETICA, 20, Font.BOLD);

            Paragraph cabecalho = new Paragraph(
                    "CERTIFICADO DE PARTICIPAÇÃO", titulo);
            cabecalho.setAlignment(Element.ALIGN_CENTER);
            cabecalho.setSpacingAfter(50);
            documento.add(cabecalho);

            Paragraph apresentacao = new Paragraph(
                    "Certificamos que", texto);
            apresentacao.setAlignment(Element.ALIGN_CENTER);
            apresentacao.setSpacingAfter(20);
            documento.add(apresentacao);

            Paragraph participante = new Paragraph(nome, destaque);
            participante.setAlignment(Element.ALIGN_CENTER);
            participante.setSpacingAfter(25);
            documento.add(participante);

            Paragraph descricao = new Paragraph(
                    "participou do evento \"" + evento.getTitulo()
                            + "\", realizado em " + formatarData(evento)
                            + ", " + descreverLocal(evento) + ".",
                    texto);
            descricao.setAlignment(Element.ALIGN_CENTER);
            descricao.setSpacingAfter(50);
            documento.add(descricao);

            Paragraph assinatura = new Paragraph(
                    "Central de Eventos UCSal", texto);
            assinatura.setAlignment(Element.ALIGN_CENTER);
            documento.add(assinatura);

            documento.close();
            return output.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o certificado em PDF", e);
        }
    }

    private String descreverLocal(Evento evento) {
        if (evento.getTipoLocal() == null) {
            return "em local informado pela organização";
        }

        return switch (evento.getTipoLocal()) {
            case CAMPUS_PITUACU -> "no Campus Pituaçu da UCSal";
            case ONLINE -> "de forma online";
            case OUTRO -> evento.getLocalDetalhe() != null
                    && !evento.getLocalDetalhe().isBlank()
                    ? "no local " + evento.getLocalDetalhe()
                    : "em local externo";
        };
    }

    private String formatarData(Evento evento) {
        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (evento.getDataInicio() == null) {
            return "data não informada";
        }

        if (evento.getDataFim() == null
                || evento.getDataInicio().equals(evento.getDataFim())) {
            return evento.getDataInicio().format(formato);
        }

        return evento.getDataInicio().format(formato)
                + " até "
                + evento.getDataFim().format(formato);
    }

    private void enviarCertificado(Certificado certificado)
            throws Exception {

        MimeMessage mensagem = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                mensagem, true, "UTF-8");

        helper.setTo(certificado.getEmailDestinatario());
        helper.setSubject("Seu certificado de participação - UCSal");
        helper.setText(
                "Olá, " + certificado.getNomeParticipante() + "!\n\n"
                        + "Seu certificado de participação está em anexo.\n\n"
                        + "Central de Eventos UCSal",
                false);

        helper.addAttachment(
                "certificado-ucsal.pdf",
                new ByteArrayResource(certificado.getArquivoPdf()),
                MediaType.APPLICATION_PDF_VALUE);

        mailSender.send(mensagem);
    }
}