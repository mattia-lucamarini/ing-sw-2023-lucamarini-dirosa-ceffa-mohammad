package it.polimi.ingsw.client;

import it.polimi.ingsw.network.message.*;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Just a random Client Application to perform some test
 * @author Paolo Ceffa
 */
public class TestClient {
    public static void main(String[] args) throws IOException {

        String username = "pool";
        try (Socket socket = new Socket("127.0.0.1", 59090);)
        {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new LoginRequest(username));
            Message message = (Message) in.readObject();

            if(message.getMessageType().equals(MessageCode.NUM_PLAYERS_REQUEST)) {
                Scanner t = new Scanner(System.in);
                System.out.println("Inserire numero di giocatori: ");
                int num = t.nextInt();
                out.writeObject(new NumPlayersMessage(username, num));
                message = (Message) in.readObject();

            }
            if(message.getMessageType().equals(MessageCode.LOGIN_REPLY)) {
                if (((LoginReply) message).getOutcome()) {
                    System.out.println("Client added!!");
                } else {
                    System.out.println("Client refused.");
                }
            }
            else {
                System.out.println("Unknown message code received.");
                throw new RuntimeException();
            }
            System.out.println("Waiting for other players...");
            try {
                message = (Message) in.readObject();
            }catch(Exception e){
                System.out.println(e);
            }
            if (message.getMessageType().equals(MessageCode.GENERIC_MESSAGE)){
                int testIterations = 0;
                while(testIterations < 10){
                    try {
                        out.writeObject(new Message(username, MessageCode.GENERIC_MESSAGE));
                        out.flush();
                        System.out.println("Message sent.");
                        TimeUnit.SECONDS.sleep(2);
                    }catch(Exception e){
                       System.out.println("Error sending message.");
                    }
                    testIterations += 1;
                }
            }
            else{
                System.out.println("Unknown message code received. Test failed");
            }
            //close the socket
            //socket.close();
            System.out.println("Test completed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
