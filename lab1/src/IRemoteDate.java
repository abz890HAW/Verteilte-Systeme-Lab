import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

public interface IRemoteDate extends Remote {
    public Date getDate() throws RemoteException;
}
