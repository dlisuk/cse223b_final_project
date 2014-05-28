package edu.ucsd.map_fold.common;

import java.rmi.*;
/**
 * Created by max on 5/19/14.
 */
public interface ControllerInterface extends Remote{
    public void doneWithWork(int workerId, int tokenId, int tokenVersion) throws RemoteException;
    public void tokenReceived(int workerId, int tokenId, int tokenVersion) throws RemoteException;
    public void dataLoaded(int workerId, String filePath, int offset, int count) throws RemoteException;
}
