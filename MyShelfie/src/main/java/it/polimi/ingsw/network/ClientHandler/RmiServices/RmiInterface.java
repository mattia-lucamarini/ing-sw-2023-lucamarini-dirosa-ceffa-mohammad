package it.polimi.ingsw.network.ClientHandler.RmiServices;

import it.polimi.ingsw.network.message.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Interface: RmiInterface
 * @author Paolo Ceffa
 * Interface to extend in order to implement an RMI service object which offers the network to allow communication
 * between clients and server
 */
public interface RmiInterface extends Remote {

    public void send(Message message) throws RemoteException;

    public void setClient(RmiInterface rmi)throws RemoteException;

    public RmiInterface getClient() throws RemoteException;

    public Message fakeReceive() throws RemoteException;
}
