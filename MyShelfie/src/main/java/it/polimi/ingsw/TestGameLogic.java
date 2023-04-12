package it.polimi.ingsw;

import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageCode;
import it.polimi.ingsw.server.ClientHandler;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Test Class to test the communication between the WebServer and the Clients
 */
public class TestGameLogic implements Runnable{

    private final HashMap<String, ClientHandler> clientList;
    private int gameID;
    private boolean isActive;

    public TestGameLogic(HashMap<String, ClientHandler> clientList, int gameID) {
        this.clientList = clientList;
        this.gameID = gameID;
        this.isActive = true;
    }

    @Override
    public void run() {
        System.out.println("Game " + this.gameID + ": Starting ...");
        for (String username : clientList.keySet()){
            boolean status = clientList.get(username).send(new Message(username, MessageCode.GENERIC_MESSAGE));
            if (status){
                System.out.println("Game " + this.gameID + ": Correctly send to: " + username);
            }
            else{
                System.out.println("Game " + this.gameID + ": Error sending to: " + username);
                throw new RuntimeException();
            }
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch(InterruptedException ignored){
        }

        int testIterations = 0;
        while (testIterations < 10) {
            for (String username : clientList.keySet()) {
                try {
                    Message message = clientList.get(username).receive();
                    if(message.getMessageType().equals(MessageCode.GENERIC_MESSAGE)) {
                        System.out.println("Game " + this.gameID + ": Correctly received from: " + username);
                    }
                    else{
                        System.out.println("Game " + this.gameID + ": Bad format message: " + username);
                    }
                }catch(Exception e){
                    System.out.println("Game "+this.gameID+": Error occurred during the test with the player: "+username);
                }
            }
            testIterations += 1;
            try {
                TimeUnit.SECONDS.sleep(10);
            }catch(InterruptedException ignored){
            }

        }
        System.out.println("Game "+this.gameID+": Test Game completed");
        this.isActive = false;
    }
    public boolean isActive() {
        return isActive;
    }
}
