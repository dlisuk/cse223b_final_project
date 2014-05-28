package edu.ucsd.map_fold.common;
import edu.ucsd.map_fold.common.logistic_regression.Token;

import java.rmi.*;
/**
 * Created by max on 5/20/14.
 */
public interface WorkerInterface extends Remote{
    public void uploadToken(Token token) throws RemoteException;
    public void startWork(int tokenId, int version) throws RemoteException;
    public void sendToken(int target, int tokenId, int version) throws RemoteException;
    public void loadData(String filePath, int offset, int count) throws RemoteException;
    public void ping(int workerId) throws RemoteException;
}
