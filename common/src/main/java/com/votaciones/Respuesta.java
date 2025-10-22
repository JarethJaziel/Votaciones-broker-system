package com.votaciones;

import java.util.Map;

import org.json.JSONObject;

public class Respuesta {
    private Map<String, Object> valores;
    private boolean exito;

    public Respuesta(Map<String, Object> valores, boolean exito) {
        this.valores = valores;
        this.exito = exito;
    }

    public Respuesta() {
    }

    public static Respuesta jsonToRespuesta(JSONObject json) {
        Map<String, Object> valores = json.toMap();
        boolean exito = !valores.isEmpty();
        return new Respuesta(valores, exito);
    }

    public int getInt(String clave, int defecto) {
        return BuscadorUtil.getInt(clave, defecto, valores);
    }

    public String getString(String clave, String defecto) {
        return BuscadorUtil.getString(clave, defecto, valores);
    }

    public Map<String, Object> getValores() { return valores; }
    public boolean isExito() { return exito; }
}