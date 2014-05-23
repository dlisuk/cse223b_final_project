package edu.ucsd.map_fold.common;

import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

public class Config{
    public Config(String path) throws IOException, ParseException{
        parser = new JSONParser();
        root   = (JSONObject) parser.parse(new FileReader(path));
        JSONArray workerNodes = (JSONArray)root.get("worker");
        JSONArray controllerNodes = (JSONArray)root.get("controller");
        nworkers    = workerNodes.size();
        workers     = new WorkerConfig[nworkers];
        ncontrollers = controllerNodes.size()
        controllers = new ControllerConfig[ncontrollers];
        for(int i = 0; i < nworkers; i++){
            workers[i] = new WorkerConfig((JSONObject)workerNodes.get(i));
        }
        for(int i = 0; i < ncontrollers; i++){
            controllers[i] = new ControllerConfig((JSONObject)controllerNodes.get(i));
        }
    }

    public int              getNworkers(){        return nworkers; }
    public WorkerConfig     getWorker(int i){     return workers[i]; }
    public int              getNcontrollers(){    return ncontrollers; }
    public ControllerConfig getController(int i){ return controllers[i]; }

    public class WorkerConfig{
        public WorkerConfig(JSONObject node){
            addr = (String) node.get("addr");
            nThreads = (Integer) node.get("threads");

        }
        private String  addr;
        private Integer nThreads;
        public String getAddr() { return addr; }
        public Integer getNThreads() { return nThreads; }
    }
    public class ControllerConfig{
        public ControllerConfig(JSONObject node){
            addr = (String) node.get("addr");
        }
        private String addr;
        public String getAddr() { return addr; }
    }

    private JSONParser parser;
    private JSONObject root;

    private WorkerConfig[]     workers;
    private ControllerConfig[] controllers;

    private int nworkers;
    private int ncontrollers;
}
