package com.cemeteryProject.ReportsGeneration.services;

import com.cemeteryProject.ReportsGeneration.dtos.CuerpoInhumadoDTO;
import com.cemeteryProject.ReportsGeneration.dtos.ReporteAnalisisDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
            Paragraph title = new Paragraph("Reporte del Cementerio", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Información del reporte
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Generado por: " + analisis.getUsuario(), bodyFont));
            document.add(new Paragraph("Fecha: " + analisis.getFechaGeneracion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), bodyFont));

            // Resumen General
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen General:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" - Total de nichos: " + analisis.getTotalNichos(), bodyFont));
            document.add(new Paragraph(" - Total de cuerpos: " + analisis.getTotalCuerpos(), bodyFont));
            document.add(new Paragraph(" - Cuerpos asignados: " + analisis.getCuerposAsignados(), bodyFont));
            document.add(new Paragraph(" - Ocupación: " + String.format("%.2f", analisis.getPorcentajeOcupacion()) + "%", bodyFont));
            document.add(new Paragraph(" - Nichos disponibles: " + analisis.getNichosDisponibles(), bodyFont));
            document.add(new Paragraph(" - Cuerpos inhumados (último mes): " + analisis.getCuerposRecientes(), bodyFont));

            // Promedios Mensuales
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Promedios Mensuales:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" - Promedio general de ingresos: " + String.format("%.2f", analisis.getPromedioMensualGeneral()), bodyFont));
            document.add(new Paragraph(" - Promedio por tipo de cuerpo:", bodyFont));
            for (Map.Entry<String, Double> entry : analisis.getPromedioMensualPorTipo().entrySet()) {
                document.add(new Paragraph("    • " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()), bodyFont));
            }

            // Gráfico de torta (estado de nichos)
            document.add(new Paragraph(" "));
            DefaultPieDataset<String> pieDatasetNichos = new DefaultPieDataset<>();
            analisis.getEstadoNichos().forEach(pieDatasetNichos::setValue);
            JFreeChart pieChartNichos = ChartFactory.createPieChart(
                "Estado de los Nichos",
                pieDatasetNichos,
                true,
                true,
                false
            );
            addChartToDocument(document, pieChartNichos, "pie");

            // Gráfico de barras (tipos de cuerpo)
            document.add(new Paragraph(" "));
            DefaultCategoryDataset barDatasetCuerpos = new DefaultCategoryDataset();
            analisis.getCuerposPorTipo().forEach((tipo, cantidad) ->
                barDatasetCuerpos.addValue(cantidad, "Cantidad", tipo.toString())
            );
            JFreeChart barChartCuerpos = ChartFactory.createBarChart(
                "Cuerpos por Tipo",
                "Tipo",
                "Cantidad",
                barDatasetCuerpos,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
            );
            addChartToDocument(document, barChartCuerpos, "bar");

            // Gráfico de torta (distribución de cuerpos: asignados, no asignados)
            document.add(new Paragraph(" "));
            DefaultPieDataset<String> pieDatasetCuerpos = new DefaultPieDataset<>();
            analisis.getCuerposAsignadosDistribucion().forEach(pieDatasetCuerpos::setValue);
            JFreeChart pieChartCuerpos = ChartFactory.createPieChart(
                "Distribución de Cuerpos",
                pieDatasetCuerpos,
                true,
                true,
                false
            );
            addChartToDocument(document, pieChartCuerpos, "pie");

            // Gráfico de líneas (tendencia de inhumaciones)
            document.add(new Paragraph(" "));
            DefaultCategoryDataset lineDatasetInhumations = new DefaultCategoryDataset();
            analisis.getWeeklyInhumations().forEach(data ->
                lineDatasetInhumations.addValue(data.getCount(), "Inhumaciones", data.getWeek())
            );
            JFreeChart lineChartInhumations = ChartFactory.createLineChart(
                "Tendencia de Inhumaciones (Últimas 12 Semanas)",
                "Semana",
                "Inhumaciones",
                lineDatasetInhumations,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
            );
            addChartToDocument(document, lineChartInhumations, "line");

            // Gráfico de barras (tipos de documentos)
            document.add(new Paragraph(" "));
            DefaultCategoryDataset barDatasetDocs = new DefaultCategoryDataset();
            analisis.getDocumentTypes().forEach((tipo, cantidad) ->
                barDatasetDocs.addValue(cantidad, "Cantidad", tipo)
            );
            JFreeChart barChartDocs = ChartFactory.createBarChart(
                "Tipos de Documentos",
                "Tipo",
                "Cantidad",
                barDatasetDocs,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
            );
            addChartToDocument(document, barChartDocs, "bar");

            // Gráfico de líneas (tendencia de generación de documentos)
            document.add(new Paragraph(" "));
            DefaultCategoryDataset lineDatasetDocs = new DefaultCategoryDataset();
            analisis.getWeeklyDocuments().forEach(data ->
                lineDatasetDocs.addValue(data.getCount(), "Documentos", data.getWeek())
            );
            JFreeChart lineChartDocs = ChartFactory.createLineChart(
                "Tendencia de Generación de Documentos (Últimas 12 Semanas)",
                "Semana",
                "Documentos",
                lineDatasetDocs,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
            );
            addChartToDocument(document, lineChartDocs, "line");

            // Top 3 usuarios con más documentos
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Top 3 Usuarios con Más Documentos:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            if (analisis.getTopUsers().isEmpty()) {
                document.add(new Paragraph("No hay datos de usuarios disponibles para mostrar", bodyFont));
            } else {
                for (int i = 0; i < analisis.getTopUsers().size(); i++) {
                    ReporteAnalisisDTO.TopUser topUser = analisis.getTopUsers().get(i);
                    String position = (i == 0) ? "1°" : (i == 1) ? "2°" : "3°";
                    document.add(new Paragraph(String.format(" - %s ID: %s: %d documentos", position, topUser.getUsuarioId(), topUser.getCount()), bodyFont));
                }
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] generarReportePDFCuerpos(List<CuerpoInhumadoDTO> cuerpoList) {
        try {
            if (cuerpoList == null || cuerpoList.isEmpty()) {
                throw new IllegalArgumentException("No se proporcionaron datos de cuerpos para generar el reporte.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Lista de Cuerpos Registrados", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Fecha de generación
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph date = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dateFont);
            date.setAlignment(Paragraph.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Formateadores para fechas
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Iterar sobre cada cuerpo y crear un bloque
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            int index = 1;

            for (CuerpoInhumadoDTO cuerpo : cuerpoList) {
                // Título del bloque: "Cuerpo #N - ID: [idCadaver]"
                Paragraph sectionTitle = new Paragraph("Cuerpo #" + index + " - ID: " + (cuerpo.getIdCadaver() != null ? cuerpo.getIdCadaver() : "N/A"), sectionTitleFont);
                sectionTitle.setSpacingBefore(20);
                sectionTitle.setSpacingAfter(10);
                document.add(sectionTitle);

                // Crear una tabla de 2 columnas para organizar los atributos
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1, 1}); // Columnas de igual ancho

                // Lista de atributos a mostrar
                String[][] attributes = {
                    {"Nombre", cuerpo.getNombre() != null ? cuerpo.getNombre() + " " + (cuerpo.getApellido() != null ? cuerpo.getApellido() : "") : "N/A"},
                    {"Documento", cuerpo.getDocumentoIdentidad() != null ? cuerpo.getDocumentoIdentidad() : "N/A"},
                    {"Protocolo Necropsia", cuerpo.getNumeroProtocoloNecropsia() != null ? cuerpo.getNumeroProtocoloNecropsia() : "N/A"},
                    {"Causa de Muerte", cuerpo.getCausaMuerte() != null ? cuerpo.getCausaMuerte() : "N/A"},
                    {"Fecha de Nacimiento", cuerpo.getFechaNacimiento() != null ? cuerpo.getFechaNacimiento().format(dateFormatter) : "N/A"},
                    {"Fecha de Defunción", cuerpo.getFechaDefuncion() != null ? cuerpo.getFechaDefuncion().format(dateFormatter) : "N/A"},
                    {"Fecha de Ingreso", cuerpo.getFechaIngreso() != null ? cuerpo.getFechaIngreso().format(dateTimeFormatter) : "N/A"},
                    {"Fecha de Inhumación", cuerpo.getFechaInhumacion() != null ? cuerpo.getFechaInhumacion().format(dateFormatter) : "N/A"},
                    {"Fecha de Exhumación", cuerpo.getFechaExhumacion() != null ? cuerpo.getFechaExhumacion().format(dateFormatter) : "N/A"},
                    {"Funcionario Receptor", cuerpo.getFuncionarioReceptor() != null ? cuerpo.getFuncionarioReceptor() + (cuerpo.getCargoFuncionario() != null ? " (" + cuerpo.getCargoFuncionario() + ")" : "") : "N/A"},
                    {"Autoridad Remitente", cuerpo.getAutoridadRemitente() != null ? cuerpo.getAutoridadRemitente() + (cuerpo.getCargoAutoridadRemitente() != null ? " (" + cuerpo.getCargoAutoridadRemitente() + ")" : "") : "N/A"},
                    {"Autoridad de Exhumación", cuerpo.getAutoridadExhumacion() != null ? cuerpo.getAutoridadExhumacion() + (cuerpo.getCargoAutoridadExhumacion() != null ? " (" + cuerpo.getCargoAutoridadExhumacion() + ")" : "") : "N/A"},
                    {"Estado", cuerpo.getEstado() != null ? cuerpo.getEstado().toString() : "N/A"},
                    {"Observaciones", cuerpo.getObservaciones() != null ? cuerpo.getObservaciones() : "Sin observaciones"}
                };

                // Llenar la tabla con los atributos
                for (String[] attribute : attributes) {
                    // Etiqueta
                    PdfPCell labelCell = new PdfPCell(new Phrase(attribute[0] + ":", labelFont));
                    labelCell.setBorder(Rectangle.NO_BORDER);
                    labelCell.setPadding(2);
                    labelCell.setBackgroundColor(new java.awt.Color(240, 240, 240)); // Fondo gris claro
                    table.addCell(labelCell);

                    // Valor
                    PdfPCell valueCell = new PdfPCell(new Phrase(attribute[1], valueFont));
                    valueCell.setBorder(Rectangle.NO_BORDER);
                    valueCell.setPadding(2);
                    valueCell.setBackgroundColor(new java.awt.Color(240, 240, 240)); // Fondo gris claro
                    table.addCell(valueCell);
                }

                // Agregar la tabla al documento
                document.add(table);

                // Línea separadora
                Paragraph separator = new Paragraph();
                separator.setSpacingAfter(10);
                separator.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator()));
                document.add(separator);

                index++;
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el reporte de cuerpos: " + e.getMessage(), e);
        }
    }

    private void addChartToDocument(Document document, JFreeChart chart, String chartType) throws Exception {
        // Personalizar la apariencia general de los gráficos
        chart.setBackgroundPaint(new java.awt.Color(240, 240, 240));
        chart.setBorderPaint(java.awt.Color.WHITE);
        chart.setBorderStroke(new java.awt.BasicStroke(2.0f));
        chart.setBorderVisible(true);

        // Personalizar según el tipo de gráfico
        if (chartType.equals("pie")) {
            org.jfree.chart.plot.PiePlot<?> piePlot = (org.jfree.chart.plot.PiePlot<?>) chart.getPlot();
            piePlot.setBackgroundPaint(java.awt.Color.WHITE);
            piePlot.setOutlineVisible(false);

            // Habilitar etiquetas simples y mostrarlas dentro de las secciones
            piePlot.setSimpleLabels(true);
            piePlot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            piePlot.setLabelGap(0.02);
            piePlot.setLabelBackgroundPaint(null);
            piePlot.setLabelOutlinePaint(null);
            piePlot.setLabelShadowPaint(null);
            piePlot.setLabelPaint(java.awt.Color.BLACK);

            // Configurar etiquetas dentro de las secciones (valor y porcentaje)
            piePlot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{1} ({2})",
                new java.text.DecimalFormat("0"),
                new java.text.DecimalFormat("0.0%")
            ));

            // Asignar colores específicos según el título del gráfico
            if (chart.getTitle().getText().equals("Estado de los Nichos")) {
                piePlot.setSectionPaint("DISPONIBLE", new java.awt.Color(46, 204, 113));
                piePlot.setSectionPaint("OCUPADO", new java.awt.Color(255, 28, 14));
                piePlot.setSectionPaint("MANTENIMIENTO", new java.awt.Color(52, 152, 219));
            } else if (chart.getTitle().getText().equals("Distribución de Cuerpos")) {
                piePlot.setSectionPaint("Asignados", new java.awt.Color(142, 68, 173));
                piePlot.setSectionPaint("No Asignados", new java.awt.Color(52, 152, 219));
            }

            chart.getLegend().setItemFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            chart.getLegend().setBackgroundPaint(new java.awt.Color(240, 240, 240));
            chart.getLegend().setFrame(new org.jfree.chart.block.BlockBorder(java.awt.Color.WHITE));
        } else if (chartType.equals("bar")) {
            org.jfree.chart.plot.CategoryPlot categoryPlot = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
            categoryPlot.setBackgroundPaint(java.awt.Color.WHITE);
            categoryPlot.setOutlinePaint(java.awt.Color.WHITE);
            categoryPlot.setRangeGridlinePaint(new java.awt.Color(220, 220, 220));
            categoryPlot.setDomainGridlinesVisible(false);

            org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) categoryPlot.getRangeAxis();
            rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());

            // Calcular el valor máximo en el dataset
            DefaultCategoryDataset barDataset = (DefaultCategoryDataset) categoryPlot.getDataset();
            double maxValue = 0;
            for (int row = 0; row < barDataset.getRowCount(); row++) {
                for (int col = 0; col < barDataset.getColumnCount(); col++) {
                    Number value = barDataset.getValue(row, col);
                    if (value != null && value.doubleValue() > maxValue) {
                        maxValue = value.doubleValue();
                    }
                }
            }
            rangeAxis.setRange(0, maxValue + 1); // Agregar un margen de 1 unidad

            BarRenderer customRenderer = new BarRenderer() {
                @Override
                public java.awt.Paint getItemPaint(int row, int column) {
                    if (chart.getTitle().getText().equals("Cuerpos por Tipo")) {
                        return column == 0 ? new java.awt.Color(79, 129, 189) : new java.awt.Color(255, 99, 71);
                    } else {
                        return column == 0 ? new java.awt.Color(46, 204, 113) : new java.awt.Color(33, 150, 243);
                    }
                }
            };

            customRenderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
            customRenderer.setShadowVisible(false);
            customRenderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
            customRenderer.setDefaultItemLabelsVisible(true);
            customRenderer.setDefaultItemLabelFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            categoryPlot.setRenderer(customRenderer);
        } else if (chartType.equals("line")) {
            org.jfree.chart.plot.CategoryPlot categoryPlot = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
            categoryPlot.setBackgroundPaint(java.awt.Color.WHITE);
            categoryPlot.setOutlinePaint(java.awt.Color.WHITE);
            categoryPlot.setRangeGridlinePaint(new java.awt.Color(220, 220, 220));
            categoryPlot.setDomainGridlinesVisible(false);

            org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) categoryPlot.getRangeAxis();
            rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());

            // Calcular el valor máximo en el dataset
            DefaultCategoryDataset lineDataset = (DefaultCategoryDataset) categoryPlot.getDataset();
            double maxValue = 0;
            for (int row = 0; row < lineDataset.getRowCount(); row++) {
                for (int col = 0; col < lineDataset.getColumnCount(); col++) {
                    Number value = lineDataset.getValue(row, col);
                    if (value != null && value.doubleValue() > maxValue) {
                        maxValue = value.doubleValue();
                    }
                }
            }
            rangeAxis.setRange(0, maxValue + 1); // Agregar un margen de 1 unidad

            // Rotar etiquetas del eje X y ajustar fuente
            categoryPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)); // Rotar 45 grados
            categoryPlot.getDomainAxis().setTickLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10)); // Tamaño de fuente más pequeño

            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, new java.awt.Color(33, 150, 243));
            categoryPlot.setRenderer(renderer);
        }

        // Convertir el gráfico a imagen y añadirlo al PDF
        int width = 400;
        int height = 300;
        BufferedImage chartImage = chart.createBufferedImage(width, height);
        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(chartOut, chartImage);
        Image pdfImage = Image.getInstance(chartOut.toByteArray());
        pdfImage.scaleToFit(width, height);
        document.add(pdfImage);
    }
}