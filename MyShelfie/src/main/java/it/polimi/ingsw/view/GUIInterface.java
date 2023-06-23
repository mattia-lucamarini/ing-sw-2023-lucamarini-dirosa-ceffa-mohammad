package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;
import it.polimi.ingsw.view.MessageView.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import static it.polimi.ingsw.view.GraphicLogic.board;

public class GUIInterface {
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private Stage stage;
    private GUIClient client;
    //private ViewHandler viewhandler;
    private ConcurrentLinkedQueue<MessageView> sended;
    private ConcurrentLinkedQueue<MessageView> received;
    private String username;
    private boolean ismyturn = false;
    private ArrayList<Pair<Integer,Integer>> mypicks;
    private List<Tiles> pickedTiles;
    @FXML
    TextField text;

    /*public GUIInterface(Stage stage, ViewHandler viewhandler) {
        this.stage = stage;
        this.viewhandler = viewhandler;
    }*/

    public GUIInterface(){
        mypicks = new ArrayList<>();
        sended = new ConcurrentLinkedQueue<>();
        received = new ConcurrentLinkedQueue<>();
    }

    public Pair<String, String> askForUsername() {
        MessageView message;
        do{
             message = received.poll();

        }while(message==null);
        username = ((PayloadUsername) message).getUsername();
        String connection = ((PayloadUsername) message).getConnection();
        return new Pair<>(username,connection);
        //mette il messaggio nella struttura d'uscita e aspetta di riceverlo in entrata.
        //trovato il messaggio e ritorna il valore alla graphic logic.

    }


    public void printErrorMessage(String error) {
        MessageView message = new ErrorMessage(error);
        sended.add(message);
    }

    public void printMessage(String msg) {
        MessageView message = new NotificationMessage(msg);
        sended.add(message);
    }

    public int askForNumOfPlayers(ClientHandler cl) throws IOException {
        MessageView messageSended = new MessageView(MessageCodeView.NEXT_SCENE);
        System.out.println("ho inviato il messaggio. Adesso aspetto risposta");
        sended.add(messageSended);
        MessageView message;
        do{
            message = received.poll();

        }while(message==null);
        int num = ((NumPlayers) message).getNumplayers();
        System.out.println(((NumPlayers) message).getNumplayers());
        boolean flag;
        try {
            flag = cl.sendingWithRetry(new NumPlayersMessage(num), 2, 1);
        } catch (ClientDisconnectedException e) { //here if I took much time to send the request
            System.out.println("Disconnected from the server before sending NumPlayer Message.");
            return 0;
        }
        if (!flag) {
            System.out.println("Can't send the NumPlayer Message.");
            return 0;
        }
        return num;
    }

    public void showGameScene(){
        MessageView message = new MessageView(MessageCodeView.GAME_SCENE);
        sended.add(message);
    }

    public void showPersonalGoal(int goalNumber) {
        MessageView message = new ShowPersonal(goalNumber);
        sended.add(message);
    }


    public void showCommonGoals(int goalNumber1, int goalNumber2) {
        MessageView message = new ShowCommon(goalNumber1,goalNumber2);
        sended.add(message);
    }

    public ArrayList<String> waitForOtherPlayers(ClientHandler cl) {
        ArrayList<String> playerOrder = null;
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        printMessage("Waiting for other players...");
        while (playerOrder == null) {
            try {
                message = cl.receivingWithRetry(100, 2);
            } catch (NoMessageToReadException e) {
                printErrorMessage("Not enough players to start a new game.");
            } catch (ClientDisconnectedException e) {
                printErrorMessage("Disconnected from the server while waiting for other players.");
            }

            if (message.getMessageType() == MessageCode.PLAYER_ORDER)
                playerOrder = ((PlayerOrder) message).getOrder();
        }
        return playerOrder;
    }

    public void showPlayersOrder(ArrayList<String> order) {
        MessageView message = new PlayerOrderView(order);
        sended.add(message);
    }


    public void showGameStart() {
        MessageView message = new NotificationMessage("The game is now starting.");
        sended.add(message);
    }

    public void turnNotification(String nowPlaying) {
        MessageView message = new NotificationMessage(nowPlaying + "it's your turn..");
        sended.add(message);
    }


