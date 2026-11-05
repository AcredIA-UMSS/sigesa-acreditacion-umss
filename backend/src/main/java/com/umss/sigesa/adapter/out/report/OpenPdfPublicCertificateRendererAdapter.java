package com.umss.sigesa.adapter.out.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.umss.sigesa.application.port.out.PublicCertificatePdfRendererPort;
import com.umss.sigesa.domain.model.PublicCertificateText;
import com.umss.sigesa.domain.model.PublicProgramSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

@Component
public class OpenPdfPublicCertificateRendererAdapter implements PublicCertificatePdfRendererPort {

    static final Color UMSS_BLUE = new Color(0x00, 0x37, 0x70);
    static final Color GOLD = new Color(0xC9, 0x96, 0x2B);

    private static final float MARGIN = 40f;
    private static final String DEFAULT_BASE_URL = "http://localhost:5173";

    private final String publicBaseUrl;
    private final BaseFont serif;
    private final BaseFont serifBold;
    private final BaseFont sans;
    private final BaseFont sansBold;
    private final Image umssLogo;
    private final Image dueaLogo;

    public OpenPdfPublicCertificateRendererAdapter(
            @Value("${sigesa.public.base-url:" + DEFAULT_BASE_URL + "}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl == null || publicBaseUrl.isBlank() ? DEFAULT_BASE_URL : publicBaseUrl.trim();
        this.serif = loadFont("/public-certificate/fonts/DejaVuSerif.ttf");
        this.serifBold = loadFont("/public-certificate/fonts/DejaVuSerif-Bold.ttf");
        this.sans = loadFont("/public-certificate/fonts/DejaVuSans.ttf");
        this.sansBold = loadFont("/public-certificate/fonts/DejaVuSans-Bold.ttf");
        this.umssLogo = loadImage("/public-certificate/logo-umss.png");
        this.dueaLogo = loadImage("/public-certificate/logo-duea.png");
    }

    @Override
    public byte[] render(PublicProgramSnapshot snapshot) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth()), MARGIN, MARGIN, MARGIN, MARGIN);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.addTitle("Certificado de acreditación — " + snapshot.programName());
            document.addSubject("Vigencia " + snapshot.validFrom() + " / " + snapshot.validUntil());
            document.addKeywords("vigencia-desde=" + snapshot.validFrom() + ";vigencia-hasta=" + snapshot.validUntil());
            document.addCreator("SIGESA — DUEA UMSS");
            document.open();

            drawDoubleBorder(writer, document);

            document.add(headerTable());
            document.add(accentBar());
            document.add(titleBlock());
            document.add(bodyBlock(snapshot));
            document.add(footerTable(snapshot));

            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el certificado PDF", ex);
        }
    }

    static String publicStatusUrl(String baseUrl, String slug) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + "/public/status?slug=" + slug;
    }

    private PdfPTable headerTable() {
        PdfPTable table = new PdfPTable(new float[] {1.1f, 5f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(6);

        Image logo = Image.getInstance(umssLogo);
        logo.scaleToFit(62, 78);
        PdfPCell logoCell = imageCell(logo, Element.ALIGN_LEFT);
        logoCell.setPaddingRight(10);
        table.addCell(logoCell);

        PdfPCell text = new PdfPCell();
        text.setBorder(Rectangle.NO_BORDER);
        text.setVerticalAlignment(Element.ALIGN_MIDDLE);
        text.addElement(new Paragraph("Universidad Mayor de San Simón", new Font(serifBold, 16, Font.NORMAL, UMSS_BLUE)));
        Paragraph duea = new Paragraph(
                "Dirección Universitaria de Evaluación y Acreditación",
                new Font(sans, 11, Font.NORMAL, UMSS_BLUE));
        duea.setSpacingBefore(3);
        text.addElement(duea);
        table.addCell(text);
        return table;
    }

    private PdfPTable accentBar() {
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100);
        bar.setSpacingBefore(2);
        bar.setSpacingAfter(4);
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(3.2f);
        cell.setBackgroundColor(GOLD);
        cell.setBorder(Rectangle.NO_BORDER);
        bar.addCell(cell);
        return bar;
    }

    private Paragraph titleBlock() {
        Paragraph title = new Paragraph(
                "CERTIFICADO DE ACREDITACIÓN",
                new Font(serifBold, 26, Font.NORMAL, UMSS_BLUE));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(8);
        title.setSpacingAfter(16);
        return title;
    }

    private Paragraph bodyBlock(PublicProgramSnapshot snapshot) {
        Paragraph body = new Paragraph(
                PublicCertificateText.certificationSentence(snapshot),
                new Font(sans, 13, Font.NORMAL, Color.DARK_GRAY));
        body.setAlignment(Element.ALIGN_JUSTIFIED);
        body.setLeading(20);
        body.setSpacingAfter(28);
        return body;
    }

    private PdfPTable footerTable(PublicProgramSnapshot snapshot) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] {1.1f, 3.6f, 1.6f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);

        Image logo = Image.getInstance(dueaLogo);
        logo.scaleToFit(56, 56);
        table.addCell(imageCell(logo, Element.ALIGN_LEFT));
        table.addCell(signatureCell());
        table.addCell(qrCell(snapshot));
        return table;
    }

    private PdfPCell signatureCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingLeft(16);
        cell.setPaddingRight(16);

        Paragraph line = new Paragraph("______________________________", new Font(sans, 11, Font.NORMAL, UMSS_BLUE));
        line.setAlignment(Element.ALIGN_CENTER);
        Paragraph label = new Paragraph("Firma y sello", new Font(sansBold, 9, Font.NORMAL, UMSS_BLUE));
        label.setAlignment(Element.ALIGN_CENTER);
        label.setSpacingBefore(4);
        Paragraph stamp = new Paragraph("Espacio para sello", new Font(sans, 8, Font.ITALIC, Color.GRAY));
        stamp.setAlignment(Element.ALIGN_CENTER);
        stamp.setSpacingBefore(10);

        cell.addElement(line);
        cell.addElement(label);
        cell.addElement(stamp);
        return cell;
    }

    private PdfPCell qrCell(PublicProgramSnapshot snapshot) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String url = publicStatusUrl(publicBaseUrl, snapshot.slug());
        Image qr = qrImage(url);
        qr.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(qr);

        Paragraph code = new Paragraph(
                "Código de verificación",
                new Font(sansBold, 7, Font.NORMAL, UMSS_BLUE));
        code.setAlignment(Element.ALIGN_RIGHT);
        code.setSpacingBefore(4);
        Paragraph uuid = new Paragraph(snapshot.id().toString(), new Font(sans, 6.5f, Font.NORMAL, Color.DARK_GRAY));
        uuid.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(code);
        cell.addElement(uuid);
        return cell;
    }

    private static Image qrImage(String url) {
        try {
            var hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 1
            );
            var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 240, 240, hints);
            BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            int black = Color.BLACK.getRGB();
            int white = Color.WHITE.getRGB();
            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    image.setRGB(x, y, matrix.get(x, y) ? black : white);
                }
            }
            Image pdfImage = Image.getInstance(image, null);
            pdfImage.scaleAbsolute(78, 78);
            return pdfImage;
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el QR del certificado", ex);
        }
    }

    private static PdfPCell imageCell(Image image, int alignment) {
        PdfPCell cell = new PdfPCell(image, false);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static void drawDoubleBorder(PdfWriter writer, Document document) {
        Rectangle page = document.getPageSize();
        PdfContentByte canvas = writer.getDirectContent();

        canvas.setColorStroke(UMSS_BLUE);
        canvas.setLineWidth(2.4f);
        canvas.rectangle(22, 22, page.getWidth() - 44, page.getHeight() - 44);
        canvas.stroke();

        canvas.setColorStroke(GOLD);
        canvas.setLineWidth(1.1f);
        canvas.rectangle(28, 28, page.getWidth() - 56, page.getHeight() - 56);
        canvas.stroke();
    }

    private static BaseFont loadFont(String classpath) {
        byte[] data = readClasspath(classpath);
        try {
            return BaseFont.createFont(classpath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, data, null);
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("No se pudo embeber la fuente " + classpath, ex);
        }
    }

    private static Image loadImage(String classpath) {
        try {
            return Image.getInstance(readClasspath(classpath));
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo cargar " + classpath, ex);
        }
    }

    private static byte[] readClasspath(String classpath) {
        try (InputStream stream = OpenPdfPublicCertificateRendererAdapter.class.getResourceAsStream(classpath)) {
            if (stream == null) {
                throw new IllegalStateException("Recurso ausente: " + classpath);
            }
            return stream.readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
