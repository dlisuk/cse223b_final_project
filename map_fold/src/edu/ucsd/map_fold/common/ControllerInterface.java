package edu.ucsd.map_fold.common;

import edu.ucsd.map_fold.common.logistic_regression.Token;
import edu.ucsd.map_fold.controller.TokenTable;

import java.rmi.*;
import java.util.List;

/**
 * Created by max on 5/19/14.
 */
public interface ControllerInterface extends Remote{
    public void uploadToken(int workerId, Token token) throws RemoteException;
    public void doneWithWork(int workerId, int tokenId, int tokenVersion) throws RemoteException;
    public void tokenReceived(int workerId, int tokenId, int tokenVersion) throws RemoteException;
    public void dataLoaded(int workerId, String filePath, Long offset, Long count) throws RemoteException;
    public void syncBetweenController(List<WorkerDataTuple> workerTupleData, TokenTable tokenTable) throws RemoteException;
    public void ping() throws RemoteException;
}
