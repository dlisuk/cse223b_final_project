package edu.ucsd.map_fold.common;
import java.rmi.*;
/**
 * Created by max on 5/20/14.
 */
public interface WorkerInterface extends Remote{
    public void uploadToken() throws RemoteException;
    public void startWork(int tokenId) throws RemoteException;
    public void sendToken(int target) throws RemoteException;
    public void loadData(String filePath, int offset, int count) throws RemoteException;
}
