import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CMessageService extends UnicastRemoteObject implements IMessageService {
    // struct to hold data regarding one client
    private class Client
    {
        public Queue<String> messageQueue = new LinkedList<String>();
        public long lastAccess;
    };

    // dictionary mapping clientID to client object
    private static Map<String, Client> clientDictionary = new HashMap<String, Client>();
    private static final int TIMEOUT_MS = 1000;
    private int messageID = 0;

    // constructor
    public CMessageService() throws RemoteException {
        super();
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException {
        // if client ID isn't registeres, create new entry for it
        if(clientDictionary.get(clientID) == null) {
            clientDictionary.put(clientID, new Client());
            System.out.println("Adding message queue for " + clientID);
        }
        Client client = clientDictionary.get(clientID);
        client.lastAccess = System.currentTimeMillis();
        return clientDictionary.get(clientID).messageQueue.poll();
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException {
        // enqueue message to all clients
        String text = String.format("%06d %16s : %-80s %08d", messageID++, clientID, message, System.currentTimeMillis());
        for(String key : clientDictionary.keySet()) {
            clientDictionary.get(key).messageQueue.add(text);
        }
    }

    public static void main(String[] args) {
        try {
            IMessageService messageService = new CMessageService();
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(1099);
                System.err.println("Created registry");
            }
            catch (ExportException ee) {
                if(null == registry) {
                    registry = LocateRegistry.getRegistry();
                    System.err.println("Connected to running registry");
                }
            }
            registry.rebind(IMessageService.REGISTRY_IDENTIFIER, messageService);
            System.out.println("Server initialized.");
            /* timeout loop */
            while(true) {
                TimeUnit.MILLISECONDS.sleep(TIMEOUT_MS);
                Set<String> keys = clientDictionary.keySet();
                for(String key : keys) {
                    if(System.currentTimeMillis()-clientDictionary.get(key).lastAccess > TIMEOUT_MS) {
                        clientDictionary.remove(key);
                        System.out.println("Dropping message queue for " + key);
                    }
                }
            }
        }
        catch(RemoteException e) {
            System.err.println("Error during initialization of date server");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
