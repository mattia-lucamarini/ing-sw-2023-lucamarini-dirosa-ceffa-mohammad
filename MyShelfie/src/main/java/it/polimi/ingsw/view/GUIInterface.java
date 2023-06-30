package it.polimi.ingsw.view;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Shelf;
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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import static it.polimi.ingsw.view.GraphicLogic.board;
/**class: GUIInterface
 * @author Angelo Di Rosa
 * Class containing methods used for communication with the GUI interface.*/
public class GUIInterface {
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private Stage stage;
    //private ViewHandler viewhandler;
    private ConcurrentLinkedQueue<MessageView> sended;
    private ConcurrentLinkedQueue<MessageView> received;
    private String username;
    private boolean ismyturn = false;
    private ArrayList<Pair<Integer,Integer>> mypicks;
    private List<Tiles> pickedTiles;
    private boolean otherShelf = false;
    private boolean takeAlreadyDone = false;

    Pattern tilePattern;
    @FXML
    TextField text;

    /*public GUIInterface(Stage stage, ViewHandler viewhandler) {
        this.stage = stage;
        this.viewhandler = viewhandler;
    }*/

    public GUIInterface(){
        mypicks = new ArrayList<>();
        pickedTiles = new ArrayList<>();
        sended = new ConcurrentLinkedQueue<>();
        received = new ConcurrentLinkedQueue<>();
    }
    /**method: askForUsername()
     * @author Angelo Di Rosa
     * Method used to get username and other login credentials from the GUI. It pops from the queue a message until
     * message!=null and gets the informations contained inside. Then, it returns it to GraphicLogic.
     * */
    public Pair<Pair<String, String>, Pair<String,Integer>> askForUsername() {
        MessageView message;
        do{
             message = received.poll();

        }while(message==null);
        username = ((PayloadUsername) message).getUsername();
        String connection = ((PayloadUsername) message).getConnection();
        String address = ((PayloadUsername) message).getAddress();
        Integer port = ((PayloadUsername) message).getPort();
        return new Pair<>(new Pair<>(username,connection), new Pair<>(address,port));
        //mette il messaggio nella struttura d'uscita e aspetta di riceverlo in entrata.
        //trovato il messaggio e ritorna il valore alla graphic logic.

    }

