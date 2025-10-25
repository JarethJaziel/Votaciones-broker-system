package com.votaciones.modelo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.controlador.ControladorServicios;

public class Server implements Runnable {

    private final int PUERTO;
    private ControladorServicios ctrlServicios = null;

    public Server(int puerto,ControladorServicios ctrlServicios) {
        this.PUERTO = puerto;
        this.ctrlServicios = ctrlServicios;
    }

    @Override
    public void run() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Broker escuchando en "+ PUERTO);

            while (true) {
                Socket socket = servidor.accept();
                System.out.println("Nueva conexión aceptada desde " + socket.getInetAddress());

                new Thread(() -> atenderCliente(socket)).start();
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar el broker: " + e.getMessage());
        }
    }

    private void atenderCliente(Socket socket) {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                JSONObject json = new JSONObject(linea);

                Solicitud solicitud = Solicitud.fromJson(json);
                String ipCliente = socket.getInetAddress().getHostAddress();
                Respuesta respuesta = ctrlServicios.procesarSolicitud(solicitud, ipCliente);

                salida.println(respuesta.toJson().toString());
                salida.flush();
            }
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        }
    }
}
