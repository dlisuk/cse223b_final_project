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
            String workerConfPath = "map_fold/conf/server_conf.json";
            String jobConfPath = "map_fold/conf/job.json";
            String primaryConf = "no";
            boolean isPrimary = false;

            if(argv.length == 1){
                primaryConf = "true";
            }

            if (primaryConf == "true"){
                isPrimary = true;
            }else{
                isPrimary = false;
            }
            ControllerNode controller = new ControllerNode(jobConfPath, workerConfPath, isPrimary);

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(controller.controllerPort));
            registry.bind("Controller", controller);
            System.out.println("Controller is ready");

            if(controller.isPrimary()){
              controller.heartbeatInit();
            }


        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
