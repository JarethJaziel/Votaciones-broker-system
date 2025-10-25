package com.votaciones.modelo.servicios;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.votaciones.ProductoDTO;
import com.votaciones.Respuesta;
import com.votaciones.modelo.Servicio;

public class ContarServicio extends Servicio{

    

    public ContarServicio() {
        super("contar");
    }

    @Override
    public Respuesta ejecutar(Map<String, Object> variables) {
        Map<String, Object> respuestasMap = new HashMap<>();
        List<ProductoDTO> productosList = ctrlPersis.getProductos();
        for(ProductoDTO producto : productosList){
            respuestasMap.put(producto.getNombre(), producto.getVotos());
        }
        if(!respuestasMap.isEmpty()){
            return new Respuesta(getNombre(), respuestasMap, true);
        } else {
            respuestasMap.put("mensaje", "Error al leer productos");
            return new Respuesta("error", respuestasMap, false);
        }
        
    }

}