    /**method: printErrorMessage(String error)
     * @param error a string describing what error occurred.
     * This method changes scene from the latest one in a new one containg a label showing the error.*/
    public void printErrorMessage(String error) {
        MessageView message = new ErrorMessage(error);
        sended.add(message);
    }
    /**method: printMessage(String msg)
     * @param msg a string showing the message.
     * This method adds a popup on the scene showing a message. When clicked anywhere but the popups, it hides.*/
    public void printMessage(String msg) {
        MessageView message = new NotificationMessage(msg);
        sended.add(message);
    }
    /**method: askForNumOfPlayers(ClientHandler cl)
     * @oaram cl instance of a class used for the communication with server.
     * @author Angelo Di Rosa
     * This method waits for a message in the queue, sended bu the GUI. When received, takes the num of players info and
     * sends it to the server.
     * */
    public int askForNumOfPlayers(ClientHandler cl) throws IOException {
        MessageView messageSended = new MessageView(MessageCodeView.NEXT_SCENE);
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
            printErrorMessage("Disconnected from the server before sending NumPlayer Message.");
            return 0;
        }
        if (!flag) {
            printErrorMessage("Can't send the NumPlayer Message.");
            return 0;
        }
        return num;
    }
    /**method : showGameScene()
     * @author Angelo Di Rosa
     * sends a message to the GUI (ViewHandler Class) saying it need to show the game scene*/
    public void showGameScene(){
        MessageView message = new MessageView(MessageCodeView.GAME_SCENE);
        sended.add(message);
    }
    /**method : showPersonalGoal()
     * @author Angelo Di Rosa
     * sends a message to the GUI (ViewHandler Class) saying it need to show the personal goals on the game scene*/
    public void showPersonalGoal(int goalNumber) {
        MessageView message = new ShowPersonal(goalNumber);
        sended.add(message);
    }

    /**method : showCommonGoal()
     * @author Angelo Di Rosa
     * sends a message to the GUI (ViewHandler Class) saying it need to show the common goals on the game scene*/
    public void showCommonGoals(int goalNumber1, int goalNumber2) {
        MessageView message = new ShowCommon(goalNumber1,goalNumber2);
        sended.add(message);
    }
    /**method : waitForPlayersOrder(ClientHandler cl)
     * @param cl instance of class ClientHandler used for communication between server and client.
     * @author Angelo Di Rosa
     * Method used to receive the Player's order from the server.*/
    public ArrayList<String> waitForPlayersOrder(ClientHandler cl) {
        ArrayList<String> playerOrder = null;
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        printMessage("Waiting for other players...");
        while (playerOrder == null) {
            try {
                message = cl.receivingWithRetry(100, 2);
            } catch (NoMessageToReadException e) {
                printErrorMessage("Got an error while waiting for the order of the players.");
            } catch (ClientDisconnectedException e) {
                printErrorMessage("Disconnected from the server while waiting for the order of the players.");
            }

            if (message.getMessageType() == MessageCode.PLAYER_ORDER)
                playerOrder = ((PlayerOrder) message).getOrder();
        }
        return playerOrder;
    }

    /** method: showPlayersOrder(ArrayList<String> order)
     * @param order player's order
     * @author Angelo Di Rosa
     * Method used to tell the GUI to show in a label the Player's order.
     */
    public void showPlayersOrder(ArrayList<String> order) {
        MessageView message = new PlayerOrderView(order);
        sended.add(message);
    }
    /**method: setPlayersInComboBox(ArrayList<String> username)
     * @param username array of players' username.
     * @author Angelo Di Rosa
     * Method used to initialise the ComboBox containing the player's name. The comboBox is sued to show other player's shelves*/
    public void setPlayersInComboBox(ArrayList<String> username){
        MessageView message = new SetComboBox(username);
        sended.add(message);
    }
    /**method: turnNotification(String nowPlaying)
     * @param nowPlaying username of the player who's playing right now
     * @author Angelo Di Rosa
     * Method used to tell the GUI to show a popup notification with who's playing*/
    public void turnNotification(String nowPlaying) {
        MessageView message = new NotificationMessage(nowPlaying + " it's your turn.");
        sended.add(message);
    }

    /**method: getCommand()
     * @author Angelo Di Rosa
     * This method listens for a player's move by popping the "received" queue untill it finds a message != null.
     * If the message code is a COMMAND then it checks which command is by using the switch.
     * If the command is not valid, it calls the printMessage() method asking the gui to show a "Unknown command" popup*/
    public boolean getCommand() {
        MessageView commandlabel = new LabelChange("Type help for more");
        sended.add(commandlabel);
        MessageView message;
        System.out.println("sono nel getCommand e il valore di ismyturn è " +ismyturn);
        boolean canContinue = false;
            do {
                message = received.poll();
                if(message!=null && message.getType()==MessageCodeView.COMMAND){
                    switch (((GetCommand)message).getContent()) {
                        case "help":
                            helpCommand();
                            break;

                        case "take":
                            try{
                            takeCommand();
                            } catch(UnsupportedOperationException e ){
                                break;
                            }
                            break;

                        case "insert":
                            insertCommand();
                            break;
                        case "endturn":
                            ismyturn = doneCommand();
                            MessageView updatemessage = new UpdateBoard(GraphicLogic.board);
                            sended.add(updatemessage);
                            break;
                        default :
                            printMessage("Unknown Command.");
                    }
                }
            } while (ismyturn);
        return canContinue;
    }

    /**method: boardCommand()
     * @author Angelo Di Rosa
     * Method used to tell the GUI to update the board and showing it on the screen.*/
    public void boardCommand() {
        MessageView message = new UpdateBoard(GraphicLogic.board);
        sended.add(message);
    }
    /**method: boardCommand()
     * @author Angelo Di Rosa
     * This method prints a message on the screen showing the different commands.*/
    public void helpCommand() {
        printMessage("""
                                                take: extract the tiles specified by your coordinates
                                                insert: put your tiles into the shelf;
                                                done: end your take/insert move;
                                                end: end your turn.""");
    }

    /**method: takeCommand()
     * @author Angelo Di Rosa
     * This method is called when the getMethod() receves a "take" command. It asks the player which tile he wants to
     * take from the board. if the take was already done, it will be shown on the screen a popup saying:
     * "you already made your move".
     * If the command is not recognized it will show "Unknown command".
     * When the input is correct and it receives a "done" command, it calls board.takeTiles(...) picking tiles from the board.*/
    public void takeCommand() {
        MessageView message;
        tilePattern = Pattern.compile("[0-9]\\s+[0-9]");
        if(takeAlreadyDone){
            printMessage("You already made your move.");
            return;
        }
        else {
            MessageView labelchange = new LabelChange("<row> blankspace <column>");
            sended.add(labelchange);
            do {
                message = received.poll();
                if (message != null) {
                    String command = ((GetCommand) message).getContent().toLowerCase();
                    if (!command.equals("done")) {
                        if (tilePattern.matcher(command).find()) {
                            Scanner pickScanner = new Scanner(command);
                            mypicks.add(Pair.of(pickScanner.nextInt(), pickScanner.nextInt()));

                        } else if (command.equals("cancel")) {
                            mypicks.clear();
                            pickedTiles.clear();
                            MessageView showpicks = new ShowPickedTiles(pickedTiles);
                            sended.add(showpicks);
                            MessageView label = new LabelChange("Take was cancelled.");
                            sended.add(label);
                            return;
                        } else {
                            MessageView unknown = new LabelChange("Unknown command.");
                            sended.add(unknown);
                            return;
                        }
                    } else {
                        if (mypicks == null || mypicks.size() == 0) {
                            printMessage("You still have to make your move.");
                        } else {
                            break;
                        }
                    }
                }
            } while (mypicks.size() < 3);
            MessageView donelabel = new LabelChange("Done.");
            sended.add(donelabel);
            Message generic = new Message(MessageCode.GENERIC_MESSAGE);
            if (mypicks.size() > 0) {
                try {
                    pickedTiles.addAll(board.takeTiles(mypicks));
                    MessageView showpicks = new ShowPickedTiles(pickedTiles);
                    sended.add(showpicks);
                    do {
                        try {
                            GraphicLogic.clientHandler.sendingWithRetry(new ChosenTiles(mypicks), ATTEMPTS, WAITING_TIME);
                            generic = GraphicLogic.clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        } catch (Exception e) {
                            printErrorMessage("Disconnected while sending 'take' move to server");
                            System.exit(13);
                        }
                    } while (generic == null);
                    if (generic.getMessageType() == MessageCode.MOVE_LEGAL) {
                        MessageView update = new UpdateBoard(GraphicLogic.board);
                        sended.add(update);
                        printMessage("Move was verified.");
                        takeAlreadyDone=true;

                    } else{
                        printMessage("Move was not verified.");
                        mypicks.clear();
                        throw new UnsupportedOperationException();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                    printMessage("Try another move.");
                    mypicks.clear();
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
    /**method: insertCommand()
     * @author Angelo Di Rosa
     * This method is called when the getMethod() receves a "insert" command. It asks the player which tile he wants to
     * insert among the selected ones and where to put it in the shelf. if the take was not done beforehand or the insert was already done,
     * it will be shown on the screen a popup saying: "You have no available tiles to insert".
     * If the command is not recognized it will show "Unknown command".
     * When the input is correct and it receives a "done" command or if the tiles where three and the player inserted all of them,
     * it calls shelf.insertTiles(...) inserting tiles onto the shelf. It also communicates the move to the server.*/
    public void insertCommand() {
        List<Tiles> copyofpicked = new ArrayList<>();
        copyofpicked.addAll(pickedTiles);
        MessageView labelchange = new LabelChange("type <index> <row> <column>");
        tilePattern = Pattern.compile("[0-2]\\s+[0-5]\\s+[0-4]");
        sended.add(labelchange);
        MessageView message;
        ArrayList<Tiles> temporaryTiles = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> selectedindexes = new ArrayList<>();
        if (pickedTiles.size() == 0) {
            printMessage("You have no available tiles to insert.");
            return ;
        }
        do {
            message = received.poll();
            if(message!=null){
                String command = ((GetCommand) message).getContent().toLowerCase();
                    if (tilePattern.matcher(command).find()) {
                        Scanner pickScanner = new Scanner(command);
                        int index = pickScanner.nextInt();
                        temporaryTiles.add(pickedTiles.get(index));
                        pickedTiles.remove(index);
                        selectedindexes.add(Pair.of(pickScanner.nextInt(),pickScanner.nextInt()));
                        System.out.println("you inserted: " + selectedindexes);
                        MessageView showpicks = new ShowPickedTiles(pickedTiles);
                        sended.add(showpicks);

                    }
                    else if(command.equals("cancel")){
                        selectedindexes.clear();
                        MessageView label = new LabelChange("Insert was cancelled.");
                        sended.add(label);
                        return;
                    }
                    else{
                        MessageView unknowninsert = new LabelChange("Unknown command");
                        sended.add(unknowninsert);
                        MessageView showpicks = new ShowPickedTiles(pickedTiles);
                        sended.add(showpicks);
                    }
                }
        }while(pickedTiles.size()>0);
        try {
            GraphicLogic.player.getShelf().insertTiles(selectedindexes, temporaryTiles);
            updateShelf();
            Message insertmessage = new Message(MessageCode.GENERIC_MESSAGE);
            do {
                try {
                    //System.out.println("Sending shelf move");
                    GraphicLogic.clientHandler.sendingWithRetry(new Insert(selectedindexes, temporaryTiles),
                            ATTEMPTS, WAITING_TIME);
                    //System.out.println("Sent shelf move");
                    insertmessage = GraphicLogic.clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    //System.out.println("Received answer");
                     pickedTiles.clear();
                     selectedindexes.clear();
                } catch (ClientDisconnectedException e) {
                    printErrorMessage("Disconnected while sending 'insert' move to server");
                    System.exit(13);
                } catch (NoMessageToReadException ignored){}
            } while (insertmessage == null);
            if (insertmessage.getMessageType() == MessageCode.MOVE_LEGAL) {
                printMessage("Move was verified.");
            }
            else{
                printMessage("Move was not verified.");
                pickedTiles.addAll(copyofpicked);
                MessageView showpicks = new ShowPickedTiles(copyofpicked);
                sended.add(showpicks);
            }
            return;
        } catch (RuntimeException e) {
            printMessage(e.getMessage());
            pickedTiles.addAll(copyofpicked);
            MessageView showpicks = new ShowPickedTiles(copyofpicked);
            sended.add(showpicks);
            return;
        }
    }
    /**method: updateShelf()
     * @author Angelo Di Rosa
     * method used to update every player's shelf in the gui.*/
    public void updateShelf(){
        MessageView msg = new ShowTile(GraphicLogic.player.getShelf());
        sended.add(msg);
    }
    /**method: doneCommand()
     * @author Angelo Di Rosa
     * method used to end the player's turn.*/
    public boolean doneCommand() { //END TURN
        MessageView mex;
        if (pickedTiles.size() > 0 || mypicks.size() == 0) {
            printMessage("You still have to complete your move.");
            return true;
        } else
            mypicks.clear();
            mex = new ShowTile(GraphicLogic.player.getShelf());
            sended.add(mex);

            return false;
    }
    /**method: showLoginScreen()
     * @author Angelo Di Rosa
     * Method used to show the login screen again in case of "client refused: choose another username."*/
    public void showLoginScreen(){
        MessageView loginscreen = new MessageView(MessageCodeView.LOGIN_SCREEN);
        sended.add(loginscreen);
    }

    /**method commonGoalReached(int index, int goalScore)
     * @param index index of one of the two common goal on the game scene.
     * @param goalScore point gained from reaching the goal
     * @author Angelo Di Rosa
     * This method is used to update the common goals stacks by showing the score token
     * on the game scene after a player takes it*/
    public void commonGoalReached(int index, int goalScore) {
        MessageView youreachedcommon = new OwnPoints(goalScore,index);
        sended.add(youreachedcommon);
    }

    /**method: shelfCompleted()
     * @author Angelo Di Rosa
     * This method shows a message saying that the player completed the shelf and gained a point*/
    public void shelfCompleted() {
        printMessage("You completed the shelf and gained 1 point.");
    }

    /**method: turnCompleted()
     * @author Angelo Di Rosa
     * This method shows a message saying that the player completed their turn*/
    public void turnCompleted() {
        ismyturn=false;
        takeAlreadyDone=false;
        printMessage("You completed your turn.");
    }

    /**method: showWhoIsPlaying()
     * @author Angelo Di Rosa
     * This method shows a message saying who's playing*/
    public void showWhoIsPlaying(String username) {
        printMessage(username+ "is now playing.");
    }

    /**method: someoneReachedCommonGoal()
     * @author Angelo Di Rosa
     * This method shows a message saying who reached the commong goal and how many points they gained*/
    public void someoneReachedCommonGoal(String username, Integer position, Integer points) {
        MessageView message = new CommonReached(points , position, username);
        sended.add(message);
        printMessage(username + " reached goal " + position + ", gaining " + points + " points.");
    }

    /**method: someoneCompletedShelf()
     * @author Angelo Di Rosa
     * This method shows a message saying who completed their shelf, and that the last round is now starting*/
    public void someoneCompletedShelf(String username) {
        printMessage(username +" completed their shelf, obtaining 1 point! \nRemaining players will play their turns before " +
                "calculating the score and ending the game.");
    }

    /**method: showPersonalGoalAchievement()
     * @author Angelo Di Rosa
     * This method shows a message saying how many points the player reached from the personal goal*/
    public void showPersonalGoalAchievement(int points) {
        printMessage("I gained " + points + " points from my personal goal.");
    }

    /**method: finalScore()
     * @author Angelo Di Rosa
     * This method shows a message saying that the game is over and counts the point for each player.*/
    public void finalScore() {
        printMessage("\nThe game is over. Counting the points\n");
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
        }

    }

    /**method: finalRank()
     * @param playerPoints  an arraylist containing the player's name and their points already
     * ordered from the winner to the last
     * @author Angelo Di Rosa
     * This method sends the players points to the GUI (ViewHandler) that will be shown onto the ranking scene.*/
    public void finalRank(ArrayList<Pair<String, Integer>> playerPoints) {
        for (int i = 0; i < playerPoints.size(); i++)
        System.out.println(playerPoints.get(0).getFirst() + " wins!");
        MessageView message = new FinalRanking(playerPoints);
        sended.add(message);
    }
    /**method: addMessage(MessageView message)
     * @author Angelo Di Rosa
     * This method add the messages onto the "sended" queue so the GUI will know what to do*/
    public void addMessage(MessageView message){
        received.add(message);
    }

    public ConcurrentLinkedQueue<MessageView> getSendedQueue(){
        return sended;
    }
    /**method: forceWin(String username)
     * @param username  username of the only player connected
     * This method communicates to the gui the only left player in the game after others disconnected so the gui can
     * show him as the winner*/
    public void forceWin(String username){
        MessageView message = new ForceWin(username);
        sended.add(message);
    }
    public void setIsmyturn(boolean ismyturn) {
        this.ismyturn = ismyturn;
    }
    public boolean getIsmyturn(){
        return ismyturn;
    }
}
