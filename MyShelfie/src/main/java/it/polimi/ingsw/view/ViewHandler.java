package it.polimi.ingsw.view;

import com.sun.javafx.scene.control.skin.FXVK;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Cell;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.view.MessageView.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import javax.management.Notification;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;


public class ViewHandler {
    private int flag=-1, nump;
    private String username;
    private String conn;
    private Stage stage;
    private boolean answer = false;
    private static Object lock = new Object();
    private GUIClient client;
    private GUIInterface gui;
    private GraphicLogic glogic;
    private ConcurrentLinkedQueue<MessageView> sendedfromgui;
    private View view;
    private Cell[][] grid;
    private int count=0;
    @FXML
    TextField text,move;
    @FXML
    RadioButton socket, rmi, two, three, four;
    @FXML
    Button button, donebutton, showplayerShelf;
    @FXML
    ToggleGroup connection, numplayers;
    @FXML
    Pane gamelayout;
    @FXML
    GridPane boardgrid;
    @FXML
    GridPane shelfgrid;
    @FXML
    ImageView commongoal1;
    @FXML
    ImageView commongoal2;
    @FXML
    ImageView stack1, stack2, iw1;
    @FXML
    Label l1, whattodo;


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
            System.out.println("sono viewHandler e ho trovato un messaggio di tipo" + message.getType());
            switch (message.getType()){
                case LOGIN_SCREEN:
                    Platform.runLater(()->{
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/login.fxml")));
                        //GUIClient client = new GUIClient(this);
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
                        l1.setAlignment(Pos.CENTER);
                        Scene scene4 = new Scene(errorscene, 480, 400);
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
                        boardgrid = (GridPane) gamelayout.getChildren().get(0);
                        stack1 = (ImageView) gamelayout.getChildren().get(4);
                        stack2 = (ImageView) gamelayout.getChildren().get(5);
                        stack1.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_8.jpg").toExternalForm()));
                        stack2.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_8.jpg").toExternalForm()));

                        Scene scene5 = new Scene(gamescene, 480, 529);
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
                        Label label = new Label();
                        label.setText("The order is: "+order);
                        Popup popup = new Popup();
                        popup.setAutoHide(true);
                        popup.getContent().add(label);
                        if(!popup.isShowing()){
                            popup.show(stage);
                        }
                        else{
                            popup.hide();
                        }
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
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/1.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/1.jpg").toExternalForm()));
                            }
                        }
                        if(i == 1 || j == 1){
                            if(i==1){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/2.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/2.jpg").toExternalForm()));
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
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/4.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/4.jpg").toExternalForm()));
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
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/6.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/6.jpg").toExternalForm()));
                            }
                        }
                        if(i == 6 || j == 6){
                            if(i==6){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/7.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/7.jpg").toExternalForm()));
                            }
                        }
                        if(i == 7 || j == 7){
                            if(i==7){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/8.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/8.jpg").toExternalForm()));
                            }
                        }
                        if(i == 8 || j == 8){
                            if(i==8){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/9.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/9.jpg").toExternalForm()));
                            }
                        }
                        if(i == 9 || j == 9){
                            if(i==9){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/10.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/10.jpg").toExternalForm()));
                            }
                        }
                        if(i == 10 || j == 10){
                            if(i==10){
                                cgoal1.setImage(new Image(getClass().getResource("/assets/common goal cards/11.jpg").toExternalForm()));
                            }
                            else{
                                cgoal2.setImage(new Image(getClass().getResource("/assets/common goal cards/11.jpg").toExternalForm()));
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
                case SHOW_TILE:
                    Shelf shelf = ((ShowTile) message).getShelf();
                    Platform.runLater(()->{
                        int k = 0;
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
                                        imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/valid.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case NOTVALID:
                                        imgvw.setImage(new Image(getClass().getResource("/assets/item tiles/valid.png").toExternalForm()));
                                        ++k;
                                        break;
                                }
                            }
                        }
                    });
                    break;
                case COMMON_REACHED:
                    int numplayers = glogic.getNumPlayers();
                    int numgoal = ((CommonReached) message).getNumgoal();
                    int points = ((CommonReached)message).getPoints();
                   Platform.runLater(()->{
                       ImageView stack;
                       if(numgoal == 0){
                            stack = (ImageView) boardgrid.getChildren().get(4);
                       }
                       else{
                           stack = (ImageView) boardgrid.getChildren().get(5);
                       }
                       if(numplayers==2 && points==8){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_4.jpg").toExternalForm()));
                       }
                       else if(numplayers==2 && points==4){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm()));
                       }
                       else if(numplayers >=3 && points==8){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_6.jpg").toExternalForm()));
                       }
                       else if(numplayers >= 3 && points==6){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_4.jpg").toExternalForm()));
                       }
                       else if(numplayers==3 && points==4){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm()));

                       }
                       else if( numplayers==4 && points==4){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_2.jpg").toExternalForm()));
                       }
                       else if(numplayers==4 && points==2){
                           stack.setImage(new Image(getClass().getResource("/assets/scoring tokens/scoring_back_EMPTY.jpg").toExternalForm()));

                       }
                    });
                    break;
                case UPDATE_BOARD:
                    grid = ((UpdateBoard) message).getBoard().getGrid();
                    Platform.runLater(()->{
                        int k=0;
                        if(gui.getIsmyturn()){
                            donebutton = (Button) gamelayout.getChildren().get(11);
                            donebutton.setVisible(true);
                            whattodo = (Label) gamelayout.getChildren().get(10);
                            whattodo.setVisible(true);
                            move = (TextField) gamelayout.getChildren().get(9);
                            move.setVisible(true);

                        }
                        for(int r = 0 ; r < 9; ++r){
                            for(int c = 0 ; c < 9 ; ++c){
                                ImageView imagvw = (ImageView) boardgrid.getChildren().get(k);
                                switch(grid[r][c].getTile()){
                                    case BLUE :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/Cornici1.1.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case WHITE :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/Libri1.1.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case GREEN :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/Gatti1.1.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case LIGHTBLUE :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/Trofei1.1.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case PURPLE :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/Piante1.1.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case YELLOW :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/Giochi1.1.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case VALID :
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/valid.png").toExternalForm()));
                                        ++k;
                                        break;
                                    case NOTVALID:
                                        imagvw.setImage(new Image(getClass().getResource("/assets/item tiles/notvalid.png").toExternalForm()));
                                        ++k;
                                        break;
                                }
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
            }
            }}while(partitainiziata);
        });
        kernel.start();
    }

    public void login(ActionEvent e) throws IOException {
        username = text.getText();
        if(connection.getSelectedToggle()==socket){
            conn = "socket";
        }
        else {
            conn = "rmi";
        }
        MessageView message = new PayloadUsername(username,conn);
        gui.addMessage(message);
        System.out.println("4");
        //System.out.println("Username "+ username + "\nConnection selected: "+ conn);
        //send these info to the user interface when done.
        //if it is the first user to log in, it shows the number of players scene.
        //num of players scene.
        //if it is not the first user to log in, shows the game board etc..
        Parent waiting = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
        /*Pane gamelayout = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/")));
        boardgrid = (GridPane) gamelayout.getChildren().get(0);
        shelfgrid = (GridPane) gamelayout.getChildren().get(1);
        iw1 = (ImageView) boardgrid.getChildren().get(0);
        iw1.setImage(new Image(getClass().getResource("/assets/item tiles/Cornici1.1.png").toExternalForm()));*/
        Scene waitingScene = new Scene(waiting, 480, 340);
        stage = (Stage)(((Node)e.getSource()).getScene().getWindow());
        stage.setScene(waitingScene);
        stage.show();
    }

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
        /*FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/.fxml")));
        loader.setController(this);
        System.out.println("eccomi");
        Parent gamescene = loader.load();
        Scene scene5 = new Scene(gamescene, 529, 480);
        scene5.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage.setScene(scene5);
        stage.show();*/
        MessageView message = new NumPlayers(nump);
        gui.addMessage(message);

    }
    public void done(ActionEvent e){
        MessageView message = new Done();
        gui.addMessage(message);
    }

    public void boardcellselected(ActionEvent e){
            System.out.println("eccomi nell'event handler");
            donebutton.setVisible(true);
            GridPane boardgrid = (GridPane) gamelayout.getChildren().get(0);
            ImageView node = (ImageView) e.getTarget();
            int i = GridPane.getRowIndex(node);
            int j = GridPane.getColumnIndex(node);
            MessageView message = new SelectedTile(i,j);
            gui.addMessage(message);
    }
    public void shelfcellselected(ActionEvent e){
        GridPane shelfgrid = (GridPane) gamelayout.getChildren().get(1);
        Node node = (Node) e.getTarget();
        int i = boardgrid.getRowIndex(node);
        int j = boardgrid.getColumnIndex(node);
        MessageView message = new ShelfSelected(i,j);
        gui.addMessage(message);
    }
    public void endTurn(ActionEvent e){
        MessageView message = new MessageView(MessageCodeView.END_TURN);
        gui.addMessage(message);
    }
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
    public void showShelf(ActionEvent e){
        MessageView message = new GetCommand("shelf");
        gui.addMessage(message);
    }
}
