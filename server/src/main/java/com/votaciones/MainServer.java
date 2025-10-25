package com.votaciones;

import java.util.ArrayList;
import java.util.List;

import com.votaciones.controlador.ControladorServicios;
import com.votaciones.controlador.ControladorVotacion;
import com.votaciones.modelo.Server;
import com.votaciones.modelo.Servicio;
import com.votaciones.modelo.Votacion;
import com.votaciones.modelo.servicios.ContarServicio;
import com.votaciones.modelo.servicios.ListarServicio;
import com.votaciones.modelo.servicios.RegistrarServicio;
import com.votaciones.modelo.servicios.VotarServicio;
import com.votaciones.persistencia.ControladorPersistencia;

/**
 * Hello world!
 *
 */
public class MainServer 
{
    public static void main( String[] args ){
        
        final String CARPETA_REPO = System.getProperty("user.dir") + "/resources";
        ControladorPersistencia ctrlPersis = new ControladorPersistencia(CARPETA_REPO);
        
        List<ProductoDTO> productos = ctrlPersis.getAllProductos();
        Votacion votacion = new Votacion(productos);
        ControladorVotacion ctrlVotacion = new ControladorVotacion(votacion, ctrlPersis);

        List<Servicio> servicios = new ArrayList<>();
        servicios.add(new ListarServicio(ctrlPersis));
        servicios.add(new RegistrarServicio(ctrlPersis));
        servicios.add(new ContarServicio(ctrlVotacion));
        servicios.add(new VotarServicio(ctrlVotacion));

        ControladorServicios ctrlServicios = new ControladorServicios(servicios, ctrlPersis);
        Server server = new Server(91,"localhost",90,  ctrlServicios);
        new Thread(server).start();
    }
}
