/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.votaciones.modelo.graficas;

import com.votaciones.modelo.Grafica;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.JPanel;

/**
 *
 * @author Jaret
 */
public class GraficaPastel extends Grafica{

    private DefaultPieDataset dataset;
    private JFreeChart chart;

    public GraficaPastel(String titulo) {
        super(titulo);
        dataset = new DefaultPieDataset();
        chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);
    }

    @Override
    public JPanel getPanel() {
        return new ChartPanel(chart);
    }

    @Override
    public void setDatos(String[] etiquetas, int[] valores) {
        dataset.clear();
        for (int i = 0; i < etiquetas.length; i++) {
            dataset.setValue(etiquetas[i], valores[i]);
        }
    }
    
}
