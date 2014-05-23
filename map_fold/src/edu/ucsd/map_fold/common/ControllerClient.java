package edu.ucsd.map_fold.common;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
/**
 * Created by max on 5/20/14.
 */
public class ControllerClient {
//    public static void main (String[] argv){
//        try{
//
//        }catch(Exception e){
//
//        }
//    }

    public static ControllerInterface connectToController(String IP) throws RemoteException, NotBoundException, MalformedURLException {
        return (ControllerInterface) Naming.lookup(IP+"/Controller");
    }
}
