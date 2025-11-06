package controlador;


import com.votaciones.controlador.ControladorBroker;
import com.votaciones.modelo.Broker;
import com.votaciones.modelo.ControladorBrokerListener;

import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.Solicitud;

import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ControladorBroker.
 * Se usan mocks y reflexión para evitar modificar la clase original.
 */
public class ControladorBrokerTest {

    @Test
    @DisplayName("getProductos() devuelve lista de productos simulada")
    void testGetProductos() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 2);
                mock.put("respuesta1", "Producto1");
                mock.put("valor1", 10);
                mock.put("respuesta2", "Producto2");
                mock.put("valor2", 5);
                return Respuesta.fromJson(mock);
            }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        List<ProductoDTO> productos = controlador.getProductos();

        assertAll("Pruebas:",
                    () -> assertEquals(2, productos.size()),
                    () -> assertEquals("Producto1", productos.get(0).getNombre()),
                    () -> assertEquals(10, productos.get(0).getVotos()),
                    () -> assertEquals("Producto2", productos.get(1).getNombre()),
                    () -> assertEquals(5, productos.get(1).getVotos()));  
    }

    @Test
    @DisplayName("votarProducto() actualiza votos correctamente")
    void testVotarProducto() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 1);
                mock.put("respuesta1", "Producto1");
                mock.put("valor1", 11);
                return Respuesta.fromJson(mock);
            }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        ProductoDTO producto = new ProductoDTO("Producto1");
        producto.setVotos(10);

        controlador.votarProducto(producto);

        assertEquals(11, producto.getVotos());
    }

    @Test
    @DisplayName("registrarBitacora() funciona sin errores con respuesta simulada")
    void testRegistrarBitacora() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("valor1", 99);
                return Respuesta.fromJson(mock);
            }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        assertDoesNotThrow(() -> controlador.registrarBitacora("Evento de prueba"));
    }

    @Test
    @DisplayName("getProductos() devuelve lista vacía si la respuesta es nula")
    void testGetProductosRespuestaNula() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) { return null; }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        List<ProductoDTO> productos = controlador.getProductos();
        assertTrue(productos.isEmpty());
    }

    @Test
    @DisplayName("votosContados() maneja datos incompletos correctamente")
    void testVotosContadosDatosIncompletos() throws Exception {
        ControladorBroker controlador = new ControladorBroker(new MockBroker());

        JSONObject resp = new JSONObject();
        resp.put("respuestas", 2);
        resp.put("respuesta1", "ProdA");
        // falta valor1
        resp.put("respuesta2", "ProdB");
        resp.put("valor2", 3);
        Respuesta respuesta = Respuesta.fromJson(resp);

        List<ProductoDTO> lista = controlador.votosContados(respuesta);

        assertEquals(2, lista.size());
        assertEquals(0, lista.get(0).getVotos());
        assertEquals(3, lista.get(1).getVotos());
    }

    @Test
    @DisplayName("votarProducto() muestra error si el producto no coincide")
    void testVotarProductoNoCoincidente() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 1);
                mock.put("respuesta1", "OtroProducto");
                mock.put("valor1", 12);
                return Respuesta.fromJson(mock);
            }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        ProductoDTO producto = new ProductoDTO("Producto1");
        producto.setVotos(10);

        controlador.votarProducto(producto);
        assertEquals(10, producto.getVotos());
    }

    @Test
    @DisplayName("votarProducto() sin respuestas válidas no lanza error")
    void testVotarProductoSinRespuestas() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud){
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 0);
                return Respuesta.fromJson(mock);
            }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        ProductoDTO producto = new ProductoDTO("ProductoX");
        assertDoesNotThrow(() -> controlador.votarProducto(producto));
    }

    @Test
    @DisplayName("listarBitacora() con respuesta nula devuelve lista vacía")
    void testListarBitacoraRespuestaNula() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) { return null; }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        List<String> eventos = controlador.listarBitacora();
        assertTrue(eventos.isEmpty());
    }

    @Test
    @DisplayName("listarBitacora() sin eventos devuelve lista vacía")
    void testListarBitacoraVacia() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 0);
                return Respuesta.fromJson(mock);
            }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        List<String> eventos = controlador.listarBitacora();
        assertTrue(eventos.isEmpty());
    }

    @Test
    @DisplayName("registrarBitacora() con respuesta nula no lanza excepción")
    void testRegistrarBitacoraRespuestaNula() {
        Broker broker = new MockBroker() {
            @Override
            public Respuesta solicitarRespuesta(Solicitud solicitud) { return null; }
        };
        ControladorBroker controlador = new ControladorBroker(broker);

        assertDoesNotThrow(() -> controlador.registrarBitacora("Evento X"));
    }

    @Test
    @DisplayName("addListener() agrega correctamente")
    void testAddListener() {
        ControladorBroker broker = new ControladorBroker(new MockBroker());
        ControladorBrokerListener listener = productos -> {};
        broker.addListener(listener);
        assertEquals(1, broker.getListeners().size());
    }

    @Test
    @DisplayName("notificarCambioVotos() llama a los listeners registrados")
    void testNotificarCambioVotos() throws Exception {
        ControladorBroker controlador = new ControladorBroker(new MockBroker());

        final boolean[] notificado = {false};
        controlador.addListener(productos -> notificado[0] = true);

        ProductoDTO p = new ProductoDTO("Prod");
        p.setVotos(3);
        List<ProductoDTO> lista = new ArrayList<>();
        lista.add(p);

        controlador.notificarCambioVotos(lista);

        assertTrue(notificado[0]);
    }

    // ------------------ CLASE AUXILIAR ------------------

    /**
     * Subclase que evita conexión real.
     */
    static class MockBroker extends Broker {
        public MockBroker() {
            super("localhost", 8080);
        }

        @Override
        public void iniciarEscuchaPush() {
            // No hace conexión real
        }

        @Override
        public Respuesta solicitarRespuesta(Solicitud solicitud) {
            return new Respuesta();
        }
    }

}