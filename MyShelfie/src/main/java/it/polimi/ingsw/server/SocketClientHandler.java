package it.polimi.ingsw.server;

import it.polimi.ingsw.network.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class: SocketClientHandler
 * Implementation of a ClientHandler using Socket network technology
 */
public class SocketClientHandler extends ClientHandler {

    //private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    public SocketClientHandler(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        this.in = in;
        this.out = out;
    }

    @Override
    public Message receive() throws Exception {
        try {
            Message message = (Message) this.in.readObject();
            return message;
        }catch(IOException | ClassNotFoundException e){
            throw new Exception("Exception message");
        }
    }

    @Override
    public boolean send(Message message) {
        try{
            this.out.writeObject(message);
            out.flush();
        }catch(IOException e){
            return false;
        }
        return true;
    }

}
