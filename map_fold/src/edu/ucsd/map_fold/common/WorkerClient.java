package edu.ucsd.map_fold.common;
import edu.ucsd.map_fold.common.logistic_regression.Token;

import java.net.MalformedURLException;
import java.rmi.*;

public class WorkerClient extends GenericClient<WorkerInterface> implements WorkerInterface{
    public static WorkerInterface connectToWorker(String IP) throws MalformedURLException {
        return new WorkerClient(IP);
    }

    public void uploadToken(Token token) throws RemoteException {
        call( w -> {try{w.uploadToken(token); return null;}catch (RemoteException e){ return e; }} );
    }
    public void startWork(int tokenId, int version) throws RemoteException {
        call( w -> {try{w.startWork(tokenId, version); return null;}catch (RemoteException e){ return e; }} );
    }
    public void sendToken(int target, int tokenId, int version) throws RemoteException {
        call( w -> {try{w.sendToken(target, tokenId, version); return null;}catch (RemoteException e){ return e; }} );
    }
    public void loadData(String filePath, Long offset, Long count) throws RemoteException {
        call( w -> {try{w.loadData(filePath, offset, count); return null;}catch (RemoteException e){ return e; }} );
    }

    public void ping(int workerId) throws RemoteException {
        call( w -> {try{w.ping(workerId); return null;}catch (RemoteException e){ return e; }} );
    }

    private WorkerClient (String _addr) throws MalformedURLException {
        addr = "//" + _addr + "/Worker";
        checkAddress();
    }
}
