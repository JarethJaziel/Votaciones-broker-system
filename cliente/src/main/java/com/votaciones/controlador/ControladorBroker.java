package com.votaciones.controlador;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.votaciones.modelo.Broker;
import com.votaciones.modelo.ControladorBrokerListener;
import com.votaciones.modelo.ProductoDTO;

public class ControladorBroker {

    private List<ControladorBrokerListener> listeners = new ArrayList<>();
    private final Broker broker;

    public ControladorBroker(Broker broker) {
        this.broker = broker;
        this.broker.setControladorBroker(this);
        
        broker.iniciarEscuchaPush();
        registrarBitacora("Cliente conectado al broker.");
    }

    public ControladorBroker(String host, int puerto) {
        this.broker = new Broker(host, puerto);
        this.broker.setControladorBroker(this);
        
        broker.iniciarEscuchaPush();
        registrarBitacora("Cliente conectado al broker.");
    }

    public void addListener(ControladorBrokerListener listener) {
        listeners.add(listener);    
    }

    public List<ProductoDTO> getProductos() {
        List<ProductoDTO> productos = new ArrayList<>();

        JSONObject solicitud = broker.crearSolicitud("contar", null);
        JSONObject respuesta = broker.getRespuesta(solicitud);

        if (respuesta == null) {
            System.err.println("No se recibió respuesta del broker.");
            return productos;
        }

        productos = votosContados(respuesta);
        
        return productos;
    }

    public List<ProductoDTO> votosContados(JSONObject respuesta) {
        List<ProductoDTO> productos = new ArrayList<>();
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

        JSONObject solicitud = broker.crearSolicitud("votar", variables);
        JSONObject respuesta = broker.getRespuesta(solicitud);

        int cantidadRespuestas = respuesta.getInt("respuestas");
        if (cantidadRespuestas > 0) {
            String nombreProducto = respuesta.optString("respuesta1", "");
            int votosActuales = respuesta.optInt("valor1", 0);

            // Actualizar el producto local si coincide el nombre
            if (producto.getNombre().equals(nombreProducto)) {
                producto.setVotos(votosActuales);
                registrarBitacora("Se registró un voto para " + nombreProducto);
            } else {
                System.err.println("El producto en la respuesta no coincide.");
            }
        } else {
            System.err.println("No se recibieron respuestas válidas del servidor.");
        }
    }

    public void registrarBitacora(String mensaje) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("evento", mensaje);
        variables.put("fecha", LocalDateTime.now().toString());

        JSONObject solicitud = broker.crearSolicitud("registrar", variables);
        JSONObject respuesta = broker.getRespuesta(solicitud);

        if (respuesta == null) {
            System.err.println("No se recibió respuesta del broker al registrar en la bitácora.");
            return;
        }

        int totalEventos = respuesta.optInt("valor1", 0);
        System.out.println("Evento registrado. Total de eventos en bitácora: " + totalEventos);
    }

    public List<String> listarBitacora() {
        List<String> eventos = new ArrayList<>();
        JSONObject solicitud = broker.crearSolicitud("listar", null);
        JSONObject respuesta = broker.getRespuesta(solicitud);

        if (respuesta == null) {
            System.err.println("No se recibió respuesta del broker al listar bitácora.");
            return eventos;
        }

        int cantidad = respuesta.optInt("respuestas", 0);
        for (int i = 1; i <= cantidad; i++) {
            String evento = respuesta.optString("valor" + i, "(sin descripción)");
            eventos.add(evento);
        }

        return eventos;
    }

    public void notificarCambioVotos(List<ProductoDTO> productos) {
        for (ControladorBrokerListener listener : listeners) {
            listener.onCambioVotos(productos);
        }
    }

    public List<ControladorBrokerListener> getListeners() {      
        return listeners;
    }


}
