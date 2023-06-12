package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class GUIInterface implements UserInterface{
    private Stage stage;
    private GUIClient client;
    private ViewHandler viewhandler;
    private boolean answertoClient;
    private ArrayList answers;
    public GUIInterface(Stage stage, ViewHandler viewhandler) {
        this.stage = stage;
        this.viewhandler = viewhandler;
    }

    @Override
    public void printErrorMessage(String error) {

    }

    @Override
    public void printMessage(String msg) {

    }

    public Player askForUsername(){
        System.out.println("6");
        viewhandler.getUsername();
        String username=null;
        username = viewhandler.getUsername();
        System.out.println(username);
        return new Player(username);
    }

    @Override
    public int askForNumOfPlayers(ClientHandler cl) throws IOException {
        return viewhandler.getNump();
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

    public boolean getAnswer(){
        return answertoClient;
    }

}
