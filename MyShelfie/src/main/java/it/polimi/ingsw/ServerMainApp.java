package it.polimi.ingsw;

import it.polimi.ingsw.server.WebServer;

/**
 * Main of the Server Application
 *
 */
public class ServerMainApp {

    private static final int SOCKET_DEFAULT_PORT = 59090; // Default server socket port
    private static final int RMI_DEFAULT_PORT = 1099; // Default server RMI port


    public static void main(String[] args) {
        int numParameters = args.length;
        int socketPort = SOCKET_DEFAULT_PORT;
        int rmiPort = RMI_DEFAULT_PORT;
        String serverMode = "production";

        printLogo();
        System.out.println("Welcome to the Server Command Line Interface!");

        if (numParameters == 3 && args[0].equals("--p")) {
        socketPort = Integer.parseInt(args[1]);
        rmiPort = Integer.parseInt(args[2]);
        } else {
        System.out.println("Warning: Bad input parameters. Using default values.");
        }

        try {
        int[] ports = {socketPort, rmiPort};
        WebServer webServer = new WebServer(serverMode, ports);
        webServer.launchKernel();
        } catch (Exception e) {
        System.out.println("Fatal Error! Unknown error occurred while running the web server.");
        e.printStackTrace();
    }
}
    private static void printLogo() {
        System.out.println(" _   _      _ _");
        System.out.println("| | | | ___| | | ___");
        System.out.println("| |_| |/ _ \\ | |/ _ \\");
        System.out.println("|  _  |  __/ | | (_) |");
        System.out.println("|_| |_|\\___|_|_|\\___/");
        System.out.println();
    }
}
