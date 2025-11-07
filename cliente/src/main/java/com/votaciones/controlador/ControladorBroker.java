package com.votaciones.controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Solicitud solicitud = new Solicitud("ejecutar");
        solicitud.agregarParametro("servicio", "registrar");
        solicitud.agregarParametro("evento", mensaje);
        solicitud.agregarParametro("fecha", LocalDateTime.now().format(formato).toString());

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

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        eventos.sort((a, b) -> {
            try {
                LocalDateTime fechaA = LocalDateTime.parse(a.substring(0, 19), formato);
                LocalDateTime fechaB = LocalDateTime.parse(b.substring(0, 19), formato);
                return fechaB.compareTo(fechaA);
            } catch (Exception e) {
                // Si no se puede parsear, deja el orden como estaba o por texto
                return a.compareTo(b);
            }
        });

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

    public int getPuertoBroker(){
        return broker.getPuerto();
    }
    
    public String getIpBroker(){
        return broker.getHost();
    }

    public Map<String, String> listarServicios() {
        Map<String, String> servicios = new HashMap<>();
        Solicitud solicitud = new Solicitud("listar");
        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta == null || !respuesta.isExito()) {
            System.err.println("No se recibió respuesta del broker al listar servicios.");
            return servicios;
        }

        for(Map.Entry<String, Object> entry : respuesta.getRespuestas().entrySet()){
            String servicio = entry.getKey();
            String servidor = entry.getValue().toString();
            servicios.put(servicio, servidor);
        }

        return servicios;
    }

    public Map<String, String> listarServicio(String text) {
        Map<String, String> servicios = new HashMap<>();
        Solicitud solicitud = new Solicitud("listar");
        solicitud.agregarParametro("palabra", text);
        Respuesta respuesta = broker.solicitarRespuesta(solicitud);

        if (respuesta == null || !respuesta.isExito()) {
            System.err.println("No se recibió respuesta del broker al listar servicios.");
            return servicios;
        }

        for(Map.Entry<String, Object> entry : respuesta.getRespuestas().entrySet()){
            String servicio = entry.getKey();
            String servidor = entry.getValue().toString();
            servicios.put(servicio, servidor);
        }

        return servicios;
    }

}
