package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.umss.sigesa.application.port.out.PdfRendererPort;
import com.umss.sigesa.domain.exception.ReportTemplateException;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OpenPdfRendererAdapter implements PdfRendererPort {

    private static final String INSTITUTION_HEADER =
            "Universidad Mayor de San Simón — SIGESA";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] render(ExecutiveReportSnapshot snapshot) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph(INSTITUTION_HEADER, titleFont));
            document.add(new Paragraph("Reporte Ejecutivo de Acreditación", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Generado: " + TIMESTAMP_FORMAT.format(snapshot.generatedAt()), bodyFont));
            document.add(new Paragraph(
                    "Gestión: " + snapshot.filters().managementYear(), bodyFont));
            document.add(new Paragraph(
                    "Facultad: " + formatOptional(snapshot.filters().facultyId()), bodyFont));
            document.add(new Paragraph(
                    "Programa: " + formatOptional(snapshot.filters().programId()), bodyFont));
            document.add(new Paragraph(" "));

            if (snapshot.programs().isEmpty()) {
                document.add(new Paragraph(
                        "No hay datos agregados para el alcance seleccionado.", bodyFont));
            } else {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addHeader(table, "Programa", headerFont);
                addHeader(table, "Semáforo", headerFont);
                addHeader(table, "Indicadores", headerFont);
                addHeader(table, "Aprobados", headerFont);
                addHeader(table, "Avance %", headerFont);

                for (ExecutiveReportSnapshot.ProgramSummary program : snapshot.programs()) {
                    int progress = program.totalIndicators() == 0
                            ? 0
                            : (program.approvedIndicators() * 100) / program.totalIndicators();
                    table.addCell(cell(program.programName(), bodyFont));
                    table.addCell(cell(program.semaphore(), bodyFont));
                    table.addCell(cell(String.valueOf(program.totalIndicators()), bodyFont));
                    table.addCell(cell(String.valueOf(program.approvedIndicators()), bodyFont));
                    table.addCell(cell(progress + "%", bodyFont));
                }
                document.add(table);
            }

            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new ReportTemplateException("Failed to render executive PDF", ex);
        }
    }

    private static String formatOptional(UUID value) {
        return value == null ? "Todos" : value.toString();
    }

    private static void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static PdfPCell cell(String text, Font font) {
        return new PdfPCell(new Phrase(text, font));
    }
}
