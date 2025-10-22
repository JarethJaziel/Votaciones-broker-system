package com.votaciones.modelo;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;

public class Servicio {

    private int id;
    private String nombre;
    private String ipServer;
    private int puerto;
    private int parametros;

    public Servicio(int id, String nombre, String ipServer, int puerto, int parametros) {
        this.id = id;
        this.nombre = nombre;
        this.ipServer = ipServer;
        this.puerto = puerto;
        this.parametros = parametros;
    }

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getIpServer() {
        return ipServer;
    }
    public int getPuerto() {
        return puerto;
    }
    public int getParametros() {
        return parametros;
    }

    public Respuesta solicitarRespuesta(Solicitud solicitud) {
    
        return Respuesta.solicitarRespuesta(ipServer, puerto, solicitud);
        
    }


}
