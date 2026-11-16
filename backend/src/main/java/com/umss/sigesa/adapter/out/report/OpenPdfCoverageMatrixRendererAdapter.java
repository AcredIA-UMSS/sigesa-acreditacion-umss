package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.umss.sigesa.application.port.out.CoverageMatrixPdfPort;
import com.umss.sigesa.domain.exception.ReportTemplateException;
import com.umss.sigesa.domain.model.CoverageMatrix;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class OpenPdfCoverageMatrixRendererAdapter implements CoverageMatrixPdfPort {

    @Override
    public byte[] render(CoverageMatrix matrix) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(
                    new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth()), 36, 36, 42, 36);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            document.add(new Paragraph("Universidad Mayor de San Simón — SIGESA", titleFont));
            document.add(new Paragraph("Informe de autoevaluación — matriz de cobertura", titleFont));
            document.add(new Paragraph(
                    "Carrera: " + nullToDash(matrix.careerName())
                            + "    Generado: "
                            + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                                    matrix.generatedAt().atZone(ZoneId.of("America/La_Paz")).toLocalDateTime()),
                    metaFont));
            CoverageMatrix.Totals totals = matrix.totals();
            document.add(new Paragraph(
                    "Indicadores: " + totals.indicators()
                            + "  | Con evidencia: " + totals.withEvidence()
                            + "  | Aprobados: " + totals.approved()
                            + "  | Observados: " + totals.observed()
                            + "  | Cobertura: " + totals.coveragePercent() + "%",
                    metaFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[] {18, 18, 22, 12, 10, 8});
            table.setWidthPercentage(100);
            addHeader(table, headerFont, "Dimensión (N1)", "Componente (N2)", "Criterio (N3)",
                    "Indicador", "Estado", "Evidencias");

            if (matrix.dimensions() != null) {
                for (CoverageMatrix.DimensionNode dimension : matrix.dimensions()) {
                    for (CoverageMatrix.ComponentNode component : dimension.components()) {
                        for (CoverageMatrix.CriterionNode criterion : component.criteria()) {
                            for (CoverageMatrix.IndicatorRow indicator : criterion.indicators()) {
                                addCell(table, cellFont, dimension.name());
                                addCell(table, cellFont, component.name());
                                addCell(table, cellFont, criterion.name());
                                addCell(table, cellFont, indicator.code());
                                addCell(table, cellFont, indicator.status());
                                addCell(table, cellFont, String.valueOf(indicator.evidenceCount()));
                            }
                        }
                    }
                }
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new ReportTemplateException("No se pudo generar el PDF de cobertura", ex);
        }
    }

    private static void addHeader(PdfPTable table, Font font, String... labels) {
        for (String label : labels) {
            PdfPCell cell = new PdfPCell(new Phrase(label, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private static void addCell(PdfPTable table, Font font, String value) {
        table.addCell(new Phrase(value != null ? value : "—", font));
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
