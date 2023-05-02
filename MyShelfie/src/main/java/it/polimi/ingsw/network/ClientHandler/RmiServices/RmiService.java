package it.polimi.ingsw.network.ClientHandler.RmiServices;

import it.polimi.ingsw.network.message.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RmiService extends UnicastRemoteObject implements RmiInterface {

    private RmiInterface client;

    public final ConcurrentLinkedQueue<Message> tempMessageQueue;

    public RmiService(RmiInterface client) throws RemoteException {
        this.client = client;
        this.tempMessageQueue = new ConcurrentLinkedQueue<>();
    }


    public RmiService() throws RemoteException {
        this.client = null;
        this.tempMessageQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void send(Message message) throws RemoteException {
        this.tempMessageQueue.add(message);
    }


    @Override
    public void setClient(RmiInterface rmi) throws RemoteException {
        this.client = rmi;
    }

    @Override
    public RmiInterface getClient() throws RemoteException {
        return this.client;
    }

    @Override
    public Message fakeReceive() {
        return this.tempMessageQueue.poll();
    }

}
