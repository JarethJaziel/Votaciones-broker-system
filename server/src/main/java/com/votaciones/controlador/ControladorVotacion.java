package com.votaciones.controlador;

import java.util.List;

import com.votaciones.ProductoDTO;
import com.votaciones.modelo.Votacion;
import com.votaciones.persistencia.ControladorPersistencia;

public class ControladorVotacion {

    private Votacion votacion = null;
    private ControladorPersistencia ctrlPersis = null;

    public ControladorVotacion(Votacion votacion, ControladorPersistencia controlPersis) {
        this.votacion = votacion;
        this.ctrlPersis = controlPersis;
    }

    public void votarProducto(ProductoDTO producto) {
        ctrlPersis.registrarVoto(producto);
        votacion.votar(producto);
    }

    public Votacion getVotacion() {
        return votacion;
    }

    public List<ProductoDTO> getProductos() {
        return votacion.getProductos();
    }

    public ControladorPersistencia getCtrlPersis(){
        return ctrlPersis;
    }

    public int getVotos(ProductoDTO producto) {
        return votacion.getVotos(producto);
    }

}
