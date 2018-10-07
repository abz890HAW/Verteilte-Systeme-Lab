import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;

public class CDateClient {
    private IRemoteDate remoteDate;

    public CDateClient() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        remoteDate = (IRemoteDate) registry.lookup("date");
    }

    public void printServerDate() throws RemoteException {
        Date date = remoteDate.getDate();
        System.out.println(date);
    }

    public static void main(String[] args) {
        try {
            CDateClient dateClient = new CDateClient();
            dateClient.printServerDate();
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
