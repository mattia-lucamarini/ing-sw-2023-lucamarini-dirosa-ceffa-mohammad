package it.polimi.ingsw.client;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.network.message.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Class: Client
 * This class manages all the requests between the players and the game server.
 * @author Mattia Lucamarini
 */
public class Client {
    public static Player player;
    public static void main(String[] args) {
        System.out.print("Inserisci il tuo username: ");
        Scanner sc = new Scanner(System.in);
        player = new Player(sc.nextLine());

        try (Socket socket = new Socket("127.0.0.1", 59090)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new LoginRequest(player.getUsername()));
            Message message = (Message) in.readObject();

            if(message.getMessageType().equals(MessageCode.NUM_PLAYERS_REQUEST)) {
                Scanner t = new Scanner(System.in);
                //System.out.println("Received "+message.getMessageType());
                System.out.println("Inserire numero di giocatori: ");
                int num = t.nextInt();
                out.writeObject(new NumPlayersMessage(player.getUsername(), num));
                message = (Message) in.readObject();

            }
            if(message.getMessageType().equals(MessageCode.LOGIN_REPLY)){
                //System.out.println("Received "+message.getMessageType());
                if (message.getMessageType() == MessageCode.LOGIN_REPLY)
                    System.out.println(((LoginReply) message).getOutcome());
                if (((LoginReply) message).getOutcome()) {
                    System.out.println("Client added!!");
                } else {
                    System.out.println("Client refused.");
                }
            }
            if(message.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL)) {
                //System.out.println("Received "+message.getMessageType());
                System.out.println("Received personal goal");
            }
            else {
                System.out.println("Unknown message code received. ("+message.getMessageType()+")");
                /*if (message.getMessageType() == MessageCode.LOGIN_REPLY)
                    System.out.println(((LoginReply) message).getOutcome());*/
                //TODO throw new RuntimeException();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
