package com.app.Utils.pdf;

import com.app.Model.domain.Pawn;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import com.app.Utils.CurrencyUtils;
import com.app.Infrastructure.logging.LoggerFactory;
import org.slf4j.Logger;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class PdfInvoiceGenerator {
    private static final Logger log = LoggerFactory.getLogger(PdfInvoiceGenerator.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void generateSaleInvoice(Sale sale) {
        if (sale == null) return;
        try {
            File dir = new File("invoices");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "invoices/Factura_Venta_" + sale.getId() + ".pdf";
            File file = new File(filename);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Encabezado
            Paragraph title = new Paragraph("SISTEMA COMPRAVENTA - FACTURA DE VENTA")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("No. Venta: " + sale.getId()));
            document.add(new Paragraph("Fecha: " + (sale.getSaleDate() != null ? sale.getSaleDate().format(FMT) : "N/A")));
            document.add(new Paragraph("Comprador: " + (sale.getClienteNombreAnon() != null ? sale.getClienteNombreAnon() : "Cliente ID " + sale.getClienteId())));
            document.add(new Paragraph("\n"));

            // Tabla de detalles
            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 45, 15, 25})).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Artículo").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Cant.").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold()));

            if (sale.getDetails() != null) {
                for (SalesDetail d : sale.getDetails()) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(d.getArticleId()))));
                    table.addCell(new Cell().add(new Paragraph("Artículo ID " + d.getArticleId())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(d.getAmount()))));
                    table.addCell(new Cell().add(new Paragraph(CurrencyUtils.format(d.getSubtotal()))));
                }
            }
            document.add(table);
            document.add(new Paragraph("\n"));

            Paragraph total = new Paragraph("TOTAL: " + CurrencyUtils.format(sale.getTotal()))
                    .setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT);
            document.add(total);

            document.close();
            log.info("Factura de venta PDF generada exitosamente en: {}", filename);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            log.error("Error al generar factura de venta PDF", e);
        }
    }

    public static void generatePawnInvoice(Pawn pawn) {
        if (pawn == null) return;
        try {
            File dir = new File("invoices");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "invoices/Boleta_Empeno_" + pawn.getId() + ".pdf";
            File file = new File(filename);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph title = new Paragraph("SISTEMA COMPRAVENTA - BOLETA DE EMPEÑO")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("No. Empeño: " + pawn.getId()));
            document.add(new Paragraph("Cliente: " + (pawn.getClientName() != null ? pawn.getClientName() : "ID " + pawn.getClientId())));
            document.add(new Paragraph("Artículo: " + (pawn.getArticleName() != null ? pawn.getArticleName() : "ID " + pawn.getArticleId())));
            document.add(new Paragraph("Fecha Ingreso: " + (pawn.getPawnDate() != null ? pawn.getPawnDate().toString() : "N/A")));
            document.add(new Paragraph("Fecha Vencimiento: " + (pawn.getReturnDate() != null ? pawn.getReturnDate().toString() : "N/A")));
            document.add(new Paragraph("Cuotas Pactadas: " + pawn.getInstallmentCount()));
            document.add(new Paragraph("Préstamo Total: " + CurrencyUtils.format(pawn.getTotal())));
            document.add(new Paragraph("\n"));

            Paragraph warning = new Paragraph("IMPORTANTE: Recuerde realizar el pago puntual de sus cuotas para evitar el vencimiento y pérdida del artículo.")
                    .setItalic().setFontSize(10).setTextAlignment(TextAlignment.CENTER);
            document.add(warning);

            document.close();
            log.info("Boleta de empeño PDF generada exitosamente en: {}", filename);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            log.error("Error al generar boleta de empeño PDF", e);
        }
    }
}
