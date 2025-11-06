package com.votaciones.modelo;

public class BrokerCLI {

    private int puerto;

    // Constructor que recibe los argumentos de línea de comandos
    public BrokerCLI(String[] args) {
        if (args.length == 0 || args.length > 1) {
            System.err.println("Error: Debes proporcionar el puerto como argumento.");
            System.exit(1);
        }

        try {
            this.puerto = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error: El puerto debe ser un número entero válido.");
            System.exit(1);
        }
    }

    public int getPuerto() {
        return puerto;
    }

}
