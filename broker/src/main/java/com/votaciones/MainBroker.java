package com.votaciones;

import java.util.HashMap;

import com.votaciones.controlador.ControladorBroker;
import com.votaciones.modelo.Broker;

public class MainBroker 
{
    public static void main( String[] args )
    {
    
        Broker broker = new Broker(new HashMap<>());
        new Thread( new ControladorBroker(90, broker))
            .start();
        
    }
}
