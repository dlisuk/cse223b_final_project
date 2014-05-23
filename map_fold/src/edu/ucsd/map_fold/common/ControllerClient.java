package edu.ucsd.map_fold.common;
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

    public static WorkerInterface connectToController(String IP) {
        try {
            ControllerInterface controller =
                    (WorkerInterface) Naming.lookup (IP+"/Controller");
            // (HelloInterface) Naming.lookup ("/Hello2");
            return controller;
        } catch (Exception e) {
            System.out.println ("ControllerClient exception: " + e);
        }
    }
}
