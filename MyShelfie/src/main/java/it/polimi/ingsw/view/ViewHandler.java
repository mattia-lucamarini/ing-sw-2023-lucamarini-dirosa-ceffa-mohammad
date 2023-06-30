package it.polimi.ingsw.view;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Cell;
import it.polimi.ingsw.view.MessageView.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**class: ViewHandler
 * @author Angelo Di Rosa
 * This is the controller of the gui.
 * It contains the methods that responds to the action events, showing something or sending a message to the graphic logic*/
public class ViewHandler {
    private int flag=-1, nump;
    private String username;
    private String conn;
    private Stage stage;
    private boolean answer = false;
    private static Object lock = new Object();
    private GUIInterface gui;
    private GraphicLogic glogic;
    private ConcurrentLinkedQueue<MessageView> sendedfromgui;
    private View view;
    private Cell[][] grid;
    private int count=0;
    @FXML
    TextField text,move, textaddress, textport;
    @FXML
    RadioButton socket, rmi, two, three, four;
    @FXML
    Button donebutton, endturn, takebutton, insertbutton, loginbutton, exit;
    @FXML
    ToggleGroup connection, numplayers;
    @FXML
    Pane gamelayout;
    @FXML
    AnchorPane finalscene;
    @FXML
    GridPane boardgrid;
    @FXML
    GridPane shelfgrid;
    @FXML
    ImageView stack1, stack2, playerpoints, commongoal1, commongoal2, takentile0, takentile1,getTakentile2;
    @FXML
    Label l1, whattodo,playerorder, addresslabel, portlabel,
            winner, second, third, fourth, ptwo , pthree, pfour, connectionresult;
    @FXML
    ComboBox comboBox;

