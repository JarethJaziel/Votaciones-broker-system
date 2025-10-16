/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.votaciones.modelo;

import javax.swing.JPanel;

/**
 *
 * @author Jaret
 */
public abstract class Grafica {
    protected String titulo;

    public Grafica(String titulo) {
        this.titulo = titulo;
    }

    public abstract JPanel getPanel();   // para insertar en la vista
    public abstract void setDatos(String[] etiquetas, int[] valores);
}
