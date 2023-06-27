package it.polimi.ingsw.server.network.RmiServerServices;

import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface: RmiServerInterface
 * @author Paolo Ceffa
 * Low level Remote interface for the server
 */
public interface RmiServerInterface extends Remote {

    /**
     * Method to allow one Rmi client to obtain the RmiInterface object of the server, which allows a ClientHandler
     * object to manage the communication using RMI technology
     * @return the Rmi service object of the server
     * @throws RemoteException if a network issue occurred using RMI technology
     */
    RmiInterface getRmiClientService() throws RemoteException;

}
