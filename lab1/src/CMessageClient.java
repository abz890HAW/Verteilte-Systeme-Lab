import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;

public class CMessageClient extends Application {

    /* fxml related variables */
    private Timeline timeline;
    @FXML
    private TextField textFieldInput;
    @FXML
    private TextField textFieldHost;
    @FXML
    private Button buttonSend;
    @FXML
    private Button buttonConnect;
    @FXML
    private ListView listViewOutput;

    /* RMI relevant variables */
    private IMessageService messageService;
    private static String clientID;
    Timer timer = new Timer();

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop(){
        System.out.println("Stage is closing");
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            clientID = Inet4Address.getLocalHost().getHostAddress();
            launch(args);
        } catch (UnknownHostException e) {
            System.err.println("Could not get IP address of localhost.");
            e.printStackTrace();
        }
    }

    public void send(ActionEvent e) {
        String text = textFieldInput.getText();
        try {
            messageService.newMessage(clientID, text);
        }
        catch (RemoteException ex) {
            System.err.println("Communication to RMI server disrupted.");
            ex.printStackTrace();
        }
        textFieldInput.setText("");
    }

    /* try to establish connection to RMI server and get remote object
     * print to output text field on success
     */
    public void connect(ActionEvent e) {
        String host = textFieldHost.getText();
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            messageService = (IMessageService) registry.lookup(IMessageService.REGISTRY_IDENTIFIER);
            buttonConnect.setDisable(true);
            textFieldHost.setDisable(true);
            buttonSend.setDisable(false);
            timeline = new Timeline(new KeyFrame(Duration.millis(100), ae -> pollMessages()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
        catch (RemoteException ex) {
            System.err.println("Could not establish communication to RMI server.");
            ex.printStackTrace();
        }
        catch (NotBoundException ex) {
            System.err.println("Could not find remote object in Registry.");
            ex.printStackTrace();
        }
        listViewOutputWrite("Connection established.");
    }

    private void listViewOutputWrite(String string) {
        ObservableList items = listViewOutput.getItems();
        items.add(string);
        listViewOutput.scrollTo(items.size()-1);
    }

    public void pollMessages()
    {
        try {
            String message = messageService.nextMessage(clientID);
            if(null != message) {
                listViewOutputWrite(message);
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
