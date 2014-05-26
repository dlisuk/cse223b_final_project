package edu.ucsd.map_fold.common;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
/**
 * Created by max on 5/20/14.
 */
public class WorkerClient {
//    public static void main (String[] argv){
//        try{
//
//        }catch(Exception e){
//
//        }
//    }
    public static WorkerInterface connectToWorker(String IP) throws RemoteException, NotBoundException, MalformedURLException {
        return (WorkerInterface) Naming.lookup ("//" + IP+"/Worker");
    }
}