    /**constructor: ViewHandler(Stage stage, View view)
     * @param stage used to set the scenes on it.
     * @param view reference of the class that instanciated it
     *The constructor starts two threads: one thread makes the graphic logic init() method starts,
     * the other one iterates on the "received" queue always looking for a message. When the message is found,
     * it gets the type and does things accordingly. Receiving a Message mostly means that this thread needs to update the
     * GUI by running a Platform.runlater thread. The messages received in fact can ask to show a label, to set a new/old
     * scene on stage, to show images and so on.*/
    public ViewHandler(Stage stage, View view){
        this.gui = new GUIInterface();
        this.glogic = new GraphicLogic(gui);
        this.stage = stage;
        this.view = view;
        boolean partitainiziata = true;
        Thread t = new Thread(()->{glogic.init();});
        t.start();
        Thread kernel = new Thread(()->{
            MessageView message;
            sendedfromgui = gui.getSendedQueue();
            do{
                message = sendedfromgui.poll();
            if(message != null){
            System.out.println("sono viewHandler e ho trovato un messaggio di tipo: " + message.getType());
            switch (message.getType()){
                case LOGIN_SCREEN:
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/login.fxml")));
                        loader.setController(this);
                        Parent login = null;
                        try {
                            login = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Scene scene1 = new Scene(login, 480, 340);
                        scene1.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(scene1);
                        stage.show();});
                    break;
                case NEXT_SCENE:
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/numberOfPlayers.fxml")));
                        loader.setController(this);
                        Parent numPlayers = null;
                        try {
                            numPlayers = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Scene scene2 = new Scene(numPlayers, 480, 340);
                        scene2.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(scene2);
                        stage.show();});
                    break;

                case ERROR_MESSAGE:
                    String error = ((ErrorMessage)message).getLabel();
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
                        loader.setController(this);
                        Parent errorscene = null;
                        try {
                            errorscene = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        l1.setText(error);
                        l1.setAlignment(Pos.TOP_CENTER);
                        Scene scene4 = new Scene(errorscene,700 , 370);
                        scene4.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(scene4);
                        stage.show();});
                        break;
                case GAME_SCENE:
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/game.fxml")));
                        loader.setController(this);
                        Parent gamescene = null;
                        System.out.println("eccomi");
                        try {
                            gamescene = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        donebutton = (Button) gamelayout.getChildren().get(11);
                        donebutton.setVisible(false);
                        whattodo.setVisible(false);
                        move.setVisible(false);
                        takebutton.setVisible(false);
                        insertbutton.setVisible(false);
                        endturn.setVisible(false);
                        playerpoints = (ImageView) gamelayout.getChildren().get(7);
                        playerpoints.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm()));
                        boardgrid = (GridPane) gamelayout.getChildren().get(0);
                        stack1 = (ImageView) gamelayout.getChildren().get(4);
                        stack2 = (ImageView) gamelayout.getChildren().get(5);
                        stack1.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_8.jpg").toExternalForm()));
                        stack2.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_8.jpg").toExternalForm()));

                        Scene scene5 = new Scene(gamescene, 804, 666);
                        scene5.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(scene5);
                        stage.show();
                    });
                    break;
                case GENERIC_MESSAGE:
                    String msg = ((NotificationMessage) message).getText();
                    Platform.runLater(()->{
                        Popup popup = new Popup();
                        popup.setAutoHide(true);
                        Label label = new Label(msg + "\nClick anywhere to close.");
                        label.setAlignment(Pos.CENTER);
                        label.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        label.setStyle(" -fx-background-color: white;");
                        label.setMinWidth(90);
                        label.setMinHeight(60);
                        popup.getContent().add(label);
                        if(!popup.isShowing()){
                            popup.show(stage);
                        }
                    });
                    break;
                case PERSONAL_GOAL:
                    int index = ((ShowPersonal) message).getGoalIndex();
                    Platform.runLater(()-> {
                        ImageView pgoal = (ImageView) gamelayout.getChildren().get(6);
                        switch(index){
                            case 0:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals.png").toExternalForm()));
                                break;
                            case 1:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals2.png").toExternalForm()));
                                break;
                            case 2:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals3.png").toExternalForm()));
                                break;
                            case 3:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals4.png").toExternalForm()));
                                break;
                            case 4:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals5.png").toExternalForm()));
                                break;
                            case 5:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals6.png").toExternalForm()));
                                break;
                            case 6:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals7.png").toExternalForm()));
                                break;
                            case 7:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals8.png").toExternalForm()));
                                break;
                            case 8:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals9.png").toExternalForm()));
                                break;
                            case 9:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals10.png").toExternalForm()));
                                break;
                            case 10:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals11.png").toExternalForm()));
                                break;
                            case 11:
                                pgoal.setImage(new Image(getClass().getResource("/assets/personal goal cards/Personal_Goals12.png").toExternalForm()));
                                break;
                        }
                    });
                    break;
                case PLAYER_ORDER:
                    ArrayList<String> players = ((PlayerOrderView)message).getOrder();
                    Platform.runLater(()->{
                        String order = players.toString();
                        playerorder = (Label) gamelayout.getChildren().get(8);
                        playerorder.setText("The order is: "+order);
                    });
                    break;
                case COMMON_GOAL:
                    int i = ((ShowCommon) message).getGoalIndex().getFirst();
                    int j = ((ShowCommon) message).getGoalIndex().getSecond();
                    Platform.runLater(()-> {
                        ImageView cgoal1 = (ImageView) gamelayout.getChildren().get(2);
                        ImageView cgoal2 = (ImageView) gamelayout.getChildren().get(3);
                        if(i == 0 || j == 0){
                            if(i==0){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/4.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/4.jpg").toExternalForm()));
                            }
                        }
                        if(i == 1 || j == 1){
                            if(i==1){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/8.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/8.jpg").toExternalForm()));
                            }
                        }
                        if(i == 2|| j == 2){
                            if(i==2){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/3.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/3.jpg").toExternalForm()));
                            }
                        }
                        if(i == 3 || j == 3){
                            if(i==3){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/1.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/1.jpg").toExternalForm()));
                            }
                        }
                        if(i == 4 || j == 4){
                            if(i==4){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/5.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/5.jpg").toExternalForm()));
                            }
                        }
                        if(i == 5 || j == 5){
                            if(i==5){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/9.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/9.jpg").toExternalForm()));
                            }
                        }
                        if(i == 6 || j == 6){
                            if(i==6){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/11.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/11.jpg").toExternalForm()));
                            }
                        }
                        if(i == 7 || j == 7){
                            if(i==7){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/7.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/7.jpg").toExternalForm()));
                            }
                        }
                        if(i == 8 || j == 8){
                            if(i==8){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/2.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/2.jpg").toExternalForm()));
                            }
                        }
                        if(i == 9 || j == 9){
                            if(i==9){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/6.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/6.jpg").toExternalForm()));
                            }
                        }
                        if(i == 10 || j == 10){
                            if(i==10){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/10.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/10.jpg").toExternalForm()));
                            }
                        }
                        if(i == 11 || j == 11){
                            if(i==11){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/12.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/12.jpg").toExternalForm()));
                            }
                        }
                    });
                    break;
                case INIT_COMBOBOX:
                    ArrayList<String> usernames = ((SetComboBox) message).getComboItems();
                    Platform.runLater(
                            ()->{
                        comboBox = (ComboBox) gamelayout.getChildren().get(16);
                        ObservableList<String> items = FXCollections.observableList(usernames);
                        comboBox.setItems(items);
                    });
                    break;

