/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.votaciones.modelo.graficas;
import com.votaciones.modelo.Grafica;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;

public class GraficaBarras extends Grafica {
    private DefaultCategoryDataset dataset;
    private JFreeChart chart;

    public GraficaBarras(String titulo) {
        super(titulo);
        dataset = new DefaultCategoryDataset();
        chart = ChartFactory.createBarChart(
                titulo, "Producto", "Votos", dataset);
    }

    @Override
    public JPanel getPanel() {
        return new ChartPanel(chart);
    }

    @Override
    public void setDatos(String[] etiquetas, int[] valores) {
        dataset.clear();
        for (int i = 0; i < etiquetas.length; i++) {
            dataset.addValue(valores[i], "Votos", etiquetas[i]);
        }
    }
}
