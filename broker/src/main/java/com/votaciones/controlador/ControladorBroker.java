package com.votaciones.controlador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.modelo.Broker;

public class ControladorBroker implements Runnable {
    private final String HOST; // opcional si lo usas para binding específico
    private final int PUERTO;
    private final Broker broker;
    private int idSuscriptor=1;

    public ControladorBroker(String host, int puerto, Broker broker) {
        this.HOST = host;
        this.PUERTO = puerto;
        this.broker = broker;
    }

    @Override
    public void run() {
        try (ServerSocket servidor = new ServerSocket(PUERTO, 
                                        50, InetAddress.getByName(HOST))) {
            System.out.println("Broker escuchando en " + HOST + ":" + PUERTO);

            while (true) {
                // Esperar conexión
                Socket socket = servidor.accept();
                System.out.println("Nueva conexión aceptada desde " + socket.getInetAddress());

                // Manejar en un hilo independiente
                new Thread(() -> manejarCliente(socket, idSuscriptor++)).start();
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar el broker: " + e.getMessage());
        }
    }

    private void manejarCliente(Socket socket, int idSuscriptor) {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                JSONObject json = new JSONObject(linea);
                Solicitud solicitud = Solicitud.fromJson(json);

                String servicio = solicitud.getString("servicio", "");

                if (servicio.equalsIgnoreCase("suscribirse")) {
                    broker.agregarSuscriptor("suscriptor-" + idSuscriptor, salida);
                    System.out.println("Suscriptor agregado: " + idSuscriptor);
                }

                Respuesta respuesta = broker.procesarSolicitud(solicitud);
                salida.println(respuesta.toJson().toString());
                salida.flush();
            }
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        }
    }
}