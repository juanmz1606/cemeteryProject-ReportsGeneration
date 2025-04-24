package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfGeneratorService {

    public byte[] generarReportePDFConAnalisis(ReporteAnalisisDTO analisis) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte Analítico del Cementerio", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Información del reporte
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Generado por: " + analisis.getUsuario(), bodyFont));
            document.add(new Paragraph("Fecha: " + analisis.getFechaGeneracion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), bodyFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen General:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" - Total de nichos: " + analisis.getTotalNichos(), bodyFont));
            document.add(new Paragraph(" - Total de cuerpos inhumados: " + analisis.getTotalCuerpos(), bodyFont));
            document.add(new Paragraph(" - Ocupación: " + String.format("%.2f", analisis.getPorcentajeOcupacion()) + "%", bodyFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Promedios Mensuales:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" - Promedio general de ingresos: " + String.format("%.2f", analisis.getPromedioMensualGeneral()), bodyFont));

            // Promedios por tipo
            document.add(new Paragraph(" - Promedio por tipo de cuerpo:", bodyFont));
            for (Map.Entry<String, Double> entry : analisis.getPromedioMensualPorTipo().entrySet()) {
                document.add(new Paragraph("    • " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()), bodyFont));
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
