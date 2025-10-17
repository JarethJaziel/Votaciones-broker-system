package controlador;


import com.votaciones.controlador.ControladorBroker;
import com.votaciones.modelo.ControladorBrokerListener;
import com.votaciones.modelo.ProductoDTO;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ControladorBroker.
 * Se usan mocks y reflexión para evitar modificar la clase original.
 */
public class ControladorBrokerTest {

    @Test
    @DisplayName("getProductos() devuelve lista de productos simulada")
    void testGetProductos() {
        ControladorBroker broker = new ControladorBroker("localhost", 8080) {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 2);
                mock.put("respuesta1", "Producto1");
                mock.put("valor1", 10);
                mock.put("respuesta2", "Producto2");
                mock.put("valor2", 5);
                return mock;
            }
        };

        List<ProductoDTO> productos = broker.getProductos();

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
        ControladorBroker broker = new ControladorBroker("localhost", 8080) {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 1);
                mock.put("respuesta1", "Producto1");
                mock.put("valor1", 11);
                return mock;
            }
        };

        ProductoDTO producto = new ProductoDTO("Producto1");
        producto.setVotos(10);

        broker.votarProducto(producto);

        assertEquals(11, producto.getVotos());
    }

    @Test
    @DisplayName("listarBitacora() devuelve eventos simulados")
    void testListarBitacora() {
        ControladorBroker broker = new ControladorBroker("localhost", 8080) {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 2);
                mock.put("valor1", "Evento A");
                mock.put("valor2", "Evento B");
                return mock;
            }
        };

        List<String> eventos = broker.listarBitacora();

        assertEquals(2, eventos.size());
        assertTrue(eventos.contains("Evento A"));
        assertTrue(eventos.contains("Evento B"));
    }

    @Test
    @DisplayName("registrarBitacora() funciona sin errores con respuesta simulada")
    void testRegistrarBitacora() {
        ControladorBroker broker = new ControladorBroker("localhost", 8080) {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("valor1", 99);
                return mock;
            }
        };

        assertDoesNotThrow(() -> broker.registrarBitacora("Evento de prueba"));
    }

   @Test
    @DisplayName("procesarMensajePush() actualiza votos simulados")
    void testProcesarMensajePush() throws Exception {
        ControladorBroker broker = new ControladorBroker("localhost", 8080) {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 0);
                return mock;
            }
        };

        // Simulamos mensaje push de actualización
        JSONObject mensaje = new JSONObject();
        mensaje.put("tipo", "actualizacionVotos");
        mensaje.put("respuestas", 1);
        mensaje.put("respuesta1", "ProductoPush");
        mensaje.put("valor1", 5);

        // Invocamos método privado con reflexión (sin usar var)
        java.lang.reflect.Method metodo = ControladorBroker.class.getDeclaredMethod("procesarMensajePush", JSONObject.class);
        metodo.setAccessible(true);

        assertDoesNotThrow(() -> metodo.invoke(broker, mensaje));
    }

     @Test
    @DisplayName("crearSolicitud() genera correctamente la estructura JSON")
    void testCrearSolicitud() {
        ControladorBroker broker = new MockBroker();

        // Caso sin variables
        JSONObject sinVars = broker.crearSolicitud("listar", null);
        assertEquals("listar", sinVars.getString("servicio"));
        assertEquals(0, sinVars.getInt("variables"));

        // Caso con variables
        Map<String, Object> vars = new HashMap<>();
        vars.put("p1", 5);
        vars.put("p2", "abc");
        JSONObject conVars = broker.crearSolicitud("votar", vars);

        assertEquals("votar", conVars.getString("servicio"));
        assertEquals(2, conVars.getInt("variables"));
        assertTrue(conVars.has("variable1"));
        assertTrue(conVars.has("valor2"));
    }

    @Test
    @DisplayName("getProductos() devuelve lista vacía si la respuesta es nula")
    void testGetProductosRespuestaNula() {
        ControladorBroker broker = new MockBroker() {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) { return null; }
        };

        List<ProductoDTO> productos = broker.getProductos();
        assertTrue(productos.isEmpty());
    }

    @Test
    @DisplayName("votosContados() maneja datos incompletos correctamente")
    void testVotosContadosDatosIncompletos() throws Exception {
        ControladorBroker broker = new MockBroker();

        JSONObject resp = new JSONObject();
        resp.put("respuestas", 2);
        resp.put("respuesta1", "ProdA");
        // falta valor1
        resp.put("respuesta2", "ProdB");
        resp.put("valor2", 3);

        Method metodo = ControladorBroker.class.getDeclaredMethod("votosContados", JSONObject.class);
        metodo.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ProductoDTO> lista = (List<ProductoDTO>) metodo.invoke(broker, resp);

        assertEquals(2, lista.size());
        assertEquals(0, lista.get(0).getVotos());
        assertEquals(3, lista.get(1).getVotos());
    }

    @Test
    @DisplayName("votarProducto() muestra error si el producto no coincide")
    void testVotarProductoNoCoincidente() {
        ControladorBroker broker = new MockBroker() {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 1);
                mock.put("respuesta1", "OtroProducto");
                mock.put("valor1", 12);
                return mock;
            }
        };

        ProductoDTO producto = new ProductoDTO("Producto1");
        producto.setVotos(10);

        broker.votarProducto(producto);
        assertEquals(10, producto.getVotos());
    }

    @Test
    @DisplayName("votarProducto() sin respuestas válidas no lanza error")
    void testVotarProductoSinRespuestas() {
        ControladorBroker broker = new MockBroker() {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 0);
                return mock;
            }
        };

        ProductoDTO producto = new ProductoDTO("ProductoX");
        assertDoesNotThrow(() -> broker.votarProducto(producto));
    }

    @Test
    @DisplayName("listarBitacora() con respuesta nula devuelve lista vacía")
    void testListarBitacoraRespuestaNula() {
        ControladorBroker broker = new MockBroker() {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) { return null; }
        };

        List<String> eventos = broker.listarBitacora();
        assertTrue(eventos.isEmpty());
    }

    @Test
    @DisplayName("listarBitacora() sin eventos devuelve lista vacía")
    void testListarBitacoraVacia() {
        ControladorBroker broker = new MockBroker() {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) {
                JSONObject mock = new JSONObject();
                mock.put("respuestas", 0);
                return mock;
            }
        };

        List<String> eventos = broker.listarBitacora();
        assertTrue(eventos.isEmpty());
    }

    @Test
    @DisplayName("registrarBitacora() con respuesta nula no lanza excepción")
    void testRegistrarBitacoraRespuestaNula() {
        ControladorBroker broker = new MockBroker() {
            @Override
            public JSONObject getRespuesta(JSONObject solicitud) { return null; }
        };

        assertDoesNotThrow(() -> broker.registrarBitacora("Evento X"));
    }

    @Test
    @DisplayName("procesarMensajePush() tipo bitacora imprime sin error")
    void testProcesarMensajePushBitacora() throws Exception {
        ControladorBroker broker = new MockBroker();
        JSONObject mensaje = new JSONObject();
        mensaje.put("tipo", "bitacora");
        mensaje.put("valor1", "Nuevo evento");

        Method metodo = ControladorBroker.class.getDeclaredMethod("procesarMensajePush", JSONObject.class);
        metodo.setAccessible(true);

        assertDoesNotThrow(() -> metodo.invoke(broker, mensaje));
    }

    @Test
    @DisplayName("procesarMensajePush() con tipo desconocido no lanza error")
    void testProcesarMensajePushDesconocido() throws Exception {
        ControladorBroker broker = new MockBroker();
        JSONObject mensaje = new JSONObject();
        mensaje.put("tipo", "otroTipo");

        Method metodo = ControladorBroker.class.getDeclaredMethod("procesarMensajePush", JSONObject.class);
        metodo.setAccessible(true);

        assertDoesNotThrow(() -> metodo.invoke(broker, mensaje));
    }

    @Test
    @DisplayName("addListener() agrega correctamente")
    void testAddListener() {
        ControladorBroker broker = new MockBroker();
        ControladorBrokerListener listener = productos -> {};
        broker.addListener(listener);
        assertEquals(1, broker.getListeners().size());
    }

    @Test
    @DisplayName("notificarCambioVotos() llama a los listeners registrados")
    void testNotificarCambioVotos() throws Exception {
        ControladorBroker broker = new MockBroker();

        final boolean[] notificado = {false};
        broker.addListener(productos -> notificado[0] = true);

        ProductoDTO p = new ProductoDTO("Prod");
        p.setVotos(3);
        List<ProductoDTO> lista = new ArrayList<>();
        lista.add(p);

        Method metodo = ControladorBroker.class.getDeclaredMethod("notificarCambioVotos", List.class);
        metodo.setAccessible(true);
        metodo.invoke(broker, lista);

        assertTrue(notificado[0]);
    }

    // ------------------ CLASE AUXILIAR ------------------

    /**
     * Subclase que evita conexión real.
     */
    static class MockBroker extends ControladorBroker {
        public MockBroker() {
            super("localhost", 8080);
        }

        @Override
        public void conectar() {
            // No hace conexión real
        }

        @Override
        public JSONObject getRespuesta(JSONObject solicitud) {
            return new JSONObject();
        }
    }

}