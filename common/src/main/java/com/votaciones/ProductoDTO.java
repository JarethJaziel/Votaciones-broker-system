package com.votaciones;

/**
 *
 * @author Jaret
 */
public class ProductoDTO {
    
    private String nombre;
    private int votos;

    public ProductoDTO(String nombre) {
        this.nombre = nombre;
        this.votos = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVotos() {
        return votos;
    }

    public void setVotos(int votos) {
        this.votos = votos;
    }
    
    public void incrementarVotos() {
        votos++;
    }
    
}
