package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.umss.sigesa.application.port.out.DraftingDocumentRendererPort;
import com.umss.sigesa.domain.exception.ReportTemplateException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class OpenPdfDraftingDocumentRendererAdapter implements DraftingDocumentRendererPort {

    static final String DRAFT_LEGEND =
            "Borrador institucional SIGESA — no es plantilla CNACU/CEUB oficial";

    @Override
    public byte[] render(String title, List<String> paragraphs) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font legendFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Universidad Mayor de San Simón — SIGESA", titleFont));
            document.add(new Paragraph(title != null ? title : "Borrador E4", titleFont));
            document.add(new Paragraph(DRAFT_LEGEND, legendFont));
            document.add(new Paragraph(" "));

            if (paragraphs != null) {
                for (String paragraph : paragraphs) {
                    document.add(new Paragraph(paragraph != null ? paragraph : "", bodyFont));
                }
            }

            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new ReportTemplateException("Failed to render drafting PDF", ex);
        }
    }
}
