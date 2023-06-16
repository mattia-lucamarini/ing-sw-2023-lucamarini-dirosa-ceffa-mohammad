package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.NumPlayersMessage;
import it.polimi.ingsw.utils.ClientDisconnectedException;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GUIInterface {
    private Stage stage;
    private GUIClient client;
    //private ViewHandler viewhandler;
    private ConcurrentLinkedQueue<MessageView> sended;
    private ConcurrentLinkedQueue<MessageView> received;
    @FXML
    TextField text;

    /*public GUIInterface(Stage stage, ViewHandler viewhandler) {
        this.stage = stage;
        this.viewhandler = viewhandler;
    }*/

    public GUIInterface(){
        sended = new ConcurrentLinkedQueue<>();
        received = new ConcurrentLinkedQueue<>();
    }

    public Pair<String, String> askForUsername() {
        MessageView message;
        do{
             message = received.poll();

        }while(message==null);
        String username = ((PayloadUsername) message).getUsername();
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

    public void showPersonalGoal(int goalNumber) {

    }


    public void showCommonGoals(int goalNumber1, int goalNumber2) {

    }

    public ArrayList<String> waitForOtherPlayers(ClientHandler cl) {
        return null;
    }

    public void showPlayersOrder(ArrayList<String> order) {

    }


    public void showGameStart() {

    }

    public void turnNotification(String nowPlaying) {

    }


    public String getCommand() {
        return null;
    }


    public void boardCommand() {

    }


    public void shelfCommand() {

    }


    public void helpCommand() {

    }


    public ArrayList<Tiles> takeCommand() {
        return null;
    }


    public void insertCommand(ArrayList<Tiles> pickedTiles) {

    }


    public boolean doneCommand() {
        return false;
    }


    public void unknownCommand() {

    }


    public void commonGoalReached(int index, int goalScore) {

    }


    public void shelfCompleted() {

    }


    public void turnCompleted() {

    }


    public void showWhoIsPlaying(String username) {

    }


    public void someoneReachedCommonGoal(String username, Integer position, Integer points) {

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
}
