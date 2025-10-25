package com.votaciones.persistencia.repo;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RepoBitacora {

    private final String archivoBitacora;
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RepoBitacora(String archivoBitacora) {
        this.archivoBitacora = archivoBitacora;
    }

    public void log(String fecha, String mensaje){
        try (FileWriter escritorArchivo = new FileWriter(archivoBitacora, true)) {
            escritorArchivo.write(
                            fecha + " | "
                            + mensaje + "\n");
        } catch (IOException e) {
            System.err.println("Error al escribir en la bitácora: " + e.getMessage());
        }
    }

    public void log(String mensaje) {
        try (FileWriter escritorArchivo = new FileWriter(archivoBitacora, true)) {
            String horaActual = LocalDateTime.now().format(formato);
            escritorArchivo.write(
                            horaActual + " | "
                            + mensaje + "\n");
        } catch (IOException e) {
            System.err.println("Error al escribir en la bitácora: " + e.getMessage());
        }
    }

    public int getNumEventos() {
        int contador = 0;
        try (java.io.BufferedReader lector = new java.io.BufferedReader(new java.io.FileReader(archivoBitacora))) {
            while (lector.readLine() != null) {
                contador++;
            }
        } catch (IOException e) {
            System.err.println("Error al leer la bitácora: " + e.getMessage());
        }
        return contador;    
    }

    public List<String> getEventos() {
        List<String> eventos = new ArrayList<>();
        try (java.io.BufferedReader lector = new java.io.BufferedReader(new java.io.FileReader(archivoBitacora))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                eventos.add(linea);
            }
        } catch (IOException e) {
            System.err.println("Error al leer la bitácora: " + e.getMessage());
        }
        return eventos;    
    }

}
