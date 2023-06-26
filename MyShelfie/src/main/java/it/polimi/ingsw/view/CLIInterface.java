package it.polimi.ingsw.view;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.PersonalGoal;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.util.*;
import java.util.regex.Pattern;

import static it.polimi.ingsw.client.Client.board;

public class CLIInterface implements UserInterface{
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private static final List<String> goalNames = List.of("6 groups of 2 equal tiles adjacent to each other", "4 tiles of the same type in each corner of the shelf", "6 groups of 4 equal tiles adjacent to each other", "2 squares of 4 equal tiles each", "3 columns of 6 tiles each having 1,2 or 3 different types", "8 equal tiles in any position on the shelf", "5 equal tiles diagonally placed", "4 rows of 5 tiles each having 1,2 or 3 different types", "2 columns each having 6 different tiles", "2 rows each having 5 different tiles", "5 equal tiles forming a cross", "5 columns in increasing or decreasing height forming a staircase with any type of tile");
    private Scanner sc = new Scanner(System.in);
    private ArrayList<Pair<Integer, Integer>> totalPick = new ArrayList<>();
    private ArrayList<Tiles> pickedTiles = new ArrayList<>();
    private List<PersonalGoal> personalGoals = PersonalGoal.all();
    Pattern tilePattern;

    @Override
    public Player askForUsername(){
        System.out.print("Insert your username: ");
        return new Player(sc.nextLine());
    }
    @Override
    public void printErrorMessage(String error){
        System.out.println(error);
    }
    @Override
    public void printMessage(String msg){
        System.out.println(msg);
    }
    @Override
    public int askForNumOfPlayers(ClientHandler clientHandler){
        //TODO: Limit number of players
        Scanner t = new Scanner(System.in);
        int num = 0;
        System.out.print("Insert player number: ");
        while (num == 0) {
            try {
                num = t.nextInt();
                while (num < 1 || num > 4) {
                    System.out.println("The number of players must be between 1-4!");
                    System.out.print("Insert player number: ");
                    num = t.nextInt();
                }
            } catch (InputMismatchException e) {
                System.out.println("Please insert an actual number.");
            }
        }
        boolean flag;
        try {
            flag = clientHandler.sendingWithRetry(new NumPlayersMessage(num), 2, 1);
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
    public void showPersonalGoal(int goalIndex){
        System.out.println("Your personal goal is: " + goalIndex);
        for (Map.Entry<Pair<Integer, Integer>, Tiles> entry : personalGoals.get(goalIndex).getConstraint().entrySet()){
            System.out.println("\t" + entry.getValue() + " in (" + entry.getKey().getFirst() + "," + entry.getKey().getSecond() + ")");
        }
    }
    @Override
    public void showCommonGoals(int goal1, int goal2){
        System.out.println("The common goals are:\n\t" + goalNames.get(goal1) + "\n\t" + goalNames.get(goal2));
    }
    @Override
    public ArrayList<String> waitForOtherPlayers(ClientHandler clientHandler){
        ArrayList<String> playerOrder = null;
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        System.out.println("Waiting for other players...");
        while (playerOrder == null) {
            try {
                message = clientHandler.receivingWithRetry(100, 2);
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
    @Override
    public void showPlayersOrder(ArrayList<String> order){
        System.out.println("\nPlayers order:");
        for (String pl : order)
            System.out.println("\t" + pl);
    }
    @Override
    public void showGameStart(){
        System.out.println("The game is now starting");
    }
    @Override
    public String getCommand(){
        System.out.print("> ");
        return sc.nextLine();
    }
    @Override
    public void boardCommand(){
        Client.board.printBoard();
    }
    @Override
    public void shelfCommand(){
        System.out.println("Insert the name of the player, or nothing if you want to see yours.");
        System.out.print("Players: ");
        for (String pl : Client.playerOrder)
            System.out.print(pl + " ");
        System.out.print("\n\t> ");
        String shelfCommand = sc.nextLine();
        if (shelfCommand.equals("") || shelfCommand.equals(Client.player.getUsername()))
            Client.player.getShelf().printShelf();
        else{
            try{
                Client.playerShelves.get(shelfCommand).printShelf();
            } catch (Exception e){
                System.out.println("Unknown player. Try again");
            }
        }
    }
    @Override
    public void helpCommand(){
        System.out.println("""
                                                \tboard: print board
                                                \tshelf: print shelf
                                                \tcommon: print common goals
                                                \tpersonal: print personal goal
                                                \ttake: extract the tiles specified by your coordinates
                                                \tinsert: put your tiles into the shelf
                                                \tdone: end your turn""");
    }

    @Override
    public ArrayList<Tiles> takeCommand() throws UnsupportedOperationException {
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        if (totalPick.size() == 0) {
            System.out.println("Type the coordinates of the tile you want to take (ex. 3 2)\n Type 'cancel' to redo your move\n Type 'done' if you are done.");
            for (int i = totalPick.size(); i < 3 && totalPick.size() < 3; i++) {
                System.out.print("\t" + (i + 1) + "> ");
                String tilePick = sc.nextLine();
                tilePattern = Pattern.compile("[0-9]\\s+[0-9]");
                if (tilePick.equals("cancel")) {
                    totalPick.clear();
                    break;
                } else if (tilePattern.matcher(tilePick).find()) {
                    Scanner pickScanner = new Scanner(tilePick);
                    totalPick.add(Pair.of(pickScanner.nextInt(), pickScanner.nextInt()));
                } else if (tilePick.equals("done") && totalPick.size() > 0)
                    break;
                else {
                    System.out.println("\tInvalid command. Type the row, followed by whitespace and the column.");
                    i--;
                }
            }
        } else {
            System.out.println("You already made your move.");
            throw new UnsupportedOperationException();
        }
        if (totalPick.size() > 0) {
            try {
                pickedTiles.addAll(board.takeTiles(totalPick));
                //System.out.println(pickedTiles);
                do {
                    try {
                        Client.clientHandler.sendingWithRetry(new ChosenTiles(totalPick), ATTEMPTS, WAITING_TIME);
                        message = Client.clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                } while (message == null);
                if (message.getMessageType() == MessageCode.MOVE_LEGAL) {
                    System.out.println("Move was verified.");
                } else
                    System.out.println("Move was not verified.");
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                System.out.println("Try another move.");
                totalPick.clear();
                throw new UnsupportedOperationException();
            }
        }
        return pickedTiles;
    }

    @Override
    public void insertCommand(ArrayList<Tiles> pickedTiles) {
        if (pickedTiles.size() == 0) {
            System.out.println("You have no available tiles to insert.");
            return;
        }
        System.out.println("Type <index> <row> <column> to insert the picked tiles in your shelf.\nType 'cancel' to redo your move.\nYour tiles: ");

        tilePattern = Pattern.compile("[0-2]\\s+[0-5]\\s+[0-4]");
        ArrayList<Pair<Integer, Integer>> finalPositions = new ArrayList<>();
        ArrayList<Tiles> finalTiles = new ArrayList<>();
        ArrayList<Tiles> tempTiles = new ArrayList<>(pickedTiles.size());
        tempTiles.addAll(pickedTiles);

        for (int i = 0; i < pickedTiles.size(); i++) {
            for (int j = 0; j < tempTiles.size(); j++)
                System.out.println("\t" + j + ": " + tempTiles.get(j));
            System.out.print("\t> ");
            String shelfMove = sc.nextLine();
            if (shelfMove.equals("cancel")) {
                return;
            } else if (tilePattern.matcher(shelfMove).find() && totalPick.size() <= 3) {
                Scanner pickScanner = new Scanner(shelfMove);
                int moveIndex = pickScanner.nextInt();
                finalTiles.add(tempTiles.get(moveIndex));
                finalPositions.add(Pair.of(pickScanner.nextInt(), pickScanner.nextInt()));
                tempTiles.remove(moveIndex);
            } else {
                System.out.println("\tUnknown command, try again.");
                i--;
            }
        }
        try {
            Client.player.getShelf().insertTiles(finalPositions, finalTiles);
            pickedTiles.clear();
            System.out.println("Done.");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public boolean doneCommand(){
        if (pickedTiles.size() > 0 || totalPick.size() == 0) {
            System.out.println("You still have to complete your move.");
            return false;
        } else
            return true;
    }
    @Override
    public void unknownCommand(){
        System.out.println("Unknown command.");
    }
    @Override
    public void commonGoalReached(int i, int goalScore){
        System.out.println("You reached common goal " + i + ", gaining " + goalScore + " points.");
    }
    @Override
    public void shelfCompleted(){
        System.out.println("You completed the shelf and gained 1 point.");
    }
    @Override
    public void turnCompleted(){
        System.out.println("You completed your turn.");
    }
    @Override
    public void showWhoIsPlaying(String username) {
        System.out.println(username + " is now playing.");
    }
    @Override
    public void turnNotification(String username){
        totalPick.clear();
        System.out.println("\n" + username + ", it's your turn.");
        System.out.println("Enter a command to play. (type 'help' to see all commands)");
    }
    @Override
    public void someoneReachedCommonGoal(String username, Integer position, Integer points){
        System.out.println(username + " reached goal " + position + ", gaining " + points + " points.");
    }
    @Override
    public void someoneCompletedShelf(String username){
        System.out.println(username + " completed their shelf, obtaining 1 point! \nRemaining players will play their turns before calculating the score and ending the game.");
    }
    @Override
    public void showPersonalGoalAchievement(int points){
        System.out.println("I gained " + points + " points from my personal goal.");
    }
    @Override
    public void finalScore(){
        System.out.println("\nThe game is over.\n");
        ArrayList<Pair<Tiles, Integer>> tileGroups = (ArrayList<Pair<Tiles, Integer>>) Client.player.getShelf().findTileGroups();
        this.showPersonalGoalAchievement(Client.personalGoal.getGoal().checkGoal(Client.player.getShelf()));
        for (Pair<Tiles, Integer> group : tileGroups) {
            int gainedPoints = 0;
            if (group.getSecond() == 3)
                gainedPoints = 2;
            else if (group.getSecond() == 4)
                gainedPoints = 3;
            else if (group.getSecond() == 5)
                gainedPoints = 5;
            else if (group.getSecond() >= 6)
                gainedPoints = 8;
            System.out.println("I gained " + gainedPoints + " points, having made a group of " + group.getSecond() + " tiles.");
        }
    }
    @Override
    public void finalRank(ArrayList<Pair<String, Integer>> playerPoints){
        System.out.println("\nFINAL SCORES:");
        for (int i = 0; i < playerPoints.size(); i++)
            System.out.println(i + 1 + ": " + playerPoints.get(i).getFirst() + " (" + playerPoints.get(i).getSecond() + " points)");
        System.out.println(playerPoints.get(0).getFirst() + " wins!");
    }
}
