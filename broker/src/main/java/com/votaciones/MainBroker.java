package com.votaciones;

import java.util.HashMap;

import com.votaciones.controlador.ControladorBroker;
import com.votaciones.modelo.Broker;
import com.votaciones.modelo.BrokerCLI;

public class MainBroker 
{
    public static void main( String[] args )
    {
        BrokerCLI cli = new BrokerCLI(args);
        int puerto = cli.getPuerto();
        Broker broker = new Broker(new HashMap<>());
        new Thread( new ControladorBroker(puerto, broker))
            .start();
    }
}
