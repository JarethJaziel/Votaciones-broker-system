package com.votaciones.modelo.servicios;

import java.util.Map;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.modelo.Servicio;
import com.votaciones.persistencia.ControladorPersistencia;

public class RegistrarServicio extends Servicio{

    public RegistrarServicio(ControladorPersistencia ctrlPersis) {
        super("registrar", 2, ctrlPersis);
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        
        if(solicitud.getParametros().size() != getNumParametros()){
            Respuesta respuestaError = new Respuesta("error", false);
            respuestaError.agregarRespuesta("mensaje", "Servicio registrar solo acepta 2 parámetros");
            return respuestaError;
        }

        String fecha = solicitud.getString("fecha", "");
        String evento = "";

        for(Map.Entry<String, Object> entry : solicitud.getParametros().entrySet()){
            if (entry.getKey().contains("evento")) {
                evento = entry.getValue().toString();
            }
        }

        ctrlPersis.registrarBitacora(fecha, evento);
        int numEventos = ctrlPersis.getNumEventos();

        Respuesta respuesta = new Respuesta(getNombre(), true);
        respuesta.agregarRespuesta("eventos", numEventos);

        return respuesta;
    }

}
