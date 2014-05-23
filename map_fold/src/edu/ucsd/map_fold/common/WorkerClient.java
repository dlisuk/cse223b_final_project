package edu.ucsd.map_fold.common;
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
    public static WorkerInterface connectToWorker(String IP) {
        try {
            WorkerInterface worker =
                    (WorkerInterface) Naming.lookup (IP+"/Worker");
            // (HelloInterface) Naming.lookup ("/Hello2");
            return worker;
        } catch (Exception e) {
            System.out.println ("WorkerClient exception: " + e);
        }
    }
}
