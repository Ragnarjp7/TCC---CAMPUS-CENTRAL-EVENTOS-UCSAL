package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.Certificado;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.Presenca;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.repository.CertificadoRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.PresencaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.mail.internet.MimeMessage;

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
    private final JavaMailSender mailSender;

    public CertificadoService(
            CertificadoRepository certificadoRepository,
            PresencaRepository presencaRepository,
            EventoRepository eventoRepository,
            JavaMailSender mailSender) {

        this.certificadoRepository = certificadoRepository;
        this.presencaRepository = presencaRepository;
        this.eventoRepository = eventoRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public int gerarCertificadosEvento(
            Long eventoId,
            UsuarioAutenticado usuario) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ConflitoException(
                        "Evento não encontrado"));

        validarPermissao(evento, usuario);

        if (evento.getStatus() != Evento.StatusEvento.APROVADO
                && evento.getStatus() != Evento.StatusEvento.ENCERRADO) {
            throw new ConflitoException(
                    "Os certificados só podem ser gerados para eventos aprovados ou encerrados");
        }else if (!evento.isEmiteCertificado()) {
           throw new ConflitoException(
            "Este evento não está configurado para emissão de certificados");
        }

        List<Presenca> presencas = presencaRepository.buscarListaCompletaPorEvento(eventoId);

        int quantidadeGerada = 0;

        for (Presenca presenca : presencas) {

            if (certificadoRepository.existsByPresencaId(presenca.getId())) {
                continue;
            }

            String nome = obterNomeParticipante(presenca);
            String email = obterEmailParticipante(presenca);

            if (email == null || email.isBlank()) {
                continue;
            }

            byte[] pdf = gerarPdf(nome, evento);

            Certificado certificado = new Certificado();
            certificado.setPresenca(presenca);
            certificado.setNomeParticipante(nome);
            certificado.setEmailDestinatario(email);
            certificado.setArquivoPdf(pdf);

            certificadoRepository.save(certificado);

            try {
                enviarCertificado(certificado);
                certificado.setEmailEnviado(true);
                certificadoRepository.save(certificado);
            } catch (Exception e) {
                // O PDF permanece salvo para permitir novo envio posteriormente.
                certificado.setEmailEnviado(false);
            }

            quantidadeGerada++;
        }

        return quantidadeGerada;
    }

    private void validarPermissao(
            Evento evento,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException(
                    "É necessário estar autenticado");
        }

        boolean possuiAcessoAdministrativo = usuario.papeis() != null
                && (usuario.papeis().contains(Papel.ADMIN)
                        || usuario.papeis().contains(Papel.CAEX));

        boolean eCriador = evento.getCriadoPor() != null
                && evento.getCriadoPor().getId().equals(usuario.id());

        if (!possuiAcessoAdministrativo && !eCriador) {
            throw new ConflitoException(
                    "Você não tem permissão para gerar certificados deste evento");
        }
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
                    "CERTIFICADO DE PARTICIPAÇÃO",
                    titulo);
            cabecalho.setAlignment(Element.ALIGN_CENTER);
            cabecalho.setSpacingAfter(50);
            documento.add(cabecalho);

            Paragraph apresentacao = new Paragraph(
                    "Certificamos que",
                    texto);
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
                    "Central de Eventos UCSal",
                    texto);
            assinatura.setAlignment(Element.ALIGN_CENTER);
            documento.add(assinatura);

            documento.close();

            return output.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o certificado em PDF",
                    e);
        }
    }

    private String descreverLocal(Evento evento) {

        if (evento.getTipoLocal() == null) {
            return "";
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
        
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        if (evento.getDataInicio().equals(evento.getDataFim())) {
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
                mensagem,
                true,
                "UTF-8");

        helper.setTo(certificado.getEmailDestinatario());
        helper.setSubject("Seu certificado de participação - UCSal");
        helper.setText(
                "Olá, " + certificado.getNomeParticipante()
                        + "!\n\n"
                        + "Seu certificado de participação está em anexo.\n\n"
                        + "Central de Eventos UCSal",
                false);

        helper.addAttachment(
                "certificado-ucsal.pdf",
                () -> new java.io.ByteArrayInputStream(
                        certificado.getArquivoPdf()),
                "application/pdf");

        mailSender.send(mensagem);
    }
}
