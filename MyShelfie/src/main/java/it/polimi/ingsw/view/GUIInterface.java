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

public class GUIInterface implements UserInterface{
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

    public synchronized Player askForUsername() {
        MessageView message;
        do{
             message = received.poll();

        }while(message==null);
        String username = ((PayloadUsername) message).getUsername();
        return new Player(username);
        //mette il messaggio nella struttura d'uscita e aspetta di riceverlo in entrata.
        //trovato il messaggio e ritorna il valore alla graphic logic.

        /*Platform.runLater(() -> {
            System.out.println("1");
            stage.setTitle("MyShelfie!");
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/login.fxml")));
            loader.setController(this);
            //viewhandler = loader.getController();
            Parent login = null;
            try {
                login = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scene scene1 = new Scene(login, 480, 340);
            scene1.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
            stage.setScene(scene1);
            stage.show();
        });*/
       /* System.out.println("6");
        Thread t = new Thread(
                ()-> {username = viewhandler.getUsername();
                    while(username==null){
                      viewhandler.getUsername();
                    System.out.println(viewhandler.getUsername());
                }
                    String username = viewhandler.getUsername();});
        t.start();*/

    }

    @Override
    public void printErrorMessage(String error) {
        MessageView message = new ErrorMessage(error);
        sended.add(message);
    }

    @Override
    public void printMessage(String msg) {

    }

    @Override
    public int askForNumOfPlayers(ClientHandler cl) {
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

    @Override
    public void showPersonalGoal(int goalNumber) {

    }

    @Override
    public void showCommonGoals(int goalNumber1, int goalNumber2) {

    }

    @Override
    public ArrayList<String> waitForOtherPlayers(ClientHandler cl) {
        return null;
    }

    @Override
    public void showPlayersOrder(ArrayList<String> order) {

    }

    @Override
    public void showGameStart() {

    }

    @Override
    public void turnNotification(String nowPlaying) {

    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public void boardCommand() {

    }

    @Override
    public void shelfCommand() {

    }

    @Override
    public void helpCommand() {

    }

    @Override
    public ArrayList<Tiles> takeCommand() {
        return null;
    }

    @Override
    public void insertCommand(ArrayList<Tiles> pickedTiles) {

    }

    @Override
    public boolean doneCommand() {
        return false;
    }

    @Override
    public void unknownCommand() {

    }

    @Override
    public void commonGoalReached(int index, int goalScore) {

    }

    @Override
    public void shelfCompleted() {

    }

    @Override
    public void turnCompleted() {

    }

    @Override
    public void showWhoIsPlaying(String username) {

    }

    @Override
    public void someoneReachedCommonGoal(String username, Integer position, Integer points) {

    }

    @Override
    public void someoneCompletedShelf(String username) {

    }

    @Override
    public void showPersonalGoalAchievement(int points) {

    }

    @Override
    public void finalScore() {

    }

    @Override
    public void finalRank(ArrayList<Pair<String, Integer>> playerPoints) {

    }

    public void addMessage(MessageView message){
        received.add(message);
    }
    public ConcurrentLinkedQueue<MessageView> getSendedQueue(){
        return sended;
    }
}
