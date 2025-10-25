package com.votaciones.modelo;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.persistencia.ControladorPersistencia;

public abstract class Servicio {

    protected ControladorPersistencia ctrlPersis = null;
    private String nombre;
    private int numParametros;

    public Servicio(String nombre, int numParametros, ControladorPersistencia ctrlPersis){
        this.ctrlPersis = ctrlPersis;
        this.numParametros = numParametros;
        this.nombre = nombre;
    }

    public abstract Respuesta ejecutar (Solicitud variables);

    public String getNombre() {
        return nombre;    
    }
    public int getNumParametros(){
        return numParametros;
    }
}
