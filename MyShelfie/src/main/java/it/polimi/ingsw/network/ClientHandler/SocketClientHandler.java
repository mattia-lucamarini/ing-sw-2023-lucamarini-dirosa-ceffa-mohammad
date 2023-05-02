package it.polimi.ingsw.network.ClientHandler;

import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.utils.ErrorReceiveException;
import it.polimi.ingsw.utils.ErrorSendException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class: SocketClientHandler
 * Implementation of a ClientHandler using Socket network technology
 */
public class SocketClientHandler extends ClientHandler {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public SocketClientHandler(Socket  socket) throws IOException{
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.pingMessageCounter = 10;
        this.connectionStatus = true;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Implementation of the method getMessageFromNetwork using socket network connection
     *
     * @return the message received
     * @throws ErrorReceiveException if an error occurs trying to get a message from the network
     */
    @Override
    protected Message getMessageFromNetwork() throws ErrorReceiveException {
        try {
            return (Message) this.in.readObject();
        }catch(ClassNotFoundException | IOException e){
            //System.out.println(e);
            throw new ErrorReceiveException();
        }
    }

    /**
     * Implementation of the method putMessageInNetwork with socket network connection
     *
     * @param message message to put in the network
     * @throws ErrorSendException if an error occurs trying to send a message through the socket
     */
    @Override
    protected void putMessageInNetwork(Message message) throws ErrorSendException {
        try {
            this.out.writeObject(message);
            this.out.flush();
        }catch(IOException e){
            //System.out.println(e);
            throw new ErrorSendException();
        }
    }

}
