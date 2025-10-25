package com.votaciones.modelo.servicios;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.controlador.ControladorVotacion;
import com.votaciones.modelo.Servicio;

public class ContarServicio extends Servicio{

    ControladorVotacion ctrlVotacion = null;

    public ContarServicio(ControladorVotacion ctrlVotacion) {
        super("contar", 0, ctrlVotacion.getCtrlPersis());
        this.ctrlVotacion = ctrlVotacion;
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        
        if(solicitud.getParametros().size() != getNumParametros()){
            Respuesta respuestaError = new Respuesta("error", false);
            respuestaError.agregarRespuesta("mensaje", "Servicio contar no acepta parámetros");
            return respuestaError;
        }

        Map<String, Object> respuestasMap = new HashMap<>();
        List<ProductoDTO> productosList = ctrlVotacion.getProductos();
        for(ProductoDTO producto : productosList){
            respuestasMap.put(producto.getNombre(), producto.getVotos());
        }
        if(!respuestasMap.isEmpty()){
            return new Respuesta(getNombre(), respuestasMap, true);
        } else {
            respuestasMap.put("mensaje", "Error al leer productos");
            return new Respuesta("error", respuestasMap, false);
        }
        
    }

}
