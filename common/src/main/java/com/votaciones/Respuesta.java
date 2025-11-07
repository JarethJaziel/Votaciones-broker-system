package com.votaciones;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

public class Respuesta {
    private Map<String, Object> respuestas;
    private String servicio;
    private boolean exito;

    public Respuesta(String servicio, Map<String, Object> respuestas, boolean exito) {
        this.servicio = servicio;
        this.respuestas = respuestas;
        this.exito = exito;
    }

    public Respuesta(String servicio, boolean exito) {
        this(servicio, new LinkedHashMap<>(), exito);
    }

    public Respuesta() {
        this.respuestas = new LinkedHashMap<>();
    }

    public static Respuesta fromJson(JSONObject json) {
        Respuesta respuesta = new Respuesta();
        respuesta.setServicio(json.optString("servicio", ""));
        int cantidad = json.optInt("respuestas", 0);
        respuesta.setExito(cantidad>0);
        for (int i = 1; i <= cantidad; i++) {
            String respuestaStr = json.optString("respuesta" + i, "");
            Object valor = json.opt("valor" + i);
            if (!respuestaStr.isEmpty()) {
                if (respuestaStr.equalsIgnoreCase("evento") || respuestaStr.equalsIgnoreCase("servicio")) {
                    respuesta.agregarRespuesta(respuestaStr + i, valor);
                } else {
                    respuesta.agregarRespuesta(respuestaStr, valor);
                }
            }
        }
        
        return respuesta;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("servicio", servicio);
        int i = 1;
        if (respuestas != null && !respuestas.isEmpty()) {
           json.put("respuestas", respuestas.size());
            for (Map.Entry<String, Object> entry : respuestas.entrySet()) {
                Boolean isSpecialKey = entry.getKey().contains("servicio") 
                                        || entry.getKey().contains("evento");

                if (isSpecialKey) {
                    json.put("respuesta" + i, entry.getKey().replaceAll("(\\d+)$", ""));
                } else {
                    json.put("respuesta" + i, entry.getKey());
                }
                json.put("valor" + i, entry.getValue());
                i++;
            } 
        } else {
            json.put("respuestas", 0);
        }
        
        return json;
    }

    public void setExito(boolean exito){
        this.exito = exito;
    }

    public void agregarRespuesta(String respuestaStr, Object valor) {
        respuestas.put(respuestaStr, valor);
    }

    public void setServicio(String servicio){
        this.servicio = servicio;
    }

    public static Respuesta solicitarRespuesta(String host, int puerto, Solicitud solicitud) {
        Respuesta respuesta = null;
        try (
            Socket socketTemp = new Socket(host, puerto);
            PrintWriter salida = new PrintWriter(socketTemp.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketTemp.getInputStream()))
        ) {
            salida.println(solicitud.toJson().toString());
            salida.flush();

            String respuestaStr = entrada.readLine();
            if (respuestaStr != null && !respuestaStr.isEmpty()) {
                JSONObject respuestaJson = new JSONObject(respuestaStr);
                respuesta = Respuesta.fromJson(respuestaJson);
            }
        } catch (Exception e) {
            Map<String, Object> valoresError = new java.util.HashMap<>();
            valoresError.put("mensaje", "Error al conectar: "+ e.getMessage());
            return new Respuesta("error", valoresError, false);
        }
        return respuesta;
    }

    public static Respuesta solicitarRespuestaConTimeout(String host, int puerto, Solicitud solicitud, int timeoutMs) {
        Respuesta respuesta = null;

        try (
            Socket socketTemp = new Socket()
        ) {
            // Conexión con timeout
            socketTemp.connect(new InetSocketAddress(host, puerto), timeoutMs);
            // Tiempo máximo de espera para recibir datos
            socketTemp.setSoTimeout(timeoutMs);

            PrintWriter salida = new PrintWriter(socketTemp.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketTemp.getInputStream()));

            salida.println(solicitud.toJson().toString());
            salida.flush();

            String respuestaStr = entrada.readLine();
            if (respuestaStr != null && !respuestaStr.isEmpty()) {
                JSONObject respuestaJson = new JSONObject(respuestaStr);
                respuesta = Respuesta.fromJson(respuestaJson);
            }
        } catch (SocketTimeoutException e) {
            Map<String, Object> valoresError = new java.util.HashMap<>();
            valoresError.put("mensaje", "Timeout al esperar respuesta del servidor: " + e.getMessage());
            return new Respuesta("error", valoresError, false);
        } catch (Exception e) {
            Map<String, Object> valoresError = new java.util.HashMap<>();
            valoresError.put("mensaje", "Error al conectar: " + e.getMessage());
            return new Respuesta("error", valoresError, false);
        }

        return respuesta;
    }


    public int getInt(String clave, int defecto) {
        return BuscadorUtil.getInt(clave, defecto, respuestas);
    }

    public String getString(String clave, String defecto) {
        return BuscadorUtil.getString(clave, defecto, respuestas);
    }

    public String getServicio(){return servicio; }
    public Map<String, Object> getRespuestas() { return respuestas; }
    public boolean isExito() { return exito; }

    public boolean isEmpty() {
        return respuestas.isEmpty();
    }
}