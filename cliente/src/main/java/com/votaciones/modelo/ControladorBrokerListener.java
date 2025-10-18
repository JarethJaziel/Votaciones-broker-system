package com.votaciones.modelo;

import java.util.List;

import com.votaciones.ProductoDTO;


public interface ControladorBrokerListener {

    public void onCambioVotos(List<ProductoDTO> productos);

}
