package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;

import java.util.ArrayList;


public interface UserInterface {

void printErrorMessage(String error);

Player askForUsername();

int askForNumOfPlayers();

void manageLogin(boolean status);

void showPersonalGoal(int goalNumber);

void showCommonGoals(int goalNumber1, int goalNumber2);

void waitForOtherPlayers();

void showPlayersOrder(ArrayList<String> order);

void showStartGame();

void turnNotification();

String getCommand();

void boardCommand();

void shelfCommand();

void helpCommand();

ArrayList<Pair<Integer, Integer>> takeCommand();

Pair<Pair<Integer, Integer>, Tiles> insertCommand();

void doneCommand();

void unknownCommand();

void commonGoalReached();

void shelfCompleted();

void turnCompleted();

void showWhoIsPlaying(String username);

void someoneReachedCommonGoal(String username, Integer position, Integer points);

void someoneCompletedShelf(String username);

void showPersonalGoalAchievement(Integer personalGoalPoints, Integer groupPoints, Integer groups);

void finalRank(ArrayList<Pair<String, Integer>> playerPoints);


}
