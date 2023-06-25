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
        int numParameters = args.length;
        int socketPort;
        int rmiPort;
        if(numParameters != 2) {
            System.out.println("Warning: Bad input parameters. Using default ports.");
            socketPort = 59090;
            rmiPort = 1099;
        }
        else{
            socketPort = Integer.parseInt(args[0]);
            rmiPort = Integer.parseInt(args[1]);
        }
        try {
            int[] ports = {socketPort, rmiPort};
            WebServer webServer = new WebServer("production", ports);
            webServer.launchKernel();
        }
        catch(Exception a){
            System.out.println(a.getMessage());
            System.out.println("Fatal Error! Unknown error occurred running web server.");
        }
    }
}
