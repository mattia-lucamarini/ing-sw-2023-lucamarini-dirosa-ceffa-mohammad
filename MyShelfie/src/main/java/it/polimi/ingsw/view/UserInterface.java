package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;

import java.io.IOException;
import java.util.ArrayList;


public interface UserInterface {
    void printErrorMessage(String error);

    void printMessage(String msg);

    Player askForUsername();

    int askForNumOfPlayers(ClientHandler cl) throws IOException;

    //void manageLogin(boolean status);

    void showPersonalGoal(int goalNumber);

    void showCommonGoals(int goalNumber1, int goalNumber2);

    ArrayList<String> waitForOtherPlayers(ClientHandler cl);

    void showPlayersOrder(ArrayList<String> order);

    void showGameStart();

    void turnNotification(String nowPlaying);

    String getCommand(String username);

    void boardCommand();

    void shelfCommand();

    void helpCommand();

    ArrayList<Tiles> takeCommand();

    boolean insertCommand(ArrayList<Tiles> pickedTiles);

    boolean doneCommand();

    void unknownCommand();

    void commonGoalReached(int index, int goalScore);

    void shelfCompleted();

    void turnCompleted();

    void showWhoIsPlaying(String username);

    void someoneReachedCommonGoal(String username, Integer position, Integer points);

    void someoneCompletedShelf(String username);

    void showPersonalGoalAchievement(int points);

    void finalScore();
    void finalRank(ArrayList<Pair<String, Integer>> playerPoints);
}
