package it.polimi.ingsw.server.network.RmiServerServices;

import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiService;
import it.polimi.ingsw.network.message.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface: RmiServerInterface
 * @author Paolo Ceffa
 * Low level Remote interface for the server
 */
public interface RmiServerInterface extends Remote {
    public RmiInterface getRmiClientService() throws RemoteException;

}
