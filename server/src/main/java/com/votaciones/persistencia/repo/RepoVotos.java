package com.votaciones.persistencia.repo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RepoVotos {

    private String carpetaVotos;
    
    public RepoVotos(String carpetaVotos) {
        this.carpetaVotos = carpetaVotos;
    }

    public int contarVotos(String producto) {
        String archivo = carpetaVotos + "/votos_" + producto + ".txt";
        int contador = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            while (br.readLine() != null) {
                contador++;
            }
        } catch (FileNotFoundException e) {
            // Si no existe, significa 0 votos hasta ahora
        } catch (IOException e) {
            System.err.println("Error al contar votos: " + e.getMessage());
        }

        return contador;
    }

    public void registrarVoto(String producto) {
        String archivo = carpetaVotos + "/votos_" + producto + ".txt";
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, true))) {
            escritor.write(LocalDateTime.now().format(formato));
            escritor.newLine();
        } catch (IOException e) {
            System.err.println("Error al registrar voto: " + e.getMessage());
        }
    }

}
