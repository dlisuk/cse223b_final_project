package edu.ucsd.map_fold.controller;

import edu.ucsd.map_fold.common.ControllerInterface;

import java.rmi.*;
import java.rmi.server.*;


/* ControllerNode -- Main class to launch a controller node */
public class ControllerNode extends UnicastRemoteObject implements ControllerInterface {
//    public static void main(String[] args) {
//        System.out.println("Hello Max");
//
//    }
//    TODO:Controller
    public ControllerNode() throws RemoteException{

    }
    public Boolean DoneWithWork() throws RemoteException{
        return true;
    }
    public Boolean tokenReceived(int tokenId, int tokenVersion){
        return true;
    }
    public Boolean dataLoaded(String filePath, int offset, int count){
        return true;
    }
}