                case SHOW_TILE:
                    Shelf shelf = ((ShowTile) message).getShelf();
                    Platform.runLater(()->{
                        int k = 0;
                        for(int r = 0 ; r < 6; ++r){
                            for(int c = 0 ; c < 5 ; ++c){
                                int m = 25-(5*r)+c;
                                ImageView imgvw = (ImageView) shelfgrid.getChildren().get(m);
                                imgvw.setImage(selectImageFromTile(shelf.getTile(r,c)));
                            }
                        }
                    });
                    break;
                case COMMON_REACHED:
                    int numplayers = glogic.getNumPlayers();
                    int numgoal = ((CommonReached) message).getNumgoal();
                    System.out.println(numgoal);
                    int points = ((CommonReached)message).getPoints();
                    String username =((CommonReached)message).getUsername();

                   Platform.runLater(()->{
                       if(numgoal == 1){
                           stack2 = (ImageView) gamelayout.getChildren().get(5);
                           stack2.setImage(selectImageFromPointsAndNumberPlayers(points, numplayers));
                       }
                       else{
                           stack1 = (ImageView) gamelayout.getChildren().get(4);
                           stack1.setImage(selectImageFromPointsAndNumberPlayers(points, numplayers));
                       }

                    });
                    break;
                case UPDATE_PERSONALSTACK:
                    int nump = glogic.getNumPlayers();
                    int pointsgained = ((OwnPoints)message).getPoints();
                    int goalnum = ((OwnPoints)message).getIndex();
                    Platform.runLater(()->{
                        ImageView stack;
                        if(goalnum == 0){
                            stack = (ImageView) boardgrid.getChildren().get(4);
                        }
                        else{
                            stack = (ImageView) boardgrid.getChildren().get(5);
                        }
                        stack.setImage(selectImageFromPointsAndNumberPlayers(pointsgained,nump));
                        playerpoints= (ImageView) gamelayout.getChildren().get(7);
                        playerpoints.setImage(selectImageFromPoints(pointsgained));
                    });
                    break;

                case UPDATE_BOARD:
                    grid = ((UpdateBoard) message).getBoard().getGrid();
                    Platform.runLater(()->{
                        int k=0;
                        if(gui.getIsmyturn()){
                            takebutton.setVisible(true);
                            insertbutton.setVisible(true);
                            endturn.setVisible(true);
                            donebutton.setVisible(true);
                            whattodo.setVisible(true);
                            move.setVisible(true);

                        }
                        else{
                            takebutton.setVisible(false);
                            insertbutton.setVisible(false);
                            endturn.setVisible(false);
                            donebutton.setVisible(false);
                            whattodo.setVisible(false);
                            move.setVisible(false);
                        }
                        for(int r = 0 ; r < 9; ++r){
                            for(int c = 0 ; c < 9 ; ++c){
                                ImageView imagvw = (ImageView) boardgrid.getChildren().get(k);
                                imagvw.setImage(selectImageFromTile(grid[r][c].getTile()));
                                ++k;
                            }
                        }
                    });
                    break;
                case LABEL_CHANGE:
                    String texttoshow = ((LabelChange)message).getText();
                    Platform.runLater(()->{
                        whattodo = (Label) gamelayout.getChildren().get(10);
                        whattodo.setText(texttoshow);
                    });

