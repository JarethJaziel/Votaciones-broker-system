package com.votaciones.controlador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

    public ControladorBroker(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
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

    private JSONObject getRespuesta(JSONObject solicitud) {
        JSONObject respuesta = null;
        try (Socket socket = new Socket(host, puerto);
             PrintWriter salida = new PrintWriter(socket.getOutputStream());
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {

            // Enviar al servidor
            salida.println(solicitud.toString());
            salida.flush();

            // Leer respuesta del servidor
            String respuestaStr = entrada.readLine();
            if (respuestaStr != null && !respuestaStr.isEmpty()) {
                respuesta = new JSONObject(respuestaStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
