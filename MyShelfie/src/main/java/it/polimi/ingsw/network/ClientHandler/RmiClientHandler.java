package it.polimi.ingsw.network.ClientHandler;

import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.utils.ErrorReceiveException;
import it.polimi.ingsw.utils.ErrorSendException;


/**
 * Class: SocketClientHandler
 * Implementation of a ClientHandler using RMI network technology
 */
public class RmiClientHandler extends ClientHandler {
    @Override
    protected Message getMessageFromNetwork() throws ErrorReceiveException {
        return null;
    }

    @Override
    protected void putMessageInNetwork(Message message) throws ErrorSendException {

    }

    @Override
    public void stopConnection() {

    }
}
