package com.votaciones.modelo;

import java.util.Map;

import com.votaciones.Respuesta;
import com.votaciones.persistencia.ControladorPersistencia;

public abstract class Servicio {

    protected ControladorPersistencia ctrlPersis = null;
    private String nombre;

    public Servicio(String nombre){
        ctrlPersis = new ControladorPersistencia();
        this.nombre = nombre;
    }

    public abstract Respuesta ejecutar (Map<String, Object> variables);

    public String getNombre() {
        return nombre;    
    }
}
