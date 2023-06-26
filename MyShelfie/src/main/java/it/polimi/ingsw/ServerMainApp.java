package it.polimi.ingsw;

import it.polimi.ingsw.server.WebServer;

/**
 * Main of the Server Application
 *
 */
public class ServerMainApp {

    public static void main( String[] args )
    {
        int numParameters = args.length;
        /* Default Server ports: */
        int socketPort = 59090;
        int rmiPort = 1099;

        System.out.println(" _   _      _ _");
        System.out.println("| | | | ___| | | ___");
        System.out.println("| |_| |/ _ \\ | |/ _ \\");
        System.out.println("|  _  |  __/ | | (_) |");
        System.out.println("|_| |_|\\___|_|_|\\___/");
        System.out.println();
        System.out.println("Welcome to the Server Command Line Interface!");

        if(numParameters != 3) {
            System.out.println("Warning: Bad input parameters. Using default ones.");
        }
        else{
            if(args[0].equals("--p")) {
                socketPort = Integer.parseInt(args[1]);
                rmiPort = Integer.parseInt(args[2]);
            }
            else{
                System.out.println("Warning: Unknown input parameter. Using default ones.");
            }
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
