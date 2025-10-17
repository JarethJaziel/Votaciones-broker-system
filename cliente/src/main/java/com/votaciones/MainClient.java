package com.votaciones;

import com.votaciones.controlador.ControladorBroker;
import com.votaciones.vista.FrmGraficaBarras;
import com.votaciones.vista.FrmGraficaPastel;
import com.votaciones.vista.FrmVotacion;

/**
 * Hello world!
 *
 */
public class MainClient 
{
    public static void main( String[] args ){

        ControladorBroker ctrlBroker = new ControladorBroker("localhost", 90);
        
        FrmVotacion frmVotacion = new FrmVotacion(ctrlBroker);
        frmVotacion.setLocation(10, 100);
        frmVotacion.setVisible(true);
        
        FrmGraficaPastel frmGraficaPastel = new FrmGraficaPastel(ctrlBroker);
        
        frmGraficaPastel.setLocation(600, 100);
        frmGraficaPastel.setVisible(true);
        
        FrmGraficaBarras frmGraficaBarras = new FrmGraficaBarras(ctrlBroker);
        frmGraficaBarras.setLocation(600, 450);
        frmGraficaBarras.setVisible(true);
    }
}
