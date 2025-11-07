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
    private final String MODO_REGISTRO;

    public Server(int puertoServer, String ipBroker, int puertoBroker, ControladorServicios ctrlServicios, String modoRegistro) {
        this.PUERTO_SERVER = puertoServer;
        this.IP_BROKER = ipBroker;
        this.PUERTO_BROKER = puertoBroker;
        this.ctrlServicios = ctrlServicios;
        this.MODO_REGISTRO = modoRegistro;
    }

    @Override
    public void run() {
        if("--each".equals(MODO_REGISTRO)){
            registrarServiciosEach();
        } else if ("--all".equals(MODO_REGISTRO)){
            registrarServiciosAll();
        }

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

    private void registrarServiciosAll() {
        Solicitud registrarServicios = new Solicitud("registrar");
        registrarServicios.agregarParametro("servidor", getLocalIp());
        registrarServicios.agregarParametro("puerto", PUERTO_SERVER);
        registrarServicios.agregarParametro("parametros", 0);
        
        List<Servicio> serviciosList = ctrlServicios.getServicios();
        int i = 1;
        for(Servicio servicio : serviciosList){
            registrarServicios.agregarParametro("servicio" + i++, servicio.getNombre());    
        }

        Respuesta respuesta = Respuesta.solicitarRespuesta(IP_BROKER, PUERTO_BROKER, registrarServicios);
        if(respuesta!=null && respuesta.isExito()){
            System.out.println("Servicios registrados con éxito");
        } else {
            System.out.println("Servicios fallaron al registrar");
        } 
    }

    private void registrarServiciosEach() {
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

                System.out.println("Solicitud: " + solicitud.toJson().toString()+"\n");
                Respuesta respuesta = ctrlServicios.procesarSolicitud(solicitud, ipCliente);
                System.out.println("Respuesta: " + respuesta.toJson().toString()+"\n");

                salida.println(respuesta.toJson().toString());
                salida.flush();
            }
        } catch (Exception e) {
            System.err.println("Error con cliente: " + e.getMessage());
        }
    }

}
