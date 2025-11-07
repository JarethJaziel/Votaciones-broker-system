package com.votaciones.modelo;

public class ServerCLI {

    private int puertoServidor;
    private String ipBroker;
    private int puertoBroker;
    private String modoRegistro; // "--each" o "--all"

    public ServerCLI(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java Server <puerto_servidor> <ip_broker:puerto_broker>");
            System.exit(1);
        }

        try {
            // Primer argumento: puerto del servidor
            this.puertoServidor = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error: El primer argumento debe ser un número entero válido (puerto del servidor).");
            System.exit(1);
        }

        // Segundo argumento: dirección del broker (ip:puerto)
        String[] partes = args[1].split(":");
        if (partes.length != 2) {
            System.err.println("Error: El segundo argumento debe tener el formato ip:puerto (por ejemplo, 127.0.0.1:5000)");
            System.exit(1);
        }

        this.ipBroker = partes[0];
        try {
            this.puertoBroker = Integer.parseInt(partes[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: El puerto del broker debe ser un número entero válido.");
            System.exit(1);
        }

        // Modo opcional (por defecto: each)
        if (args.length >= 3) {
            String modo = args[2].trim().toLowerCase();
            if (modo.equals("--each") || modo.equals("--all")) {
                this.modoRegistro = modo;
            } else {
                System.err.println("Modo no reconocido. Usa '--each' o '--all'. Por defecto: '--each'");
                this.modoRegistro = "--each";
            }
        } else {
            this.modoRegistro = "--each";
        }
    }

    public int getPuertoServidor() {
        return puertoServidor;
    }

    public String getIpBroker() {
        return ipBroker;
    }

    public int getPuertoBroker() {
        return puertoBroker;
    }

    public String getModoRegistro() {
        return modoRegistro;
    }

}
