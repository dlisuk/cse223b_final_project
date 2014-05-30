package edu.ucsd.map_fold.common;
import java.io.FileReader;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.ucsd.map_fold.controller.ControllerNode;
/**
 * Created by max on 5/20/14.
 */
public class ControllerServer {
    public static void main (String[] argv){
        try{
            String worker_conf_path = "map_fold/conf/server_conf.json";
            String job_conf_path = "map_fold/conf/job.json";

            if(argv.length == 2){
                worker_conf_path = argv[0];
                job_conf_path = argv[1];
            }
            //int port = JsonParser.parseControllerPort(worker_conf_path);

            ControllerNode controller = new ControllerNode(job_conf_path, worker_conf_path);

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(controller.controllerPort));
            registry.bind("Controller", controller);
            System.out.println("Controller is ready");

            controller.heartbeatInit();

        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
