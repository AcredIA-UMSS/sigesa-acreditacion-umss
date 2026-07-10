package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportFormat;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

@Component
public class OpenPdfReportStreamingAdapter implements ReportGeneratorPort {

    @Override
    public boolean supports(ReportFormat format) {
        return format == ReportFormat.PDF;
    }

    @Override
    public File generateReport(Stream<ObservationSummary> dataStream, ReportFormat format) {
        try {
            File tempFile = File.createTempFile("sigesa_report_", ".pdf");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
                PdfWriter.getInstance(document, out);
                document.open();

                // Title Section
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
                Paragraph title = new Paragraph("SIGESA - Reporte de Observaciones de Acreditación", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Table setup (8 columns matching Excel/CSV headers)
                // Headers: "Observation ID", "Indicator", "Code", "Title", "Description", "Issue Date", "Due Date", "Status"
                float[] columnWidths = {2f, 2f, 1.5f, 3f, 4f, 2f, 2f, 1.5f};
                PdfPTable table = new PdfPTable(columnWidths);
                table.setWidthPercentage(100);

                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
                Color headerColor = new Color(23, 37, 90); // primary-800 equivalent

                String[] headers = {"Obs ID", "Indicador ID", "Código", "Título Indicador", "Descripción", "F. Emisión", "F. Límite", "Estado"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(headerColor);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(6);
                    table.addCell(cell);
                }

                Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
                dataStream.forEach(obs -> {
                    String obsId = obs.observationId() != null ? obs.observationId() : "";
                    String displayObsId = obsId.length() > 8 ? obsId.substring(0, 8) : obsId;
                    table.addCell(createCell(displayObsId, bodyFont));

                    String indId = obs.indicatorId() != null ? obs.indicatorId() : "";
                    String displayIndId = indId.length() > 8 ? indId.substring(0, 8) : indId;
                    table.addCell(createCell(displayIndId, bodyFont));

                    table.addCell(createCell(obs.indicatorCode() != null ? obs.indicatorCode() : "", bodyFont));
                    table.addCell(createCell(obs.indicatorTitle() != null ? obs.indicatorTitle() : "", bodyFont));
                    table.addCell(createCell(obs.description() != null ? obs.description() : "", bodyFont));
                    table.addCell(createCell(obs.issueDate() != null ? obs.issueDate().toString() : "", bodyFont));
                    table.addCell(createCell(obs.dueDate() != null ? obs.dueDate().toString() : "", bodyFont));
                    table.addCell(createCell(obs.status() != null ? obs.status() : "", bodyFont));
                });

                document.add(table);
                document.close();
            }

            return tempFile;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error generating PDF streaming report", e);
        }
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}
