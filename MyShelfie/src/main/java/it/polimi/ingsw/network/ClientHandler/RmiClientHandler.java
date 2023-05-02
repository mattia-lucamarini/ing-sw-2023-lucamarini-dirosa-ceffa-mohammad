package it.polimi.ingsw.network.ClientHandler;

import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiService;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.utils.ErrorReceiveException;
import it.polimi.ingsw.utils.ErrorSendException;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Class: SocketClientHandler
 * Implementation of a ClientHandler using RMI network technology
 */
public class RmiClientHandler extends ClientHandler {

    private final RmiInterface rmiService;
    public RmiClientHandler(RmiInterface rmiService) {
        this.rmiService = rmiService;
        this.pingMessageCounter = 10;
        this.connectionStatus = true;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Implementation of the method getMessageFromNetwork using RMI network connection
     *
     * @return the message received, so the first message in
     */
    @Override
    protected Message getMessageFromNetwork() throws ErrorReceiveException{
        Message message = null;
        while(message == null && isConnected()){
            try {
                message = rmiService.fakeReceive();
            }catch(RemoteException e){
                throw new ErrorReceiveException();
            }
        }
        if (message == null) throw new ErrorReceiveException();
        return message;
    }

    /**
     * Implementation of the method putMessageInNetwork with RMI network connection
     *
     * @param message message to add in the tempMessageQueue of the ClientrmiService
     * @throws ErrorSendException if an error occurs trying to send a message
     */
    @Override
    protected void putMessageInNetwork(Message message) throws ErrorSendException {
        try {
            rmiService.getClient().send(message);
        }catch(RemoteException e){
            throw new ErrorSendException();
        }
    }

}
