package com.votaciones;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Solicitud implements Serializable {
    private String servicio;
    private Map<String, Object> parametros = new HashMap<>();

    public Solicitud(String servicio, Map<String, Object> parametros) {
        this.servicio = servicio;
        this.parametros = parametros;
    }

    public Solicitud(String servicio) {
        this.servicio = servicio;
    }

    public Solicitud() {}

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public Map<String, Object> getParametros() { return parametros; }
    public void setParametros(Map<String, Object> parametros) { this.parametros = parametros; }

    public void agregarParametro(String clave, Object valor) {
        parametros.put(clave, valor);
    }
    
    public JSONObject toJson() { 
        JSONObject solicitudJSON = new JSONObject(); 
        solicitudJSON.put("servicio", servicio); 
        if (parametros != null && !parametros.isEmpty()){ 
            solicitudJSON.put("variables", parametros.size()); 
            int i=1; 
            for (Map.Entry<String, Object> entry : parametros.entrySet()) { 
                solicitudJSON.put("variable" + i, entry.getKey()); 
                solicitudJSON.put("valor" + i, entry.getValue()); i++; 
            } 
        } else { 
            solicitudJSON.put("variables", 0); 
        } 
        return solicitudJSON; 
    }

    public static Solicitud fromJson(JSONObject json) {
        Solicitud solicitud = new Solicitud();
        solicitud.setServicio(json.optString("servicio", ""));
        int cantidad = json.optInt("variables", 0);

        for (int i = 1; i <= cantidad; i++) {
            String variable = json.optString("variable" + i, "");
            Object valor = json.opt("valor" + i);
            if (!variable.isEmpty()) {
                solicitud.agregarParametro(variable, valor);
            }
        }
        return solicitud;
    }

    public int getInt(String clave, int defecto) {
        return BuscadorUtil.getInt(clave, defecto, parametros);
    }

    public String getString(String clave, String defecto) {
        return BuscadorUtil.getString(clave, defecto, parametros);
    }
    
}
