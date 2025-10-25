package com.votaciones.modelo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.json.JSONObject;

import com.votaciones.Respuesta;
import com.votaciones.Solicitud;
import com.votaciones.controlador.ControladorServicios;

public class Server implements Runnable {

    private final int PUERTO_SERVER;
    private final String IP_BROKER;
    private final int PUERTO_BROKER;
    private ControladorServicios ctrlServicios = null;

    public Server(int puertoServer, String ipBroker, int puertoBroker, ControladorServicios ctrlServicios) {
        this.PUERTO_SERVER = puertoServer;
        this.IP_BROKER = ipBroker;
        this.PUERTO_BROKER = puertoBroker;
        this.ctrlServicios = ctrlServicios;
    }

    @Override
    public void run() {
        registrarServicios();

        try (ServerSocket servidor = new ServerSocket(PUERTO_SERVER)) {
            System.out.println("Server escuchando en "+ PUERTO_SERVER);

            while (true) {
                Socket socket = servidor.accept();
                System.out.println("Nueva conexión aceptada desde " + socket.getInetAddress());

                new Thread(() -> atenderCliente(socket)).start();
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar el broker: " + e.getMessage());
        }
    }

    private void registrarServicios() {
        List<Servicio> serviciosList = ctrlServicios.getServicios();
        for(Servicio servicio : serviciosList){
            Solicitud registrarServicio = new Solicitud("registrar");
            registrarServicio.agregarParametro("servidor", getLocalIp());
            registrarServicio.agregarParametro("puerto", PUERTO_SERVER);
            registrarServicio.agregarParametro("servicio", servicio.getNombre());
            registrarServicio.agregarParametro("parametros", servicio.getNumParametros());

            Respuesta respuesta = Respuesta.solicitarRespuesta(IP_BROKER, PUERTO_BROKER, registrarServicio);
            if(respuesta!=null && respuesta.isExito()){
                System.out.println("Servicio: "+servicio.getNombre()
                                    +" registrado con éxito");
            } else {
                System.out.println("Servicio: "+servicio.getNombre()
                                    +" falló al registrar");
            }
        }

    }

    private String getLocalIp() {
        try {
            InetAddress direccion = InetAddress.getLocalHost();
            return direccion.getHostAddress();
        } catch (Exception e) {
            System.err.println("No se pudo obtener la IP local: " + e.getMessage());
            return "127.0.0.1"; // Valor por defecto
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
