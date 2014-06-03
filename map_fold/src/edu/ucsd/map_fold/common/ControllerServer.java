package edu.ucsd.map_fold.common;
import java.io.FileReader;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.Set;

import edu.ucsd.map_fold.ServerHelpers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.ucsd.map_fold.controller.ControllerNode;
/**
 * Created by max on 5/20/14.
 */
public class ControllerServer {
    public static void main (String[] argv){
        System.out.println("Starting mapfold controller system");
        try{
            String workerConfPath = "conf/server_conf.json";
            String jobConfPath = "conf/job.json";

            if(argv.length > 0){
                workerConfPath = argv[0];
            }
            System.out.println("Reading config: " + workerConfPath);
            Config config = new Config(workerConfPath);

            if(argv.length > 1){
                jobConfPath = argv[1];
            }
            System.out.println("Reading config: " + jobConfPath);

            Set<String> ips = ServerHelpers.getIPs();
            ips.add("127.0.0.1");
            ips.add("localhost");
            System.out.println("All IP addresses:");
            for(String addr : ips ){
                System.out.println(addr);
            }
            System.out.println();

            for( int i = 0; i < config.getNcontrollers(); i++){
                Config.ControllerConfig controllerConfig = config.getController(i);
                if(ips.contains(controllerConfig.getIpAddr())){
                    System.out.print("Starting controller ");
                    System.out.print(i);
                    System.out.print(" ");
                    System.out.println(controllerConfig.getAddr());
                    ControllerNode controller = new ControllerNode(i,config, jobConfPath);
                    if(controller.isPrimary()){
                        System.out.println("Starting as master");
                        controller.startMaster();
                    }

                    Registry registry = LocateRegistry.createRegistry(Integer.parseInt(controllerConfig.getPort()));
                    registry.bind("Controller", controller);
                    System.out.println("Controller is ready");
                }
            }
            System.out.println("System Ready");
        }catch(Exception e){
            System.out.println ("Worker startup failed: " + e);
            e.printStackTrace();
        }
    }
}
