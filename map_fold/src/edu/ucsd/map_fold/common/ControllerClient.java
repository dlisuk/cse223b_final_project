package edu.ucsd.map_fold.common;
import edu.ucsd.map_fold.controller.TokenTable;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.List;

/**
 * Created by max on 5/20/14.
 */
public class ControllerClient extends GenericClient<ControllerInterface> implements ControllerInterface{

    public static ControllerInterface connectToController(String IP) throws MalformedURLException {
        return new ControllerClient(IP);
    }

    public void doneWithWork(int workerId, int tokenId, int tokenVersion) throws RemoteException {
        call( w -> {try{w.doneWithWork(workerId, tokenId, tokenVersion); return null;}catch (RemoteException e){ return e; }} );
    }
    public void tokenReceived(int workerId, int tokenId, int tokenVersion) throws RemoteException {
        call( w -> {try{w.tokenReceived(workerId, tokenId, tokenVersion); return null;}catch (RemoteException e){ return e; }} );
    }
    public void dataLoaded(int workerId, String filePath, int offset, int count) throws RemoteException {
        call( w -> {try{w.dataLoaded(workerId, filePath,offset, count); return null;}catch (RemoteException e){ return e; }} );
    }

    public void syncBetweenController(List<WorkerDataTuple> workerTupleData, TokenTable tokenTable) throws RemoteException{
        call( w -> {try{w.syncBetweenController(workerTupleData, tokenTable); return null;}catch (RemoteException e){ return e;}} );
    }

    public void ping() throws RemoteException {
        call( w -> {try{w.ping(); return null;}catch (RemoteException e){ return e; }} );
    }

    private ControllerClient (String _addr) throws MalformedURLException {
        addr = "//" + _addr + "/Controller";
        checkAddress();
    }
}
