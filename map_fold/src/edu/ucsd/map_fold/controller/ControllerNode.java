package edu.ucsd.map_fold.controller;

import edu.ucsd.map_fold.common.ControllerInterface;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;
import java.util.List;
import java.io.File;

import edu.ucsd.map_fold.common.WorkerInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.ucsd.map_fold.common.*;
import edu.ucsd.map_fold.common.logistic_regression.*;


/* ControllerNode -- Main class to launch a controller node */
public class ControllerNode extends UnicastRemoteObject implements ControllerInterface {

    //    TODO:Controller
    public ControllerNode(String jobConf, String workerConf) throws RemoteException, FileNotFoundException, IOException, ParseException{
        JsonParser parser = new JsonParser(jobConf);
        this.workerNum = parser.parseWorkerNum();
        this.dataPath = parser.parseDataPath();
        this.tokenList = parser.parseTokens();

        parser = new JsonParser(workerConf);
        this.workerList = parser.parseWorkerAddr();
        this.controllerPort = parser.parseControllerPort();

        File file = new File(dataPath);
        this.fileSize = file.length();

    }

    public void doneWithWork() throws RemoteException{ }


    public void tokenReceived(int tokenId, int tokenVersion) throws RemoteException{
        //TODO read the data set and divide the works
        int workerSize = workerList.size();
        int each_count = this.safeLongToInt(this.fileSize / workerSize);
        int offset = 0;
        for(WorkerConf worker : workerList){
            try {
                WorkerInterface workerRMI = (WorkerInterface)Naming.lookup(worker.getUrl());
                workerRMI.loadData(this.dataPath, offset, each_count);
                offset += each_count;
            }catch (NotBoundException notBound){
               notBound.printStackTrace();
            }catch (MalformedURLException mu){
                mu.printStackTrace();
            }
        }
        System.out.println("Divided data to all workers");
    }


    public void dataLoaded(String filePath, int offset, int count) throws RemoteException{
        // TODO clients start work
        for(WorkerConf worker : workerList){
            try {
                WorkerInterface workerRMI = (WorkerInterface)Naming.lookup(worker.getUrl());
                //workerRMI.startWork();

            }catch (NotBoundException notBound){
                notBound.printStackTrace();
            }catch (MalformedURLException mu){
                mu.printStackTrace();
            }
        }
    }

    public void init() throws RemoteException, NotBoundException, MalformedURLException{
        //TODO upload token to all clients
        int workerNum = workerList.size();
        int tokenNum = tokenList.size();
        if (workerNum >= tokenNum){
            for(int i = 0; i < tokenNum; i++){
                try{
                    WorkerConf worker = workerList.get(i);
                    WorkerInterface workerRMI = (WorkerInterface)Naming.lookup(worker.getRmiPath());
                    workerRMI.uploadToken(tokenList.get(i));
                }catch (NotBoundException notBound){
                    notBound.printStackTrace();
                }catch (MalformedURLException mu){
                    mu.printStackTrace();
                }
            }
        }else{
            for(int i = 0; i < workerNum; i++){
                try {
                    WorkerConf worker = workerList.get(i);
                    System.out.println(worker.getRmiPath());
                    WorkerInterface workerRMI = (WorkerInterface)Naming.lookup(worker.getRmiPath());
                    workerRMI.uploadToken(tokenList.get(i));

                }catch (NotBoundException notBound){
                    notBound.printStackTrace();
                }catch (MalformedURLException mu){
                    mu.printStackTrace();
                }
            }
        }


    }

    public int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }



    public List<WorkerConf> workerList;
    public List controllerList;
    public List<Token> tokenList;
    public long workerNum;
    public String dataPath;
    public long fileSize;
    public String controllerPort;


}
