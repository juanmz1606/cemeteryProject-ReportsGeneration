package com.cemeteryProject.ReportsGeneration.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    public byte[] generarReportePDF(int totalNichos, int totalCuerpos, int totalRelaciones, String usuario) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte General del Cementerio", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // Espacio

            // Información del reporte
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Generado por: " + usuario, bodyFont));
            document.add(new Paragraph("Fecha: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), bodyFont));
            document.add(new Paragraph(" ")); // Espacio

            document.add(new Paragraph("Resumen:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" - Nichos: " + totalNichos, bodyFont));
            document.add(new Paragraph(" - Cuerpos inhumados: " + totalCuerpos, bodyFont));
            document.add(new Paragraph(" - Relaciones Nicho-Cuerpo: " + totalRelaciones, bodyFont));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
