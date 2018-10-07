import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageService extends Remote {
    public static final String REGISTRY_IDENTIFIER = "MessageService";
    public String nextMessage (String clientID) throws RemoteException;
    public void newMessage (String clientID, String message) throws RemoteException;
}
