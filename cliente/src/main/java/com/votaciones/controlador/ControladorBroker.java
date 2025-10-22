package com.votaciones.controlador;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.votaciones.modelo.Broker;
import com.votaciones.modelo.ControladorBrokerListener;
import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;

public class ControladorBroker {

    private List<ControladorBrokerListener> listeners = new ArrayList<>();
    private final Broker broker;
    private static final String CLAVE_RESP = "respuestas";

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

        Solicitud solicitud = new Solicitud("contar", null);
        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta == null || !respuesta.isExito()) {
            System.err.println("No se recibió respuesta del broker.");
            return productos;
        }

        productos = votosContados(respuesta);
        
        return productos;
    }

    public List<ProductoDTO> votosContados(Respuesta respuesta) {
        List<ProductoDTO> productos = new ArrayList<>();
        int cantidadRespuestas = respuesta.getInt(CLAVE_RESP, 0);

        for(int i=1; i<=cantidadRespuestas; i++) {
            String nombreProducto = respuesta.getString("respuesta"+i, "Producto"+i);
            int votosProducto = respuesta.getInt("valor"+i, 0);
            ProductoDTO producto = new ProductoDTO(nombreProducto);
            producto.setVotos(votosProducto);
            productos.add(producto);
        }
        return productos;
    }

    public void votarProducto(ProductoDTO producto) {
        
        
        Solicitud solicitud = new Solicitud("votar");
        solicitud.agregarParametro(producto.getNombre(), 1);

        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        int cantidadRespuestas = respuesta.getInt(CLAVE_RESP, 0);
        if (cantidadRespuestas > 0) {
            String nombreProducto = respuesta.getString("respuesta1", "");
            int votosActuales = respuesta.getInt("valor1", 0);

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

        Solicitud solicitud = new Solicitud("registrar");
        solicitud.agregarParametro("evento", mensaje);
        solicitud.agregarParametro("fecha", LocalDateTime.now().toString());

        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta == null || !respuesta.isExito()) {
            System.err.println("No se recibió respuesta del broker al registrar en la bitácora.");
            return;
        }

        int totalEventos = respuesta.getInt("valor1", 0);
        System.out.println("Evento registrado. Total de eventos en bitácora: " + totalEventos);
    }

    public List<String> listarBitacora() {
        List<String> eventos = new ArrayList<>();
        Solicitud solicitud = new Solicitud("listar", null);
        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta == null || !respuesta.isExito()) {
            System.err.println("No se recibió respuesta del broker al listar bitácora.");
            return eventos;
        }
        int cantidad = respuesta.getInt(CLAVE_RESP, 0);
        for (int i = 1; i <= cantidad; i++) {
            String evento = respuesta.getString("valor" + i, "(sin descripción)");
            eventos.add(evento);
            System.out.println("Evento " + i + ": " + evento);
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
