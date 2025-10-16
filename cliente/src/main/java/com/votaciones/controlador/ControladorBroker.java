package com.votaciones.controlador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.votaciones.modelo.ControladorBrokerListener;
import com.votaciones.modelo.ProductoDTO;

public class ControladorBroker {

    List<ControladorBrokerListener> listeners = new ArrayList<>();
    private final String host;
    private final int puerto;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;

    public ControladorBroker(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
        try {
            conectar();
        } catch (IOException e) {
            System.err.println("⚠️ No se pudo conectar al broker en " + host + ":" + puerto);
        }
    }

    private void conectar() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, puerto);
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
    }

    public void addListener(ControladorBrokerListener listener) {
        listeners.add(listener);    
    }

    public List<ProductoDTO> getProductos() {
        List<ProductoDTO> productos = new ArrayList<>();

        JSONObject solicitud = crearSolicitud("contar", null);
        JSONObject respuesta = getRespuesta(solicitud);

        if (respuesta == null) {
            System.err.println("⚠️ No se recibió respuesta del broker.");
            return productos;
        }

        int cantidadRespuestas = respuesta.getInt("respuestas");

        for(int i=1; i<=cantidadRespuestas; i++) {
            String nombreProducto = respuesta.optString("respuesta"+i, "Producto"+i);
            int votosProducto = respuesta.optInt("valor"+i, 0);
            ProductoDTO producto = new ProductoDTO(nombreProducto);
            producto.setVotos(votosProducto);
            productos.add(producto);
        }
        return productos;
    }

    public void votarProducto(ProductoDTO producto) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put(producto.getNombre(),1);

        JSONObject solicitud = crearSolicitud("votar", variables);
        JSONObject respuesta = getRespuesta(solicitud);

        int cantidadRespuestas = respuesta.getInt("respuestas");
        if (cantidadRespuestas > 0) {
            String nombreProducto = respuesta.optString("respuesta1", "");
            int votosActuales = respuesta.optInt("valor1", 0);

            // Actualizar el producto local si coincide el nombre
            if (producto.getNombre().equals(nombreProducto)) {
                producto.setVotos(votosActuales);
                System.out.println("✅ Voto registrado para " + nombreProducto + 
                                ". Total actual: " + votosActuales);
            } else {
                System.err.println("⚠️ El producto en la respuesta no coincide.");
            }
        } else {
            System.err.println("⚠️ No se recibieron respuestas válidas del servidor.");
        }
    }

    private void registrarBitacora(String mensaje) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("evento", mensaje);
        variables.put("fecha", LocalDateTime.now().toString());

        JSONObject solicitud = crearSolicitud("registrar", variables);
        JSONObject respuesta = getRespuesta(solicitud);

        if (respuesta == null) {
            System.err.println("⚠️ No se recibió respuesta del broker al registrar en la bitácora.");
            return;
        }

        int totalEventos = respuesta.optInt("valor1", 0);
        System.out.println("Evento registrado. Total de eventos en bitácora: " + totalEventos);
    }

    public List<String> listarBitacora() {
        List<String> eventos = new ArrayList<>();
        JSONObject solicitud = crearSolicitud("listar", null);
        JSONObject respuesta = getRespuesta(solicitud);

        if (respuesta == null) {
            System.err.println("⚠️ No se recibió respuesta del broker al listar bitácora.");
            return eventos;
        }

        int cantidad = respuesta.optInt("respuestas", 0);
        for (int i = 1; i <= cantidad; i++) {
            String evento = respuesta.optString("valor" + i, "(sin descripción)");
            eventos.add(evento);
        }

        return eventos;
    }

    private void notificarCambioVotos(List<ProductoDTO> productos) {
        for (ControladorBrokerListener listener : listeners) {
            listener.onCambioVotos(productos);
        }
    }

    private JSONObject getRespuesta(JSONObject solicitud) {
        JSONObject respuesta = null;
        try {
            // Enviar al servidor
            salida.println(solicitud.toString());
            salida.flush();

            // Leer respuesta del servidor
            String respuestaStr = entrada.readLine();
            if (respuestaStr != null && !respuestaStr.isEmpty()) {
                respuesta = new JSONObject(respuestaStr);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error en comunicación con el broker: " + e.getMessage());
        }
        return respuesta;
    }

    private JSONObject crearSolicitud(String servicio, Map<String, Object> variables) {
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

}