                    break;
                case PICKED_TILES:
                    List<Tiles> tiles = ((ShowPickedTiles)message).getPickedTiles();
                    Platform.runLater(()->{
                        int l=0;
                        for(Tiles tile : tiles){
                            ImageView imgvw = (ImageView) gamelayout.getChildren().get(13+l);
                            imgvw.setImage(selectImageFromTile(tile));
                            ++l;
                        }
                        if(tiles.size()==2){
                            ImageView pickedtile2 = (ImageView) gamelayout.getChildren().get(15);
                            pickedtile2.setImage(null);
                        }
                        else if(tiles.size()==1){
                            ImageView pickedtile1 = (ImageView) gamelayout.getChildren().get(14);
                            pickedtile1.setImage(null);
                            ImageView pickedtile2 = (ImageView) gamelayout.getChildren().get(15);
                            pickedtile2.setImage(null);
                        }
                        else if(tiles.size()==0){
                            ImageView pickedtile0 = (ImageView) gamelayout.getChildren().get(13);
                            pickedtile0.setImage(null);
                            ImageView pickedtile1 = (ImageView) gamelayout.getChildren().get(14);
                            pickedtile1.setImage(null);
                            ImageView pickedtile2 = (ImageView) gamelayout.getChildren().get(15);
                            pickedtile2.setImage(null);
                        }
                    });
                    break;
                case SHOW_FINALRANKS:
                    ArrayList<Pair<String, Integer>> rankings = ((FinalRanking) message).getRanks();
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/finalranks.fxml")));
                        loader.setController(this);
                        Parent finalranks = null;
                        try {
                            finalranks = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ptwo = (Label) finalscene.getChildren().get(6);
                        pthree = (Label) finalscene.getChildren().get(7);
                        pfour = (Label) finalscene.getChildren().get(8);
                        second = (Label) finalscene.getChildren().get(2);
                        third = (Label) finalscene.getChildren().get(3);
                        fourth = (Label) finalscene.getChildren().get(4);
                        exit = (Button) finalscene.getChildren().get(9);
                        exit.setAlignment(Pos.BOTTOM_CENTER);
                        if(rankings.size()==1){
                            ptwo.setVisible(false);
                            second.setVisible(false);
                            pthree.setVisible(false);
                            third.setVisible(false);
                            pfour.setVisible(false);
                            fourth.setVisible(false);
                        }
                        if(rankings.size()==2){
                            pthree.setVisible(false);
                            third.setVisible(false);
                            pfour.setVisible(false);
                            fourth.setVisible(false);
                        }
                        else if(rankings.size()==3){
                            pfour.setVisible(false);
                            fourth.setVisible(false);
                        }
                        for (int m = 0; m < rankings.size(); m++){
                            Label label = (Label) finalscene.getChildren().get(m+1);
                            label.setText(rankings.get(m).getFirst()+ ":" +rankings.get(m).getSecond()+ "points");
                        }
                        Scene ranksscene = new Scene(finalranks, 600, 400);
                        ranksscene.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(ranksscene);
                        stage.show();});
                    break;
                case FORCE_WIN:
                    String wins = ((ForceWin)message).getUsername();
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/finalranks.fxml")));
                        loader.setController(this);
                        Parent forced = null;
                        try {
                            forced = loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        winner=(Label) finalscene.getChildren().get(1);
                        ptwo = (Label) finalscene.getChildren().get(6);
                        pthree = (Label) finalscene.getChildren().get(7);
                        pfour = (Label) finalscene.getChildren().get(8);
                        second = (Label) finalscene.getChildren().get(2);
                        third = (Label) finalscene.getChildren().get(3);
                        fourth = (Label) finalscene.getChildren().get(4);
                        exit = (Button) finalscene.getChildren().get(9);
                        exit.setAlignment(Pos.BOTTOM_CENTER);
                        ptwo.setVisible(false);
                        second.setVisible(false);
                        pthree.setVisible(false);
                        third.setVisible(false);
                        pfour.setVisible(false);
                        fourth.setVisible(false);
                        winner.setText(wins);
                        Scene forcedwin = new Scene(forced, 600, 400);
                        forcedwin.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(forcedwin);
                        stage.show();
                    });
                    break;
            }
            }}while(partitainiziata);
        });
        kernel.start();
    }
    /**method: login
     * @author Angelo Di Rosa
     * This is the Action Listener for the first login scene(Username/Connection type)*/
    public void login(ActionEvent e) throws IOException {
        int port;
        username = text.getText();
        if(connection.getSelectedToggle()==socket){
            conn = "socket";
        }
        else {
            conn = "rmi";
        }

        System.out.println("4");
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/addressport.fxml")));
        loader.setController(this);
        Parent addressport = null;
        try {
            addressport = loader.load();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        Scene addressportscene = new Scene(addressport, 600, 400);
        addressportscene.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage = (Stage)(((Node)e.getSource()).getScene().getWindow());
        stage.setScene(addressportscene);
        stage.show();
    }
    /**method: numberOfPlayersController
     * @author Angelo Di Rosa
     * This is the Action Listener for the number of players scene*/
    public void numberOfPlayersController(ActionEvent e) throws IOException {
        if(numplayers.getSelectedToggle()==two){
            nump=2;
        }
        else if(numplayers.getSelectedToggle()==three){
            nump=3;
        }
        else if(numplayers.getSelectedToggle()==four){
            nump=4;
        }
        Parent waiting = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
        Scene scene5 = new Scene(waiting, 529, 480);
        scene5.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage.setScene(scene5);
        stage.show();
        MessageView message = new NumPlayers(nump);
        gui.addMessage(message);

    }
    /*public void done(ActionEvent e){
        MessageView message = new Done();
        gui.addMessage(message);
    }*/
    /**method: showAddressPort
     * @author Angelo Di Rosa
     * This is the Action Listener for the second part of the login screen (Address/Port selection)*/
    public void showAddressPort(ActionEvent e){
        String port=null;
        String address=null;
        Scanner sc;
        Scanner sc1;
        Integer portchoice = null;
        try{
            address = textaddress.getText();
            sc = new Scanner(address);
            address = sc.nextLine();
        }catch(Exception exception){}
        address = (address.isEmpty()) ? "127.0.0.1" : address;
        String portstring;
        try{
                portstring = textport.getText();
            sc1 = new Scanner(portstring);
            port = sc1.nextLine();
        }catch(Exception exception){}
        try{
            if(conn.equals("socket")){
                  if(port.isEmpty()){
                    portchoice = 59090;
                  }
                 else{
                     portchoice = Integer.parseInt(port);} 
            }
            else if(conn.equals("rmi")){
                  if(port.isEmpty()){
                      portchoice = 1099;
                  }
                  else{
                      portchoice = Integer.parseInt(port);
                  }
            }
        } catch(Exception exception){
                if(conn.equals("socket")){
                     portchoice=59090;
                }
                 else{
                    portchoice=1099;
                 }
        }
        connectionresult.setText("Connection param : " + address +" "+ portchoice);
        MessageView message = new PayloadUsername(username,conn,address,portchoice);
        gui.addMessage(message);
    }
    /**method: endTurn
     * @author Angelo Di Rosa
     * This is the Action Listener for the END TURN button in the game scene*/
    public void endTurn(ActionEvent e){
        MessageView message = new GetCommand("endturn");
        gui.addMessage(message);
    }
    /**method: send
     * @author Angelo Di Rosa
     * This is the Action Listener for the SEND button used to send a move*/
    public void send(ActionEvent e){ //to stop choosing from the board
        if(gui.getIsmyturn()){
            String string = move.getText();
            move.clear();
            MessageView message = new GetCommand(string);
            gui.addMessage(message);}
        else{
            gui.printMessage("it's not your turn.");
        }
    }
    /**method: select
     * @author Angelo Di Rosa
     * This is the Action Listener for the selection of the player's shelf to show */
    public void select(ActionEvent e){
        Shelf shelf;
        String username = comboBox.getSelectionModel().getSelectedItem().toString();
        if(username.equals(glogic.player.getUsername())){
             shelf = glogic.player.getShelf();
        }
        else{
             shelf = glogic.playerShelves.get(username);
        }
        int k = 0;
        shelf.printShelf();
        for(int r = 0 ; r < 6; ++r){
            for(int c = 0 ; c < 5 ; ++c){
                int m = 25-(5*r)+c;
                ImageView imgvw = (ImageView) shelfgrid.getChildren().get(m);
                switch(shelf.getTile(r,c)){
                        case BLUE :
                        imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/Cornici1.1.png").toExternalForm()));
                        ++k;
                        break;
                        case WHITE :
                            imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/Libri1.1.png").toExternalForm()));
                            ++k;
                            break;
                        case GREEN :
                            imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/Gatti1.1.png").toExternalForm()));
                            ++k;
                            break;
                        case LIGHTBLUE :
                            imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/Trofei1.1.png").toExternalForm()));
                            ++k;
                            break;
                        case PURPLE :
                            imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/Piante1.1.png").toExternalForm()));
                            ++k;
                            break;
                        case YELLOW :
                            imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/Giochi1.1.png").toExternalForm()));
                            ++k;
                            break;
                        case VALID :
                            imgvw.setImage(new Image(getClass().getResource("/free_resources/valid.png").toExternalForm()));
                            ++k;
                            break;
                        case NOTVALID:
                            imgvw.setImage(new Image(getClass().getResource("/free_resources/notvalid.png").toExternalForm()));
                            ++k;
                            break;
                }
            }
        }
    }
    /**method: insertCommand
     * @author Angelo Di Rosa
     * This is the Action Listener insert button*/
    public void insertCommand(ActionEvent e){
        MessageView message = new GetCommand("insert");
        gui.addMessage(message);
    }
    /**method: takeCommand
     * @author Angelo Di Rosa
     * This is the Action Listener for the take command*/
    public void takeCommand(ActionEvent e){
        MessageView message = new GetCommand("take");
        gui.addMessage(message);
    }
    /**method: selectImageFromTile(Tiles tile)
     * @param tile
     * @author Angelo Di Rosa
     * returns the image corrisponding to the tiles passed as parameter*/
    public Image selectImageFromTile(Tiles tile){
        Image image;
        switch(tile){
            case BLUE :
                image= new Image(getClass().getResource("/assets/item tiles/Cornici1.1.png").toExternalForm());
                break;
            case WHITE :
                image= new Image(getClass().getResource("/assets/item tiles/Libri1.1.png").toExternalForm());
                break;
            case GREEN :
                image= new Image(getClass().getResource("/assets/item tiles/Gatti1.1.png").toExternalForm());
                break;
            case LIGHTBLUE :
                image= new Image(getClass().getResource("/assets/item tiles/Trofei1.1.png").toExternalForm());
                break;
            case PURPLE :
                image= new Image(getClass().getResource("/assets/item tiles/Piante1.1.png").toExternalForm());
                break;
            case YELLOW :
                image= new Image(getClass().getResource("/assets/item tiles/Giochi1.1.png").toExternalForm());
                break;
            case VALID :
                image= new Image(getClass().getResource("/free_resources/valid.png").toExternalForm());
                break;
            case NOTVALID:
                image= new Image(getClass().getResource("/free_resources/notvalid.png").toExternalForm());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tile);
        }
        return image;
    }

    public Image selectImageFromPoints(int points){
        Image image;
        switch(points) {
            case 2 -> image = new Image(getClass().getResource("/assets/scoring tokens/scoring_2.jpg").toExternalForm());
            case 4 -> image = new Image(getClass().getResource("/assets/scoring tokens/scoring_4.jpg").toExternalForm());
            case 6 -> image = new Image(getClass().getResource("/assets/scoring tokens/scoring_6.jpg").toExternalForm());
            case 8 -> image = new Image(getClass().getResource("/assets/scoring tokens/scoring_8.jpg").toExternalForm());
            default -> throw new IllegalStateException("Unexpected value: "+points );
        }
        return image;
    }
    /**method: selectImageFromPointsAndNumber(int points, int numplayers)
     * @param points the points a player gained.
     * @param numplayers  number of players
     * @author Angelo Di Rosa
     * returns the image corrisponding to the scoring token to show after a player took one from the game scene*/
    public Image selectImageFromPointsAndNumberPlayers(int points, int numplayers){
        Image image;
        if(numplayers==2 && points==8){
            image = new Image(getClass().getResource("/assets/scoring tokens/scoring_4.jpg").toExternalForm());
        }
        else if(numplayers==2 && points==4){
            image = new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm());
        }
        else if(numplayers >=3 && points==8){
            image =new Image(getClass().getResource("/assets/scoring tokens/scoring_6.jpg").toExternalForm());
        }
        else if(numplayers >= 3 && points==6){
            image =new Image(getClass().getResource("/assets/scoring tokens/scoring_4.jpg").toExternalForm());
        }
        else if(numplayers==3 && points==4){
            image =new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm());

        }
        else if( numplayers==4 && points==4){
            image =new Image(getClass().getResource("/assets/scoring tokens/scoring_2.jpg").toExternalForm());
        }
        else if(numplayers==4 && points==2){
            image =new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm());

        }
        else {
             throw new IllegalStateException("Unexpected values : " +points + numplayers);
        }
        return image;
    }
    /**method: exit
     * @author Angelo Di Rosa
     * action listener for the EXIT button in the final ranking scene*/
    public void exit(ActionEvent e){
        System.exit(13);
    }
}
