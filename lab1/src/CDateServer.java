import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

public class CDateServer extends UnicastRemoteObject implements IRemoteDate {
    public CDateServer() throws RemoteException {
        super();
    }
    @Override
    public Date getDate() throws RemoteException {
        Date date = new Date();
        System.out.println("\"getDate\" method was called. Current date is "+ date);
        return date;
    }
    public static void main(String[] args) {
        try {
            IRemoteDate remoteDate = new CDateServer();
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("date", remoteDate);
            System.out.println("Server initialized.");
        }
        catch(RemoteException e) {
            System.err.println("Error during initialization of date server");
            e.printStackTrace();
        }
    }
}
