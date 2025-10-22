package com.votaciones.controlador;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.votaciones.modelo.Broker;
import com.votaciones.modelo.ControladorBrokerListener;
import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;

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

        Solicitud solicitud = new Solicitud("ejecutar");
        solicitud.agregarParametro("servicio", "contar");
        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if(respuesta != null && respuesta.isExito()){
            productos = votosContados(respuesta);
        } else {
            System.err.println("No se recibió respuesta del broker.");
        }
        return productos;
    }

    public List<ProductoDTO> votosContados(Respuesta respuesta) {
        List<ProductoDTO> productos = new ArrayList<>();

        Map<String, Object> respuestas = respuesta.getRespuestas();
        for(Map.Entry<String, Object> entry : respuestas.entrySet()){
            String nombreProducto = entry.getKey();
            int votosProducto = respuesta.getInt(nombreProducto, 0);

            ProductoDTO producto = new ProductoDTO(nombreProducto);
            producto.setVotos(votosProducto);
            productos.add(producto);
        }

        return productos;
    }

    public void votarProducto(ProductoDTO producto) {
        
        
        Solicitud solicitud = new Solicitud("ejecutar");
        solicitud.agregarParametro("servicio", "votar");
        solicitud.agregarParametro(producto.getNombre(), 1);

        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta != null && respuesta.isExito()) {
            boolean nombresCoinciden = respuesta.getRespuestas()
                                                .containsKey(producto.getNombre());
            // Actualizar el producto local si coincide el nombre
            if (nombresCoinciden) {
                int votosActuales = respuesta.getInt(producto.getNombre(), 0);
                producto.setVotos(votosActuales);
                registrarBitacora("Se registró un voto para " + producto.getNombre());
            } else {
                System.err.println("El producto en la respuesta no coincide.");
            }
        } else {
            System.err.println("No se recibieron respuestas válidas del servidor.");
        }
    }

    public void registrarBitacora(String mensaje) {

        Solicitud solicitud = new Solicitud("ejecutar");
        solicitud.agregarParametro("servicio", "registrar");
        solicitud.agregarParametro("evento", mensaje);
        solicitud.agregarParametro("fecha", LocalDateTime.now().toString());

        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if(respuesta != null && respuesta.isExito()){
            int totalEventos = respuesta.getInt("eventos", 0);
            System.out.println("Evento registrado. Total de eventos en bitácora: " + totalEventos);
        } else {
            System.err.println("No se recibió respuesta del broker al registrar en la bitácora.");
        }
        
    }

    public List<String> listarBitacora() {
        List<String> eventos = new ArrayList<>();
        Solicitud solicitud = new Solicitud("ejecutar");
        solicitud.agregarParametro("servicio", "listar");
        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta == null || !respuesta.isExito()) {
            System.err.println("No se recibió respuesta del broker al listar bitácora.");
            return eventos;
        }

        for(Map.Entry<String, Object> entry : respuesta.getRespuestas().entrySet()){
            String evento = (String) entry.getValue();
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
