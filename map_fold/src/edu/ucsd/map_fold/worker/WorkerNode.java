package edu.ucsd.map_fold.worker;

import edu.ucsd.map_fold.common.WorkerInterface;

import java.rmi.*;
import java.rmi.server.*;

/* WorkerNode -- Main class to launch a worker node */
public class WorkerNode extends UnicastRemoteObject implements WorkerInterface{
//    public static void main(String[] args) {
//
//    }
    public WorkerNode() throws RemoteException{

    }

    public void uploadToken() throws RemoteException{

    }
    public void startWork(int tokenId) throws RemoteException{

    }
    public void sendToken(int target) throws RemoteException{

    }
    public void loadData(String filePath, int offset, int count) throws RemoteException{

    }


    

}
