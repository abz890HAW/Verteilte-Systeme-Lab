import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class CMessageService extends UnicastRemoteObject implements IMessageService {

    public CMessageService() throws RemoteException {
        super();
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException {
        return null;
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException {

    }

    public static void main(String[] args) {
        try {
            IMessageService messageService = new CMessageService();
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(IMessageService.REGISTRY_IDENTIFIER, messageService);
            System.out.println("Server initialized.");
        }
        catch(RemoteException e) {
            System.err.println("Error during initialization of date server");
            e.printStackTrace();
        }
    }
}