    public boolean getCommand() {
        MessageView message;
        System.out.println("sono nel getCommand e il valore di ismyturn è " +ismyturn);
        boolean canContinue = false;
            do {
                System.out.println("sono nel do while");
                message = received.poll();
                if(message!=null){
                    switch (message.getType()) {
                        /*case "board":
                            userInterface.boardCommand();
                            break;
                        case "shelf":
                            userInterface.shelfCommand();
                            break;
                        case "help":
                            userInterface.helpCommand();
                            break;*/

                        case SELECTED_TILE:
                            takeCommand();
                            break;

                        case SHELF:
                            insertCommand();
                            break;
                        case END_TURN:
                            ismyturn = doneCommand();
                            break;
                    }
                }
            } while (ismyturn);
        return canContinue;
    }


    public void boardCommand() {
        MessageView message = new UpdateBoard(GraphicLogic.board);
        sended.add(message);
    }


    public void shelfCommand() {
    }


    public void helpCommand() {

    }


    public void takeCommand() {
        MessageView message;
        do{
            message = received.poll();
            if(ismyturn ==true){
                if(message!=null){
                    if(message.getType()!=MessageCodeView.DONE){
                        mypicks.add(((SelectedTile) message).getIndexes());
                    }
                    else if(message.getType()==MessageCodeView.DONE && mypicks.size()==0){
                         printMessage("You didn't choose any tile." +
                                 "\nPlease do your move before moving forward");
                    }
                    else{ break;}
                }
            }
            else{
                printMessage("Please, wait for your turn");
            }
        }while(mypicks.size()<3);
        Message generic = new Message(MessageCode.GENERIC_MESSAGE);
        if (mypicks.size() > 0) {
            try {
                pickedTiles.addAll(board.takeTiles(mypicks));
                do {
                    try {
                        GraphicLogic.clientHandler.sendingWithRetry(new ChosenTiles(mypicks), ATTEMPTS, WAITING_TIME);
                        generic = GraphicLogic.clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (Exception e) {
                        printErrorMessage("An error occurred in sending the chosen tiles.");
                    }
                } while (generic == null);
                if (generic.getMessageType() == MessageCode.MOVE_LEGAL) {
                    printMessage("Move was verified.");
                } else
                    printMessage("Move was not verified.");
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                printMessage("Try another move.");
                throw new UnsupportedOperationException();
            }
        }
    }


    public void insertCommand() {
        MessageView message;
        ArrayList<Pair<Integer, Integer>> selectedindexes = new ArrayList<>();
        if (pickedTiles.size() == 0) {
            printMessage("You have no available tiles to insert.");
            return;
        }
        do{
            message = received.poll();
            if(ismyturn ==true){
                if(message!=null){
                    int i = ((ShelfSelected) message).getIndexes().getFirst();
                    int j = ((ShelfSelected) message).getIndexes().getSecond();
                    selectedindexes.add(Pair.of(i,j));
                    }
                }
            else{
                printMessage("Please, wait for your turn");
            }
        }while(pickedTiles.size()>0);
        try {
            GraphicLogic.player.getShelf().insertTiles(selectedindexes, pickedTiles);
            MessageView msg = new ShowTile(false, pickedTiles,selectedindexes);
            sended.add(msg);
            pickedTiles.clear();
            printMessage("Done.");
        } catch (RuntimeException e) {
            printErrorMessage(e.getMessage());
        }

    }


    public boolean doneCommand() { //END TURN
        if (pickedTiles.size() > 0 || mypicks.size() == 0) {
            printMessage("You still have to complete your move.");
            return true;
        } else
            return false;
    }


    public void unknownCommand() {

    }


    public void commonGoalReached(int index, int goalScore) {

    }


    public void shelfCompleted() {

    }


    public void turnCompleted() {
        ismyturn=false;
        printMessage("You completed your turn.");
    }


    public void showWhoIsPlaying(String username) {
        printMessage(username+ "is now playing.");
    }


    public void someoneReachedCommonGoal(String username, Integer position, Integer points) {
        MessageView message = new CommonReached(points , position);
        sended.add(message);
        printMessage(username + " reached goal " + position + ", gaining " + points + " points.");
    }


    public void someoneCompletedShelf(String username) {

    }


    public void showPersonalGoalAchievement(int points) {

    }


    public void finalScore() {

    }


    public void finalRank(ArrayList<Pair<String, Integer>> playerPoints) {

    }
    public void addMessage(MessageView message){
        received.add(message);
    }
    public ConcurrentLinkedQueue<MessageView> getSendedQueue(){
        return sended;
    }

    public void setIsmyturn(boolean ismyturn) {
        this.ismyturn = ismyturn;
    }
}
