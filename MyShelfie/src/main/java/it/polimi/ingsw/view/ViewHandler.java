package it.polimi.ingsw.view;

import com.sun.javafx.scene.control.skin.FXVK;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
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
import javafx.stage.Popup;
import javafx.stage.Stage;

import javax.management.Notification;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


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
    @FXML
    TextField text;
    @FXML
    RadioButton socket, rmi, two, three, four;
    @FXML
    Button button;
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
    Label l1;
   /*public ViewHandler(GUIClient client){
       //this.lock = lock;
       //this.answer = answer;
       this.client = client;
    }*/

    public ViewHandler(Stage stage, View view){
        this.gui = new GUIInterface();
        this.glogic = new GraphicLogic(gui);
        this.stage = stage;
        this.view = view;
        Thread t = new Thread(()->{glogic.init();});
        t.start();
        Thread kernel = new Thread(()->{
            MessageView message;
            sendedfromgui = gui.getSendedQueue();
            do{
                message = sendedfromgui.poll();
            }while(message==null);

            switch (message.getType()){
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
                        Scene scene3 = new Scene(errorscene, 480, 340);
                        scene3.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
                        stage.setScene(scene3);
                        stage.show();});
                    break;
                case GENERIC_MESSAGE:
                    String msg = ((NotificationMessage) message).getText();
                    Platform.runLater(()->{
                        Popup popup = new Popup();
                        popup.setAutoHide(true);
                        Label label = new Label(msg);
                        popup.getContent().add(label);
                        while(!popup.isShowing()){
                            popup.show(stage);
                        }
                        popup.hide();
                    });
            }
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
        /*Pane gamelayout = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/game.fxml")));
        boardgrid = (GridPane) gamelayout.getChildren().get(0);
        shelfgrid = (GridPane) gamelayout.getChildren().get(1);
        iw1 = (ImageView) boardgrid.getChildren().get(0);
        iw1.setImage(new Image(getClass().getResource("/assets/item tiles/Cornici1.1.png").toExternalForm()));*/
        Scene waitingScene = new Scene(waiting, 480, 340);
        stage = (Stage)(((Node)e.getSource()).getScene().getWindow());
        stage.setScene(waitingScene);
        stage.show();
    }

    public void numberOfPlayersController(ActionEvent e){

            if(numplayers.getSelectedToggle()==two){
                nump=2;
            }
            else if(numplayers.getSelectedToggle()==three){
                nump=3;
            }
            else if(numplayers.getSelectedToggle()==four){
                nump=4;
            }
            MessageView message = new NumPlayers(nump);
            gui.addMessage(message);
    }
    public void boardcellselected(ActionEvent e){

    }
    public void shelfcellselected(ActionEvent e){

    }

    public String getUsername() {
       return username;
    }
    public int getNump(){
        return nump;
    }
    public Boolean getAnswer(){
        return answer;
    }

}
