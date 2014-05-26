package edu.ucsd.map_fold.common;
import java.rmi.*;
import edu.ucsd.map_fold.worker.WorkerNode;

/**
 * Created by max on 5/20/14.
 */
public class WorkerServer {
    public static void main (String[] argv){
        try{
            String worker_conf_path = "conf/server_conf.json";

            Config config = new Config(worker_conf_path);

            WorkerNode worker = new WorkerNode(0, config);
            worker.start();
            Naming.rebind ("worker", worker);

        }catch(Exception e){
            System.out.println ("Hello Server failed: " + e);
            e.printStackTrace();
        }
    }
}
