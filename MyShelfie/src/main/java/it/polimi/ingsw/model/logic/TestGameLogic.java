package it.polimi.ingsw.model.logic;

import it.polimi.ingsw.model.logic.Logic;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageCode;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Test Class to test the communication between the WebServer and the Clients
 */
public class TestGameLogic implements Runnable, Logic {

    private final ConcurrentHashMap<String, ClientHandler> clientList;
    private final int gameID;
    private boolean isActive;

    public TestGameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID) {
        this.clientList = clientList;
        this.gameID = gameID;
        this.isActive = true;
    }

    @Override
    public void run() {
        System.out.println("*Game " + this.gameID + "* Starting ...");
        for (String username : clientList.keySet()){
            try {
                boolean status = clientList.get(username).sendingWithRetry(new Message(MessageCode.GENERIC_MESSAGE),
                        2, 1);
                if(! status){
                    System.out.println("*Game " + this.gameID + "* Error sending to: " + username);
                    clientList.remove(username); //we remove this player from the test
                }
                else{
                    System.out.println("*Game " + this.gameID + "* Correctly send to: " + username);
                }
            }catch(ClientDisconnectedException e){
                System.out.println("*Game " + this.gameID + "* Disconnected: " + username);
                clientList.remove(username); //we remove this player from the test
            }
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        }catch(InterruptedException ignored){
        }

        Message message = null;
        while (clientList.size()>0) {
            for (String username : clientList.keySet()) {
                try {
                    message = clientList.get(username).receive();
                    if(message.getMessageType().equals(MessageCode.GENERIC_MESSAGE)) {
                        System.out.println("*Game " + this.gameID + "* Correctly received from: " + username);
                    }
                    else{
                        System.out.println("*Game " + this.gameID + "* Bad format message: " + username);
                    }
                }catch(NoMessageToReadException e){
                    System.out.println("*Game " + this.gameID + "* No message from: " + username);
                }catch(ClientDisconnectedException  e){
                    System.out.println("*Game " + this.gameID + "* Disconnected: " + username);
                    clientList.remove(username); //we remove this player from the test
                }
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch(InterruptedException ignored){
            }

        }

        System.out.println("*Game "+this.gameID+"* Test Game completed");
        this.isActive = false;
    }
    @Override
    public boolean isActive() {
        return isActive;
    }
}
