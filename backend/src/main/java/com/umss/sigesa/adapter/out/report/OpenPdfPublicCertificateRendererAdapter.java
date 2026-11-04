package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.umss.sigesa.application.port.out.PublicCertificatePdfRendererPort;
import com.umss.sigesa.domain.model.PublicProgramSnapshot;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
public class OpenPdfPublicCertificateRendererAdapter implements PublicCertificatePdfRendererPort {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public byte[] render(PublicProgramSnapshot snapshot) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 64, 48);
            com.lowagie.text.pdf.PdfWriter.getInstance(document, output);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Universidad Mayor de San Simón — DUEA", title));
            document.add(new Paragraph("Certificado oficial de acreditación", heading));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Carrera: " + snapshot.programName(), body));
            document.add(new Paragraph("Estado: " + snapshot.accreditationStatus(), body));
            document.add(new Paragraph("Vigencia desde: " + DATE.format(snapshot.validFrom()), body));
            document.add(new Paragraph("Vigencia hasta: " + DATE.format(snapshot.validUntil()), body));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Documento generado por SIGESA. No constituye un archivo almacenado ni una URL pública.",
                    body));

            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el certificado PDF", ex);
        }
    }
}
