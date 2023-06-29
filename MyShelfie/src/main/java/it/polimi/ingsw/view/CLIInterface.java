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

/**
 * Defines all the interactive commands used by the CLI.
 * @author Mattia Lucamarini
 */
public class CLIInterface implements UserInterface{
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private static final List<String> goalNames = List.of("6 groups of 2 equal tiles adjacent to each other",
            "4 tiles of the same type in each corner of the shelf", "4 groups of 4 equal tiles adjacent to each other",
            "2 squares of 4 equal tiles each", "3 columns of 6 tiles each having 1,2 or 3 different types",
            "8 equal tiles in any position on the shelf",
            "5 equal tiles diagonally placed",
            "4 rows of 5 tiles each having 1,2 or 3 different types",
            "2 columns each having 6 different tiles",
            "2 rows each having 5 different tiles",
            "5 equal tiles forming a cross",
            "5 columns in increasing or decreasing height forming a staircase with any type of tile");
    private Scanner sc = new Scanner(System.in);
    private ArrayList<Pair<Integer, Integer>> totalPick = new ArrayList<>();
    private ArrayList<Tiles> pickedTiles = new ArrayList<>();
    private List<PersonalGoal> personalGoals = PersonalGoal.all();
    Pattern tilePattern;

    @Override
    public Player askForUsername(){
        String username;
        do{
            System.out.print("\nInsert your username: ");
            username = sc.nextLine();
            if(username.isEmpty())  System.out.print("Null username inserted!\n");
        }
        while(username.isEmpty());

        return new Player(username);
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
        int num = 0;
        System.out.print("\nInsert player number: ");
        while (num == 0) {
            try {
                Scanner t = new Scanner(System.in);
                num = t.nextInt();
                while (num < 2 || num > 4) {
                    System.out.print("Please insert a number between 2 and 4: ");
                    num = t.nextInt();
                }
            } catch (InputMismatchException e) {
                System.out.print("\nPlease insert an actual number: ");
            }
        }
        try {
            clientHandler.sendingWithRetry(new NumPlayersMessage(num), 2, 1);
        } catch (ClientDisconnectedException e) { //here if I took much time to send the request
            System.out.println("Disconnected from the server before sending NumPlayer Message.");
            return 0;
        }
        return num;
    }
    @Override
    public void showPersonalGoal(int goalIndex){
        System.out.println("\n##########################");
        System.out.println("\nYour personal goal is: " + goalIndex);
        for (Map.Entry<Pair<Integer, Integer>, Tiles> entry : personalGoals.get(goalIndex).getConstraint().entrySet()){
            System.out.println("\t" + entry.getValue() + " in (" + entry.getKey().getFirst() + "," + entry.getKey().getSecond() + ")");
        }
    }
    @Override
    public void showCommonGoals(int goal1, int goal2){
        System.out.println("\nThe common goals are:\n\t" + goalNames.get(goal1) + "\n\t" + goalNames.get(goal2));
    }
    @Override
    public ArrayList<String> waitForPlayersOrder(ClientHandler clientHandler){
        ArrayList<String> playerOrder = null;
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        while (playerOrder == null) {
            try {
                message = clientHandler.receivingWithRetry(100, 2);
            } catch (NoMessageToReadException e) {
                printErrorMessage("Error receiving the order of the players..");
                System.exit(1);
            } catch (ClientDisconnectedException e) {
                printErrorMessage("Disconnected while waiting for the order of the players.");
                System.exit(13);
            }

            if (message.getMessageType() == MessageCode.PLAYER_ORDER)
                playerOrder = ((PlayerOrder) message).getOrder();
        }
        return playerOrder;
    }
    @Override
    public void showPlayersOrder(ArrayList<String> order){
        System.out.println("\nOrder of the players:");
        for (String pl : order)
            System.out.println("\t" + pl);
    }
    @Override
    public void showGameStart(){
        System.out.println("\n");
        Client.printCustomMessage("Game started !", "notable");
    }
    @Override
    public String getCommand(String username){
        System.out.print(username + "> ");
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

    /**
     * Makes the player choose the wanted tiles and extracts them from the board
     * @return an ArrayList of the extracted tiles
     * @throws UnsupportedOperationException if the move is not legal
     */
    @Override
    public ArrayList<Tiles> takeCommand() throws UnsupportedOperationException {
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        //checks if the player has already made a move and adds the coordinates to totalPick
        if (totalPick.size() == 0) {
            System.out.println("""
                    Type the coordinates of the tile you want to take (ex. 3 2)
                     Type 'cancel' to redo your move
                     Type 'done' if you are done.""");
            for (int i = totalPick.size(); i < 3 && totalPick.size() < 3; i++) {
                System.out.print("\t" + (i + 1) + "> ");
                String tilePick = sc.nextLine();
                tilePattern = Pattern.compile("[0-9]\\s+[0-9]");    //regex used for accepting the coordinates (two numbers between 0 and 9)
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
                pickedTiles.addAll(board.takeTiles(totalPick)); //actually extracts the tiles
                //System.out.println(pickedTiles);
                do {
                    try {   //sends the move to server
                        Client.clientHandler.sendingWithRetry(new ChosenTiles(totalPick), ATTEMPTS, WAITING_TIME);
                        message = Client.clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e) {
                        System.out.println("Disconnected while sending 'take' move to server");
                        System.exit(13);
                    } catch (NoMessageToReadException ignored){}
                } while (message == null);
                //receives the check result from the server
                if (message.getMessageType() == MessageCode.MOVE_LEGAL) {
                    System.out.println("Move was verified.");
                } else if (message.getMessageType() == MessageCode.MOVE_ILLEGAL)
                    throw new RuntimeException(((IllegalMove) message).getReason());
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                System.out.println("Try another move.");
                totalPick.clear();
                throw new UnsupportedOperationException();
            }
        }
        return pickedTiles;
    }

    /**
     * Manages the player insertion of tiles into their shelf, and sends the resulting move to the server.
     * @param pickedTiles an ArrayList of the tiles about to be inserted.
     * @return a boolean value representing the success of the operation
     */
    @Override
    public boolean insertCommand(ArrayList<Tiles> pickedTiles) {
        if (pickedTiles.size() == 0) {
            System.out.println("You have no available tiles to insert.");
            //pickedTiles.forEach(System.out::println);
            return false;
        }
        System.out.println("""
                Type <index> <row> <column> to insert the picked tiles in your shelf.
                Type 'cancel' to redo your move.
                Your tiles:\s""");

        tilePattern = Pattern.compile("[0-2]\\s+[0-5]\\s+[0-4]");   //this regex accepts the tile index, followed by the row and column numbers.
        //these two ArrayList represent the final move which will be checked
        ArrayList<Pair<Integer, Integer>> finalPositions = new ArrayList<>();
        ArrayList<Tiles> finalTiles = new ArrayList<>();
        //a temporary representation of the tiles to insert which will be resetted if the move is not accepted
        ArrayList<Tiles> tempTiles = new ArrayList<>(pickedTiles.size());
        tempTiles.addAll(pickedTiles);

        for (int i = 0; i < pickedTiles.size(); i++) {
            for (int j = 0; j < tempTiles.size(); j++)
                System.out.println("\t" + j + ": " + tempTiles.get(j));
            System.out.print("\t> ");
            String shelfMove = sc.nextLine();
            if (shelfMove.equals("cancel")) {
                return false;
            } else if (tilePattern.matcher(shelfMove).find() && totalPick.size() <= 3) {
                Scanner pickScanner = new Scanner(shelfMove);
                int moveIndex = pickScanner.nextInt();
                finalTiles.add(tempTiles.get(moveIndex));
                finalPositions.add(Pair.of(pickScanner.nextInt(), pickScanner.nextInt()));
                tempTiles.remove(moveIndex);
            } else {
                System.out.println("\tUnrecognized format, try again.");
                i--;
            }
        }
        try {
            Client.player.getShelf().insertTiles(finalPositions, finalTiles);
            Message message = new Message(MessageCode.GENERIC_MESSAGE);
            do {
                try {
                    //System.out.println("Sending shelf move");
                    Client.clientHandler.sendingWithRetry(new Insert(finalPositions, finalTiles), ATTEMPTS, WAITING_TIME);
                    //System.out.println("Sent shelf move");
                    message = Client.clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    //System.out.println("Received answer");
                    pickedTiles.clear();
                } catch (ClientDisconnectedException e) {
                    System.out.println("Disconnected while sending 'insert' move to server");
                    System.exit(13);
                } catch (NoMessageToReadException ignored){}
            } while (message == null);
            if (message.getMessageType() == MessageCode.MOVE_LEGAL) {
                System.out.println("Move was verified.");
            } else if (message.getMessageType() == MessageCode.MOVE_ILLEGAL)
                throw new RuntimeException(((IllegalMove) message).getReason());
            return true;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return false;
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
        System.out.println(username + " completed their shelf, obtaining 1 point! " +
                "\nRemaining players will play their turns before calculating the score and ending the game.");
    }
    @Override
    public void showPersonalGoalAchievement(int points){
        System.out.println("I gained " + points + " points from my personal goal.");
    }

    /**
     * Calculates and prints the final score.
     */
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

    /**
     * Prints all the player's scores.
     * @param playerPoints an ArrayList containing the actual scores.
     */
    @Override
    public void finalRank(ArrayList<Pair<String, Integer>> playerPoints){
        System.out.println("\nFINAL SCORES:");
        for (int i = 0; i < playerPoints.size(); i++)
            System.out.println(i + 1 + ": " + playerPoints.get(i).getFirst() + " (" + playerPoints.get(i).getSecond() + " points)");
        System.out.println(playerPoints.get(0).getFirst() + " wins!");
    }
}
