package com.votaciones.modelo.servicios;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.modelo.Servicio;
import com.votaciones.persistencia.ControladorPersistencia;

public class ListarServicio extends Servicio{

    public ListarServicio(ControladorPersistencia ctrlPersis) {
        super("listar", 0, ctrlPersis);
        //TODO Auto-generated constructor stub
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {

        if(solicitud.getParametros().size() != getNumParametros()){
            Respuesta respuestaError = new Respuesta("error", false);
            respuestaError.agregarRespuesta("mensaje", "Servicio listar no acepta parámetros");
            return respuestaError;
        }

        List<String> eventosList = ctrlPersis.getEventos();
        Map<String, Object> respuestasMap = new HashMap<>();
        
        int i=0;
        for(String evento : eventosList){
            respuestasMap.put("evento"+(++i), evento);
        }

        return new Respuesta(getNombre(), respuestasMap, true);

    }

}
