package com.votaciones.modelo;

import java.util.List;

import com.votaciones.ProductoDTO;

public class Votacion {

    private List<ProductoDTO> productos = null;

    public Votacion(List<ProductoDTO> productos) {
        this.productos = productos;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public void votar(ProductoDTO productoVotado) {
        for (ProductoDTO producto : productos) {
            if (productoVotado.getNombre().equals(producto.getNombre())) {
                producto.incrementarVotos();
                return;
            }
        }
        System.err.println("El producto " + productoVotado.getNombre() + " no se encontró en la votación.");
    }

    public int getVotos(ProductoDTO productoElegido) {
        for (ProductoDTO producto : productos) {
            if (productoElegido.getNombre().equals(producto.getNombre())) {
                return producto.getVotos();
            }
        }
        System.err.println("El producto " + productoElegido.getNombre() + " no se encontró en la votación.");
        return 0;
    }

}
