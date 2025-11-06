package com.votaciones.modelo;

public class ClienteCLI {

    private String ipBroker;
    private int puertoBroker;

    public ClienteCLI(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java Cliente <ip_broker:puerto_broker>");
            System.exit(1);
        }

        String[] partes = args[0].split(":");
        if (partes.length != 2) {
            System.err.println("Error: El argumento debe tener el formato ip:puerto (por ejemplo, 127.0.0.1:5000)");
            System.exit(1);
        }

        this.ipBroker = partes[0];

        try {
            this.puertoBroker = Integer.parseInt(partes[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: El puerto del broker debe ser un número entero válido.");
            System.exit(1);
        }
    }

    public String getIpBroker() {
        return ipBroker;
    }

    public int getPuertoBroker() {
        return puertoBroker;
    }
}
