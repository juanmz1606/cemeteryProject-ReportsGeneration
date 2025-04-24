package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import com.lowagie.text.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfGeneratorService {

    public byte[] generarReportePDFConAnalisis(ReporteAnalisisDTO analisis) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte del Cementerio", titleFont);
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
            document.add(new Paragraph(" - Total de cuerpos: " + analisis.getTotalCuerpos(), bodyFont));
            document.add(new Paragraph(" - Ocupación: " + String.format("%.2f", analisis.getPorcentajeOcupacion()) + "%", bodyFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Promedios Mensuales:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" - Promedio general de ingresos: " + String.format("%.2f", analisis.getPromedioMensualGeneral()), bodyFont));

            document.add(new Paragraph(" - Promedio por tipo de cuerpo:", bodyFont));
            for (Map.Entry<String, Double> entry : analisis.getPromedioMensualPorTipo().entrySet()) {
                document.add(new Paragraph("    • " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()), bodyFont));
            }

            document.add(new Paragraph(" "));

            // Gráfico de torta (estado de nichos)
            DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
            analisis.getEstadoNichos().forEach(pieDataset::setValue);
            JFreeChart pieChart = ChartFactory.createPieChart(
                "Estado de los Nichos", 
                pieDataset, 
                true,   // Mostrar leyenda
                true,   // Mostrar tooltips
                false   // No URLs
            );
            addChartToDocument(document, pieChart);

            // Gráfico de barras (tipos de cuerpo)
            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            analisis.getCuerposPorTipo().forEach((tipo, cantidad) -> 
                barDataset.addValue(cantidad, "Cantidad", tipo.toString())
            );
            JFreeChart barChart = ChartFactory.createBarChart(
                "Cuerpos por Tipo",   // Título
                "Tipo",               // Etiqueta eje X
                "Cantidad",           // Etiqueta eje Y
                barDataset,
                PlotOrientation.VERTICAL,
                false,                // No incluir leyenda
                true,                 // Tooltips
                false                 // URLs
            );
            addChartToDocument(document, barChart);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addChartToDocument(Document document, JFreeChart chart) throws Exception {
        // Personalizar la apariencia general de los gráficos
        chart.setBackgroundPaint(new java.awt.Color(240, 240, 240));
        chart.setBorderPaint(java.awt.Color.WHITE);
        chart.setBorderStroke(new java.awt.BasicStroke(2.0f));
        chart.setBorderVisible(true);
        
        // Personalizar según el tipo de gráfico
        if (chart.getPlot() instanceof org.jfree.chart.plot.PiePlot) {
            // Usar PiePlot<?> para manejar el genérico
            org.jfree.chart.plot.PiePlot<?> piePlot = (org.jfree.chart.plot.PiePlot<?>) chart.getPlot();
            piePlot.setBackgroundPaint(java.awt.Color.WHITE);
            piePlot.setOutlineVisible(false);
            
            // Habilitar etiquetas simples y mostrarlas dentro de las secciones
            piePlot.setSimpleLabels(true); // Usa etiquetas simples para evitar líneas de conexión
            piePlot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            piePlot.setLabelGap(0.02);
            // Eliminar fondo de las etiquetas para que queden directamente sobre el color de la torta
            piePlot.setLabelBackgroundPaint(null);
            piePlot.setLabelOutlinePaint(null);
            piePlot.setLabelShadowPaint(null);
            piePlot.setLabelPaint(java.awt.Color.BLACK);

            // Configurar etiquetas dentro de las secciones (valor y porcentaje)
            piePlot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                    "{1} ({2})", // Formato: valor (porcentaje), por ejemplo, "1 (16.7%)"
                    new java.text.DecimalFormat("0"), // Formato para el valor
                    new java.text.DecimalFormat("0.0%") // Formato para el porcentaje
            ));

            // Asignar colores específicos a cada estado
            piePlot.setSectionPaint("DISPONIBLE", new java.awt.Color(46, 204, 113));  // Verde más suave
            piePlot.setSectionPaint("OCUPADO", new java.awt.Color(255, 28, 14));    // Naranja muted
            piePlot.setSectionPaint("MANTENIMIENTO", new java.awt.Color(52, 152, 219)); // Azul más claro
                    
            // Personalizar la leyenda para mostrar cantidades
            chart.getLegend().setItemFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            chart.getLegend().setBackgroundPaint(new java.awt.Color(240, 240, 240));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(java.awt.Color.WHITE));
        }
        else if (chart.getPlot() instanceof org.jfree.chart.plot.CategoryPlot) {
            org.jfree.chart.plot.CategoryPlot categoryPlot = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
            categoryPlot.setBackgroundPaint(java.awt.Color.WHITE);
            categoryPlot.setOutlinePaint(java.awt.Color.WHITE);
            categoryPlot.setRangeGridlinePaint(new java.awt.Color(220, 220, 220));
            categoryPlot.setDomainGridlinesVisible(false);
            
            // Personalizar el eje Y para mostrar solo valores enteros
            org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) categoryPlot.getRangeAxis();
            rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
            
            // Crear un renderer personalizado para asignar colores diferentes a cada barra
            BarRenderer customRenderer = new BarRenderer() {
                @Override
                public java.awt.Paint getItemPaint(int row, int column) {
                    // Asignar colores diferentes según la columna (categoría)
                    if (column == 0) { // INHUMADO
                        return new java.awt.Color(79, 129, 189); // Azul
                    } else { // EXHUMADO
                        return new java.awt.Color(255, 99, 71); // Rojo coral
                    }
                }
            };
            
            // Personalizar el renderer para barras más modernas
            customRenderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
            customRenderer.setShadowVisible(false);

            // Agregar etiquetas a las barras con valores
            customRenderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
            customRenderer.setDefaultItemLabelsVisible(true);
            customRenderer.setDefaultItemLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));

            // Asignar el renderer personalizado al gráfico
            categoryPlot.setRenderer(customRenderer);
        }
        
        // Fuentes modernas para títulos
        chart.getTitle().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        
        // Crear y añadir la imagen al documento
        BufferedImage image = chart.createBufferedImage(500, 350);
        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(chartOut, image);
        Image chartImage = Image.getInstance(chartOut.toByteArray());
        chartImage.setAlignment(Image.ALIGN_CENTER);
        document.add(chartImage);
        document.add(new Paragraph(" "));  // Espacio después de cada gráfico
    }
    
}