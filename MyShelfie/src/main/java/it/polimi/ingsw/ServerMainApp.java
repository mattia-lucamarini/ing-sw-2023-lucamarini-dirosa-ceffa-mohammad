package it.polimi.ingsw;

import it.polimi.ingsw.server.WebServer;

/**
 * Main of the Server Application
 *
 */
public class ServerMainApp {

    public static void main( String[] args )
    {
        System.out.println(" _   _      _ _");
        System.out.println("| | | | ___| | | ___");
        System.out.println("| |_| |/ _ \\ | |/ _ \\");
        System.out.println("|  _  |  __/ | | (_) |");
        System.out.println("|_| |_|\\___|_|_|\\___/");
        System.out.println();
        System.out.println("Welcome to the Server Command Line Interface!");
        System.out.println();
        try {
            WebServer webServer = new WebServer("production");
            webServer.launchKernel();
        }
        catch(Exception a){
            System.out.println(a.getMessage());
            System.out.println("Fatal Error! Unknown error occurred running web server.");
        }
    }
}
