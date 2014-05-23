package edu.ucsd.map_fold.controller;

import edu.ucsd.map_fold.common.ControllerInterface;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.ucsd.map_fold.common.JsonParser;

/* ControllerNode -- Main class to launch a controller node */
public class ControllerNode extends UnicastRemoteObject implements ControllerInterface {

    //    TODO:Controller
    public ControllerNode(String jobConf, String workerConf) throws FileNotFoundException, IOException, ParseException{
        JsonParser parser = new JsonParser(jobConf);
        this.workerNum = parser.parseWorkerNum();
        this.dataPath = parser.parseDataPath();
        this.workerList = parser.parseWorkerAddr();

    }
    public Boolean DoneWithWork() throws RemoteException{
        return true;
    }

    public Boolean tokenReceived(int tokenId, int tokenVersion){
        // TODO load data to clients
        return true;
    }
    public Boolean dataLoaded(String filePath, int offset, int count){
        // TODO clients start work
        
        return true;
    }

    public void run(){
        //TODO read the data set and divide the works

        //TODO upload token to all clients


    }

    public List<String> workerList;
    public List controllerList;
    public int workerNum;
    public String dataPath;


}
