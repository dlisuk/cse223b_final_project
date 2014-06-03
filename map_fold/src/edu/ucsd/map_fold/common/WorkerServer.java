package edu.ucsd.map_fold.common;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;

import edu.ucsd.map_fold.ServerHelpers;
import edu.ucsd.map_fold.worker.WorkerNode;

/**
 * Created by max on 5/20/14.
 */
public class WorkerServer {
    public static void main (String[] argv){
        System.out.println("Starting mapfold worker system");
        try{
            String worker_conf_path = "conf/server_conf.json";
            if(argv.length > 0){
                worker_conf_path = argv[0];
            }
            System.out.println("Reading config: " + worker_conf_path);

            Set<String> ips = ServerHelpers.getIPs();
            ips.add("127.0.0.1");
            ips.add("localhost");
            System.out.println("All IP addresses:");
            for(String addr : ips ){
                System.out.println(addr);
            }
            System.out.println();

            Config config = new Config(worker_conf_path);
            for( int i = 0; i < config.getNworkers(); i++){
                Config.WorkerConfig workerConf = config.getWorker(i);
                if(ips.contains(workerConf.getIpAddr())){
                    System.out.print("Starting worker ");
                    System.out.print(i);
                    System.out.print(" ");
                    System.out.println(workerConf.getAddr());
                    WorkerNode worker = new WorkerNode(i, config);
                    worker.start();

                    Registry registry = LocateRegistry.createRegistry(Integer.parseInt(workerConf.getPort()));
                    registry.bind("Worker", worker);
                    System.out.println("Worker is ready");
                }
            }
            System.out.println("System Ready");
        }catch(Exception e){
            System.out.println ("Worker startup failed: " + e);
            e.printStackTrace();
        }
    }
}
