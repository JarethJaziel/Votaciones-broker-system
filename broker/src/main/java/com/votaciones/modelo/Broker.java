package com.votaciones.modelo;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;

public class Broker {

    private Map<String, PrintWriter> suscriptores = new ConcurrentHashMap<>(); 
    private Map<String, Servicio> servicios;
    private int contadorServicios = 0;

    public Broker(Map<String, Servicio> servicios) {
        this.servicios = servicios;
        this.contadorServicios = servicios.size();
    }

    public Respuesta procesarSolicitud(Solicitud solicitud) {
        Respuesta respuesta = null;
        String nombreServicio = solicitud.getServicio();
        switch (nombreServicio) {
            case "registrar":
                respuesta = agregarServicio(solicitud);
                break;
            case "listar":
                respuesta = listarServicios(solicitud);
                break;
            case "ejecutar":
                respuesta = ejecutarServicio(solicitud);  
                break;
            case "suscribir":
                respuesta = suscribir(solicitud);
                break;
            default:
                Map<String, Object> valoresError = new java.util.HashMap<>();
                valoresError.put("mensaje", "Servicio no reconocido.");
                respuesta = new Respuesta(nombreServicio, valoresError, false);
                break;
        }
        return respuesta;
    }

    private Respuesta ejecutarServicio(Solicitud solicitud) {
        Respuesta respuesta = null;

        String servicioEjecutar = solicitud.getString("servicio", "");
        
        Servicio servicio = servicios.get(servicioEjecutar);
        
        if (servicio != null) {
            
            Solicitud solicitudServicio = new Solicitud(servicioEjecutar);
            solicitudServicio.setParametros(new HashMap<>(solicitud.getParametros()));
            solicitudServicio.getParametros().remove("servicio");

            respuesta = servicio.solicitarRespuesta(solicitudServicio);

            if ("votar".equals(respuesta != null ? respuesta.getServicio() : "")) {
                Respuesta mensajePush = new Respuesta("mensaje-push", true);
                enviarPushATodos(mensajePush);
            }

        } else {
            Map<String, Object> valores = new java.util.HashMap<>();
            valores.put("mensaje", "Servicio no registrado en el broker.");
            respuesta = new Respuesta(servicioEjecutar, valores, false);
        }

        return respuesta;
    }


    private Respuesta listarServicios(Solicitud solicitud) {
        Map<String, Object> valores = new java.util.HashMap<>();

        Map variables = solicitud.getParametros();
        if (variables == null || variables.isEmpty()) {
            for (Map.Entry<String, Servicio> entry : servicios.entrySet()) {
                String nombreServicio = entry.getValue().getNombre();
                String servidor = entry.getValue().getIpServer() + ":"
                                    + entry.getValue().getPuerto();
                valores.put(nombreServicio, servidor);
            }
        }  else {
            String servicioBuscado = solicitud.getString("palabra", "");
            for (Map.Entry<String, Servicio> entry : servicios.entrySet()) {
                String nombreServicio = entry.getValue().getNombre();
                if (nombreServicio.equalsIgnoreCase(servicioBuscado)) {
                    String servidor = entry.getValue().getIpServer() + ":" 
                                        + entry.getValue().getPuerto();
                    valores.put(nombreServicio, servidor);
                }
            }
        }
        return new Respuesta("listar", valores, true);  
    }

    public Map<String, Servicio> getServicios() {
        return servicios;
    }

    public Respuesta agregarServicio(Solicitud solicitud) {
        Map<String, Object> valoresServicio = solicitud.getParametros();
        String nombre = "", ipServer = "";
        int puerto = 0, parametros = 0;
        for (Map.Entry<String, Object> entry : valoresServicio.entrySet()) {
            String variable = entry.getKey();
            String valor = entry.getValue().toString();
            switch (variable) {
                case "servicio":
                    nombre = valor;
                    break;
                case "servidor":
                    ipServer = valor;
                    break;
                case "puerto":
                    puerto = Integer.parseInt(valor);
                    break;
                case "parametros":
                    parametros = Integer.parseInt(valor);
                    break;
                default:
                    System.out.println("Variable desconocida: " + variable);
                    break;
            }
        }
        Servicio newServicio = new Servicio(++contadorServicios, 
                                            nombre, ipServer, puerto, parametros);
        servicios.put(nombre, newServicio);

        Map<String, Object> respuesta = new java.util.HashMap<>();
        respuesta.put("identificador", newServicio.getId());

        return new Respuesta("registrar", respuesta, true);
    }

    public void agregarSuscriptor(String idCliente, PrintWriter salida) {
        suscriptores.put(idCliente, salida);
        System.out.println("Cliente suscrito: " + idCliente);
    }

    public void enviarPushATodos(Respuesta mensajePush) {
        for (PrintWriter salida : suscriptores.values()) {
            salida.println(mensajePush.toJson().toString());
            salida.flush();
        }
    }

    public Respuesta suscribir(Solicitud solicitud) {
        Respuesta respuesta = new Respuesta("suscribir", true);
        respuesta.agregarRespuesta("totalSuscriptores", suscriptores.size());
        return respuesta;
    }

}
