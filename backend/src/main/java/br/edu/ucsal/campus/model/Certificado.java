package br.edu.ucsal.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_certificado_presenca", columnNames = "presenca_id")
})
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presenca_id", nullable = false)
    private Presenca presenca;

    @Column(name = "nome_participante", nullable = false, length = 150)
    private String nomeParticipante;

    @Column(name = "email_destinatario", nullable = false, length = 150)
    private String emailDestinatario;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "arquivo_pdf", nullable = false, columnDefinition = "bytea")
    private byte[] arquivoPdf;

    @Column(name = "email_enviado", nullable = false)
    private boolean emailEnviado = false;

    public Certificado() {
    }

    @PrePersist
    protected void prePersist() {
        if (dataGeracao == null) {
            dataGeracao = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Presenca getPresenca() {
        return presenca;
    }

    public void setPresenca(Presenca presenca) {
        this.presenca = presenca;
    }

    public String getNomeParticipante() {
        return nomeParticipante;
    }

    public void setNomeParticipante(String nomeParticipante) {
        this.nomeParticipante = nomeParticipante;
    }

    public String getEmailDestinatario() {
        return emailDestinatario;
    }

    public void setEmailDestinatario(String emailDestinatario) {
        this.emailDestinatario = emailDestinatario;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public byte[] getArquivoPdf() {
        return arquivoPdf;
    }

    public void setArquivoPdf(byte[] arquivoPdf) {
        this.arquivoPdf = arquivoPdf;
    }

    public boolean isEmailEnviado() {
        return emailEnviado;
    }

    public void setEmailEnviado(boolean emailEnviado) {
        this.emailEnviado = emailEnviado;
    }
}