package com.votaciones.persistencia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.votaciones.ProductoDTO;
import com.votaciones.persistencia.repo.RepoBitacora;
import com.votaciones.persistencia.repo.RepoProductos;
import com.votaciones.persistencia.repo.RepoVotos;

public class ControladorPersistencia {

    private final RepoProductos repoProductos;
    private final RepoVotos repoVotos;
    private final RepoBitacora repoBitacora;

    public ControladorPersistencia(String carpetaRepo) {
        File carpeta = new File(carpetaRepo);
        if (!carpeta.exists()) {
            carpeta.mkdirs();  // crea todas las subcarpetas necesarias
        }
        
        File archivoProductos = new File(carpetaRepo, "productos.txt");

        // Si no existe productos.txt, crear uno con valor por defecto
        if (!archivoProductos.exists()) {
            try (FileWriter escritor = new FileWriter(archivoProductos)) {
                escritor.write("Pizza\n"+
                                "Hamburguesa\n"+
                                "Hot Dog");
            } catch (IOException e) {
                System.out.println("Error al crear el archivo");
            }
        }
        
        this.repoProductos = new RepoProductos(carpetaRepo + "/productos.txt");
        this.repoVotos = new RepoVotos(carpetaRepo);
        this.repoBitacora = new RepoBitacora(carpetaRepo + "/bitacora.txt");
    }

    public List<ProductoDTO> getAllProductos() {
        List<ProductoDTO> listaProductos = repoProductos.cargarProductos();
        registrarBitacora(repoProductos.getClass().getSimpleName()
                            + ": Productos cargados");
        for (ProductoDTO producto : listaProductos) {
            int votos = repoVotos.contarVotos(producto.getNombre());
            producto.setVotos(votos);
            registrarBitacora(repoVotos.getClass().getSimpleName()
                            + ": Votos de" +producto.getNombre()+" actualizados");
        }
        
        return listaProductos;
    }

    public void registrarBitacora(String string) {
        repoBitacora.log(string);    
    }

    public void registrarBitacora(String fecha, String mensaje){
        repoBitacora.log(fecha, mensaje);
    }

    public void registrarVoto(ProductoDTO producto) {
        repoVotos.registrarVoto(producto.getNombre());
        registrarBitacora(repoVotos.getClass().getSimpleName()
                            + ": Voto registrado para " + producto.getNombre());
    }

    public int getNumEventos() {
        return repoBitacora.getNumEventos();    
    }

    public List<String> getEventos() {
        return repoBitacora.getEventos();    
    }

}
