package com.votaciones.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.votaciones.controlador.ControladorBroker;

public class Broker {

    private final String host;
    private final int puerto;
    private Socket socketPush;
    private PrintWriter salidaPush;
    private BufferedReader entradaPush;
    private Thread hiloPush;
    private ControladorBroker controlador;

    public Broker(String host, int puerto, ControladorBroker controlador) {
        this.host = host;
        this.puerto = puerto;
        this.controlador = controlador;
    }

    public Broker(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public JSONObject getRespuesta(JSONObject solicitud) {
        JSONObject respuesta = null;
        try (
            Socket socketTemp = new Socket(host, puerto);
            PrintWriter salida = new PrintWriter(socketTemp.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketTemp.getInputStream()))
        ) {
            salida.println(solicitud.toString());
            salida.flush();

            String respuestaStr = entrada.readLine();
            if (respuestaStr != null && !respuestaStr.isEmpty()) {
                respuesta = new JSONObject(respuestaStr);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en comunicación con el broker: " + e.getMessage());
        }
        return respuesta;
    }

    public JSONObject crearSolicitud(String servicio, Map<String, Object> variables) {
        JSONObject solicitud = new JSONObject();
        solicitud.put("servicio", servicio);
        
        if (variables != null && !variables.isEmpty()){
            solicitud.put("variables", variables.size());
            int i=1;
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                solicitud.put("variable" + i, entry.getKey());
                solicitud.put("valor" + i, entry.getValue());
                i++;
            }
        } else {
            solicitud.put("variables", 0);
        }
        
        return solicitud;
    }

    public void iniciarEscuchaPush() {
        hiloPush = new Thread(() -> {
            try {
                socketPush = new Socket(host, puerto);
                salidaPush = new PrintWriter(socketPush.getOutputStream(), true);
                entradaPush = new BufferedReader(new InputStreamReader(socketPush.getInputStream()));

                // Enviar solicitud de suscripción
                JSONObject solicitud = crearSolicitud("suscribirse", null);
                salidaPush.println(solicitud.toString());
                salidaPush.flush();

                System.out.println("Cliente suscrito al canal push del servidor.");

                // Escuchar mensajes continuamente
                String linea;
                while ((linea = entradaPush.readLine()) != null) {
                    try {
                        JSONObject mensaje = new JSONObject(linea);
                        procesarMensajePush(mensaje);
                    } catch (Exception e) {
                        System.err.println("Error procesando mensaje push: " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                System.err.println("Error en la conexión push: " + e.getMessage());
            } finally {
                cerrarConexionPush();
            }
        }, "HiloPushListener");

        hiloPush.setDaemon(true);
        hiloPush.start();
    }

    public void cerrarConexionPush() {
        try {
            if (entradaPush != null) entradaPush.close();
            if (salidaPush != null) salidaPush.close();
            if (socketPush != null) socketPush.close();
            System.out.println("🔌 Conexión push cerrada correctamente.");
        } catch (IOException ignored) {}
    }

    private void procesarMensajePush(JSONObject mensaje) {
        if (controlador == null) {
            System.err.println("No hay controlador asignado para manejar mensajes push.");
            return;
        }
        String tipo = mensaje.optString("tipo", "");
        switch (tipo) {
            case "actualizacionVotos":
                // El servidor envía productos actualizados
                List<ProductoDTO> productos = new ArrayList<>();
                productos = controlador.votosContados(mensaje);
                controlador.notificarCambioVotos(productos);
                controlador.registrarBitacora("El servidor notificó actualización de votos.");
                break;

            case "bitacora":
                String evento = mensaje.optString("valor1", "");
                System.out.println("Nueva entrada en bitácora: " + evento);
                break;

            default:
                System.out.println("Mensaje push desconocido: " + mensaje);
        }
    }

    public void setControladorBroker(ControladorBroker controladorBroker) {
        this.controlador = controladorBroker;    
    }
}
