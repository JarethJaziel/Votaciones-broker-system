package com.votaciones.modelo.servicios;

import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.controlador.ControladorVotacion;
import com.votaciones.modelo.Servicio;

public class VotarServicio extends Servicio{

    ControladorVotacion ctrlVotacion = null;

    public VotarServicio(ControladorVotacion ctrlVotacion) {
        super("votar", 1, ctrlVotacion.getCtrlPersis());
        this.ctrlVotacion = ctrlVotacion;
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {

        if(solicitud.getParametros().size() != getNumParametros()){
            Respuesta respuestaError = new Respuesta("error", false);
            respuestaError.agregarRespuesta("mensaje", "Servicio votar solo acepta 1 parámetro");
            return respuestaError;
        }

        String nombreProducto = solicitud.getParametros()
                                                .keySet()
                                                .iterator()
                                                .next()
                                                .toString();
        ProductoDTO producto = new ProductoDTO(nombreProducto);
        ctrlVotacion.votarProducto(producto);
        int votosProducto = ctrlVotacion.getVotos(producto);

        Respuesta respuesta = new Respuesta(getNombre(), true);
        respuesta.agregarRespuesta(producto.getNombre(), votosProducto);

        return respuesta;
    }

}
