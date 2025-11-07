package com.votaciones.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

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

    public Respuesta solicitarRespuesta(Solicitud solicitud) {
        return Respuesta.solicitarRespuesta(host, puerto, solicitud);
    }

    public void iniciarEscuchaPush() {
        hiloPush = new Thread(() -> {
            try {
                socketPush = new Socket(host, puerto);
                salidaPush = new PrintWriter(socketPush.getOutputStream(), true);
                entradaPush = new BufferedReader(new InputStreamReader(socketPush.getInputStream()));
                
                // Enviar solicitud de suscripción
                Solicitud solicitud = new Solicitud("suscribir", null);
                salidaPush.println(solicitud.toJson().toString());
                salidaPush.flush();

                System.out.println("Cliente suscrito al canal push del servidor.");

                // Escuchar mensajes continuamente
                String linea;
                while ((linea = entradaPush.readLine()) != null) {
                    try {
                        JSONObject mensaje = new JSONObject(linea);
                        Respuesta mensajePush = Respuesta.fromJson(mensaje);
                        System.out.println("Procesando msj push...");
                        procesarMensajePush(mensajePush);
                    } catch (Exception e) {
                        System.err.println("Error procesando mensaje push: " + e.getMessage());
                    }
                }
                System.out.println("Terminando...");
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
        System.out.println("notificando cambio...");
        if (controlador == null) {
            System.err.println("No hay controlador asignado para manejar mensajes push.");
            return;
        }
        if ("mensaje-push".equals(mensaje.getServicio())) {
            
            controlador.notificarCambioVotos(controlador.getProductos());
            controlador.registrarBitacora("El servidor notificó actualización de votos (push).");
        }
    }

    public void setControladorBroker(ControladorBroker controladorBroker) {
        this.controlador = controladorBroker;    
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }
    
}
