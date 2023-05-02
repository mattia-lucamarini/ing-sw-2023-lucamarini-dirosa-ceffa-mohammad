package it.polimi.ingsw.network.ClientHandler.RmiServices;

import it.polimi.ingsw.network.message.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Interface to extend in order to implement an RMI service object
 *
 */
public interface RmiInterface extends Remote {

    public void send(Message message) throws RemoteException;

    public void setClient(RmiInterface rmi)throws RemoteException;

    public RmiInterface getClient() throws RemoteException;

    public Message fakeReceive() throws RemoteException;
}
