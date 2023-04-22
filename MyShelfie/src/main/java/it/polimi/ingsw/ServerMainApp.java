package it.polimi.ingsw;

import it.polimi.ingsw.server.WebServer;

/**
 * Main of the Server Application
 *
 */
public class ServerMainApp {

    public static void main( String[] args )
    {
        System.out.println( "Starting application!" );
        try {
            WebServer webServer = new WebServer();
            webServer.launchKernel();
        }
        catch(Exception a){
            System.out.println("Fatal Error!");
        }
    }
}
