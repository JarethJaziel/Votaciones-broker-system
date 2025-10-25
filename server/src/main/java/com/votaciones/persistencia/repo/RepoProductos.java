package com.votaciones.persistencia.repo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.votaciones.ProductoDTO;

public class RepoProductos {

    private String archivo;
    
    public RepoProductos(String archivo) {
        this.archivo = archivo;
    }

    /**
     * Lee el archivo de productos y devuelve una lista de objetos Producto.
     * @return 
     */
    public List<ProductoDTO> cargarProductos() {
        List<ProductoDTO> productos = new ArrayList<>();

        try (BufferedReader lectorArchivo = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lectorArchivo.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    productos.add(new ProductoDTO(linea));
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer productos: " + e.getMessage());
        }

        return productos;
    }

}
