package it.polimi.ingsw.server;

import it.polimi.ingsw.network.message.Message;

/**
 * Class: SocketClientHandler
 * Implementation of a ClientHandler using RMI network technology
 */
public class RmiClientHandler extends ClientHandler{
    @Override
    public Message receive() {
        return null;
    }

    @Override
    public boolean send(Message message){
        return false;
    }
}
