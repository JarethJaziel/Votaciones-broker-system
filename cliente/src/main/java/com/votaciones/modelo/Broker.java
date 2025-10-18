package com.votaciones.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
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

    public Respuesta getRespuesta(Solicitud solicitud) {
        Respuesta respuesta = null;
        try (
            Socket socketTemp = new Socket(host, puerto);
            PrintWriter salida = new PrintWriter(socketTemp.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketTemp.getInputStream()))
        ) {
            salida.println(solicitud.getJson().toString());
            salida.flush();

            String respuestaStr = entrada.readLine();
            if (respuestaStr != null && !respuestaStr.isEmpty()) {
                JSONObject respuestaJson = new JSONObject(respuestaStr);
                respuesta = Respuesta.jsonToRespuesta(respuestaJson);
            }
        } catch (Exception e) {
            System.err.println("Error en comunicación con el broker: " + e.getMessage());
        }
        return respuesta;
    }

    public void iniciarEscuchaPush() {
        hiloPush = new Thread(() -> {
            try {
                socketPush = new Socket(host, puerto);
                salidaPush = new PrintWriter(socketPush.getOutputStream(), true);
                entradaPush = new BufferedReader(new InputStreamReader(socketPush.getInputStream()));

                // Enviar solicitud de suscripción
                Solicitud solicitud = new Solicitud("suscribirse", null);
                salidaPush.println(solicitud.getJson().toString());
                salidaPush.flush();

                System.out.println("Cliente suscrito al canal push del servidor.");

                // Escuchar mensajes continuamente
                String linea;
                while ((linea = entradaPush.readLine()) != null) {
                    try {
                        JSONObject mensaje = new JSONObject(linea);
                        Respuesta mensajePush = Respuesta.jsonToRespuesta(mensaje);
                        procesarMensajePush(mensajePush);
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
            System.out.println("Conexión push cerrada correctamente.");
        } catch (IOException ignored) {}
    }

    private void procesarMensajePush(Respuesta mensaje) {
        if (controlador == null) {
            System.err.println("No hay controlador asignado para manejar mensajes push.");
            return;
        }
        String tipo = mensaje.getString("tipo", "");
        switch (tipo) {
            case "actualizacionVotos":
                // El servidor envía productos actualizados
                List<ProductoDTO> productos = new ArrayList<>();
                productos = controlador.votosContados(mensaje);
                controlador.notificarCambioVotos(productos);
                controlador.registrarBitacora("El servidor notificó actualización de votos.");
                break;

            case "bitacora":
                String evento = mensaje.getString("valor1", "");
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
