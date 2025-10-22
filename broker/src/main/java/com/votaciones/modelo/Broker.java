package com.votaciones.modelo;

import java.io.PrintWriter;
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
        String nombreServicio = solicitud.getString("servicio", "");
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

        String servicioEjecutar = solicitud.buscarVariable("servicio");

        Servicio servicio = servicios.get(servicioEjecutar);
        if (servicio != null) {
            Solicitud solicitudServicio = new Solicitud(servicioEjecutar);
            int variables = solicitud.getInt("variables", 0);

            for (int i = 2; i <= variables; i++) {
                String clave = solicitud.getString("variable" + i, "");
                Object valor = solicitud.getString("valor" + i, "");
                solicitudServicio.agregarParametro(clave, valor);
            }
            respuesta = servicio.solicitarRespuesta(solicitudServicio);

            if("votar".equals(respuesta.getServicio())){
                Respuesta mensajePush = new Respuesta("mensajePush", true);
                mensajePush.agregarRespuesta("mensaje", "actualizar-votos");
                enviarPushATodos(mensajePush);
            }

        } else {
            Map<String, Object> valores = new java.util.HashMap<>();
            respuesta = new Respuesta(servicioEjecutar, valores, false);
        }


        return respuesta;
    }

    private Respuesta listarServicios(Solicitud solicitud) {
        Map<String, Object> valores = new java.util.HashMap<>();

        int variables = solicitud.getInt("variables", 0);
        if (variables == 0) {
            for (Map.Entry<String, Servicio> entry : servicios.entrySet()) {
                String nombreServicio = entry.getValue().getNombre();
                String servidor = entry.getValue().getIpServer() + ":"
                                    + entry.getValue().getPuerto();
                valores.put(nombreServicio, servidor);
            }
        }  else {
            String servicioBuscado = solicitud.buscarVariable("palabra");
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
        int variables = solicitud.getInt("variables", 0);
        String nombre = "", ipServer = "";
        int puerto = 0, parametros = 0;
        for (int i = 1; i <= variables; i++) {
            String variable = solicitud.getString("variable" + i, "");
            String valor = solicitud.getString("valor" + i, "");
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
