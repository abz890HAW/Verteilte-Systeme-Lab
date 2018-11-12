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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;

public class CMessageClient extends Application {
    /* behavior defining variables */
    private final int POLL_INTERVAL_MS = 100;
    private final float POLL_RETRY_DURATION_SEC = 10;
    private int poll_retries = 0;

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

    private String host = "";

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("./GUI.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
        Exception exception = null;
        String error_message = "";
        try {
            messageService.newMessage(clientID, text);
            textFieldInput.setText("");
        }
        catch (ConnectException ex) {
            error_message = "Communication to registry disrupted.";
            exception = ex;
        }
        catch (RemoteException ex) {
            error_message = "Communication to RMI server disrupted.";
            exception = ex;
        }
        if (null != exception) {
            buttonSend.setDisable(true);
            print_error(exception, error_message);
        }
    }

    public void keyPressed(KeyEvent e)
    {
        // send on enter
        if (e.getCharacter().charAt(0) == 0x0d) {
            send(null);
        }
    }

    /* try to establish connection to RMI server and get remote object
     * print to output text field on success
     */
    public void connect(ActionEvent e) {
        host = textFieldHost.getText();
        String error_message = "";
        Exception exception = null;
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            messageService = (IMessageService) registry.lookup(IMessageService.REGISTRY_IDENTIFIER);
            buttonConnect.setDisable(true);
            textFieldHost.setDisable(true);
            buttonSend.setDisable(false);
            timeline = new Timeline(new KeyFrame(Duration.millis(POLL_INTERVAL_MS), ae -> pollMessages()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
            poll_retries = 0;
            listViewOutputWrite("Connection established.");
        }
        catch (RemoteException ex) {
            error_message = "Could not establish communication to RMI server.\nPlease make sure 'rmiregistry' is running on server.";
            exception = ex;
        }
        catch (NotBoundException ex) {
            error_message = "Could not find remote object in Registry.";
            exception = ex;
        }
        if(null != exception) {
            print_error(exception, error_message);
        }
    }

    private void listViewOutputWrite(String string) {
        ObservableList items = listViewOutput.getItems();
        items.add(string);
        listViewOutput.scrollTo(items.size()-1);
    }

    private void pollMessages()
    {
        try {
            String message = messageService.nextMessage(clientID);
            if (null != message) listViewOutputWrite(message);
            poll_retries = 0;
        }
        catch (RemoteException e) {
            reconnect();
        }
    }

    private void reconnect() {
        String error_message = "";
        Exception exception = null;
        /* inform user about first disconnect detected */
        if (0 == poll_retries) {
            listViewOutputWrite("Connection to remote object has been lost. Retrying to connect. Please wait.");
            buttonSend.setDisable(true);
        }
        /* try to reconnect */
        if (poll_retries < (POLL_RETRY_DURATION_SEC/POLL_INTERVAL_MS*1000)) {
            boolean reconnected = false;
            try {
                Registry registry = LocateRegistry.getRegistry(host);
                messageService = (IMessageService) registry.lookup(IMessageService.REGISTRY_IDENTIFIER);
                reconnected = true;
            }
            catch (NotBoundException ex) {
                exception = ex;
            }
            catch (RemoteException ex) {
                error_message = "Could not establish communication to RMI server.\nPlease make sure 'rmiregistry' is running on server.";
                exception = ex;
            }
            if (reconnected) {
                try {
                    String message = messageService.nextMessage(clientID);
                    buttonSend.setDisable(false);
                    listViewOutputWrite("Connection to remote object has been reestablished.");
                    if (null != message) listViewOutputWrite(message);
                }
                catch (RemoteException ex) {
                    error_message = "Remote object is registered, but not running.";
                    exception = ex;
                }
            }

            if (null != exception) {
                if (0 == poll_retries) {
                    print_error(exception, error_message);
                }
            }
            poll_retries++;
        }
        else {                                                  /* reconnection tries exceeded */
            listViewOutputWrite("Reconnecting failed.");
            timeline.stop();                                    /* stop retrying */
            buttonConnect.setDisable(false);                    /* make connection available again*/
            textFieldHost.setDisable(false);

        }
    }

    private void print_error(Exception exception, String error_message) {
        listViewOutputWrite(error_message);
        System.err.println(error_message);
        exception.printStackTrace();
    }
}
