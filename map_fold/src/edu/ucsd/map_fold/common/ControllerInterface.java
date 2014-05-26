package edu.ucsd.map_fold.common;

import java.rmi.*;
/**
 * Created by max on 5/19/14.
 */
public interface ControllerInterface extends Remote{
    public Boolean DoneWithWork() throws RemoteException;
    public Boolean tokenReceived(int tokenId, int tokenVersion) throws RemoteException;
    public Boolean dataLoaded(String filePath, int offset, int count) throws RemoteException;
}
