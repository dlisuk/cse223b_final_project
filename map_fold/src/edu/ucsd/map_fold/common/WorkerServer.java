package edu.ucsd.map_fold.common;
import java.rmi.*;
import java.rmi.server.*;
import edu.ucsd.map_fold.worker;
import edu.ucsd.map_fold.worker.WorkerNode;

/**
 * Created by max on 5/20/14.
 */
public class WorkerServer {
    public static void main (String[] argv){
        try{
            WorkerNode worker = new WorkerNode();
            worker.start();
            Naming.rebind ("worker", worker);

        }catch(Exception e){
            System.out.println ("Hello Server failed: " + e);
        }
    }
}
