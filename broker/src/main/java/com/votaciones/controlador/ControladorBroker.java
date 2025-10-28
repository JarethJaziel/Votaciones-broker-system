package com.votaciones.controlador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.modelo.Broker;

public class ControladorBroker implements Runnable {
    private final int PUERTO;
    private final Broker broker;
    private int idSuscriptor=1;

    public ControladorBroker(int puerto, Broker broker) {
        this.PUERTO = puerto;
        this.broker = broker;
    }

    @Override
    public void run() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Broker escuchando en "+ PUERTO);

            while (true) {
                Socket socket = servidor.accept();
                System.out.println("Nueva conexión aceptada desde " + socket.getInetAddress());

                new Thread(() -> atenderCliente(socket, idSuscriptor++)).start();
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar el broker: " + e.getMessage());
        }
    }

    private void atenderCliente(Socket socket, int idSuscriptor) {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                JSONObject json = new JSONObject(linea);
                Solicitud solicitud = Solicitud.fromJson(json);

                String servicio = solicitud.getServicio();

                if (servicio.equalsIgnoreCase("suscribir")) {
                    broker.agregarSuscriptor("suscriptor-" + idSuscriptor, salida);
                    System.out.println("Suscriptor agregado: " + idSuscriptor);
                }
                System.out.println("Solicitud:"+solicitud.toJson().toString()+"\n");
                Respuesta respuesta = broker.procesarSolicitud(solicitud);
                System.out.println("Respuesta: "+respuesta.toJson().toString()+"\n");
                salida.println(respuesta.toJson().toString());
                salida.flush();
            }
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        }
    }
}