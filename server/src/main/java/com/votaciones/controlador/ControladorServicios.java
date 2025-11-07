package com.votaciones.controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.modelo.Servicio;
import com.votaciones.persistencia.ControladorPersistencia;

public class ControladorServicios {

    Map<String, Servicio> servicios = null;
    ControladorPersistencia ctrlPersis = null;

    public ControladorServicios(List<Servicio> servicios, ControladorPersistencia ctrlPersis) {
        this.servicios = new HashMap<>();
        for(Servicio servicio : servicios){
            this.servicios.put(servicio.getNombre(), servicio);
        }
        this.ctrlPersis = ctrlPersis;
    }

    public Respuesta procesarSolicitud(Solicitud solicitud, String ipCliente) {
        String nombreServicio = solicitud.getServicio();
        Servicio servicio = servicios.get(nombreServicio);

        if (servicio == null) {
            Respuesta respuestaError = new Respuesta("error", false);
            respuestaError.agregarRespuesta("mensaje", "Servicio no encontrado");
            return respuestaError;
        } 
        
        Respuesta respuesta = servicio.ejecutar(solicitud);
        
        ctrlPersis.registrarBitacora("Servicio: " + respuesta.getServicio()
                                        + (respuesta.isExito()? " ejecutado con éxito":
                                        " no se pudo ejecutar: "+respuesta.getString("mensaje", "")));
        return respuesta;
    }

    public List<Servicio> getServicios() {
        return new ArrayList<>(servicios.values());
    }

}
