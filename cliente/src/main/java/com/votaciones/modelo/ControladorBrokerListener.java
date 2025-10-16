package com.votaciones.modelo;

import java.util.List;

public interface ControladorBrokerListener {

    public void onCambioVotos(List<ProductoDTO> productos);

}
