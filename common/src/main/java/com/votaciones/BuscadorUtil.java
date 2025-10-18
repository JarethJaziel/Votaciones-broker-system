package com.votaciones;

import java.util.Map;

public class BuscadorUtil {

    public static int getInt(String clave, int defecto, Map<String, Object> valores) {
        Object v = valores.get(clave);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (NumberFormatException ignored) {}
        }
        return defecto;
    }

    public static String getString(String clave, String defecto, Map<String, Object> valores) {
        Object v = valores.get(clave);
        return (v != null) ? v.toString() : defecto;
    }

}
