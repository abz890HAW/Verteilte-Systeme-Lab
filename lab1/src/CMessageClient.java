import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CMessageClient {
    private IMessageService messageService;

    public CMessageClient() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        messageService = (IMessageService) registry.lookup(IMessageService.REGISTRY_IDENTIFIER);
    }


    public static void main(String[] args) {
        try {
            CMessageClient messageClient = new CMessageClient();
        }
        catch (NotBoundException e) {
            System.err.println("Could not find remote object in Registry.");
            e.printStackTrace();
        }
        catch (RemoteException e) {
            System.err.println("Could not establish communication to RMI server.");
            e.printStackTrace();
        }
    }
}
