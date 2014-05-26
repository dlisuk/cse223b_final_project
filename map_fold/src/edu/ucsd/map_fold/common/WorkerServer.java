package edu.ucsd.map_fold.common;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import edu.ucsd.map_fold.worker.WorkerNode;

/**
 * Created by max on 5/20/14.
 */
public class WorkerServer {
    public static void main (String[] argv){
        try{
            String worker_conf_path = "map_fold/conf/server_conf.json";

            Config config = new Config(worker_conf_path);

            WorkerNode worker = new WorkerNode(0, config);
            worker.start();

            Registry registry = LocateRegistry.createRegistry(8888);
            registry.bind("worker", worker);
            System.out.println("Worker is ready");

        }catch(Exception e){
            System.out.println ("Hello Server failed: " + e);
            e.printStackTrace();
        }
    }
}
