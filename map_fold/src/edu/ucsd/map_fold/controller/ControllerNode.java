package edu.ucsd.map_fold.controller;

import edu.ucsd.map_fold.common.ControllerInterface;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;

import edu.ucsd.map_fold.common.WorkerInterface;
import org.json.simple.parser.ParseException;

import edu.ucsd.map_fold.common.*;
import edu.ucsd.map_fold.common.logistic_regression.*;


/* ControllerNode -- Main class to launch a controller node */
public class ControllerNode extends UnicastRemoteObject implements ControllerInterface {
    //    TODO:Controller
    public ControllerNode(int _ControllerId, Config config, String jobConf) throws IOException, ParseException {
        controllerId = _ControllerId;
        primary = config.getController(_ControllerId).isPrimary();

        JsonParser parser = new JsonParser(jobConf);
        workerNum = safeLongToInt(parser.parseWorkerNum());
        dataPath = parser.parseDataPath();
        tokenList = parser.parseTokens();
        log(tokenList.toString());

        controllerList = new ArrayList<>();
        for( int i = 0; i < config.getNcontrollers(); i++){
            Config.ControllerConfig cc = config.getController(i);
            ControllerConf conf = new ControllerConf(cc.getIpAddr(),cc.getPort(),cc.isPrimary());
            controllerList.add(conf);
        }
        workerList = new ArrayList<>();
        workerDataMapping = new ArrayList<>();
        for( int i = 0; i < config.getNworkers(); i++){
            Config.WorkerConfig cc = config.getWorker(i);
            WorkerConf conf = new WorkerConf(cc.getIpAddr(),cc.getPort(),cc.getNThreads());
            workerList.add(conf);
        }


        dataMapping = new ArrayList<>();
        File file = new File(dataPath);
        fileSize = safeLongToInt(file.length());
        int start = 0;
        int length = fileSize / workerNum;

        for (int i = 0; i < workerNum; i++) {
            if(i==(workerNum-1)){
                length=fileSize-start;
            }
            DataSegment ds = new DataSegment(start, length);
            dataMapping.add(ds);
            start += length;
        }
        tokenTable = new TokenTable(tokenList.size(),workerList.size());

        this.controllerDataMapping = new ArrayList<>();

        for ( int i = 0; i < controllerList.size(); i++){
            ControllerConf cc = controllerList.get(i);
            try{
                ControllerInterface controllerRMI = ControllerClient.connectToController(cc.getUrl());
                ControllerDataTuple tuple = new ControllerDataTuple(controllerRMI, cc.isPrimary(), false);
                controllerDataMapping.add(tuple);
            } catch (MalformedURLException e){
                throw new RemoteException("Malformed controller URL: " + cc.getUrl());
            }
        }

        for( int i = 0; i < workerList.size(); i++){
            WorkerConf wc = workerList.get(i);
            try{
                WorkerInterface workerRMI = WorkerClient.connectToWorker(wc.getUrl());
                WorkerDataTuple tuple = new WorkerDataTuple(workerRMI, i, false, false);
                workerDataMapping.add(tuple);

            } catch (MalformedURLException e) {
                throw new RemoteException("Malformed worker URL: " + wc.getUrl());
            }
        }
    }

    public void startMaster() throws RemoteException{
        // Start running
        HeartBeatThread hb = new HeartBeatThread();
        new Thread(hb).start();
        MasterThread mt = new MasterThread();
        new Thread(mt).start();
    }

    public void doneWithWork(int workerId, int tokenId, int tokenVersion) throws RemoteException{
        log("doneWithWork("+workerId+", " + tokenId+ ", " + tokenVersion + ")");
        TokenTableEntry head = tokenTable.getLatestVersion(tokenId);
        if( head.getTokenVersion() == tokenVersion ){
            tokenTable.stopRunning(tokenId);
            int finishedSegment = workerDataMapping.get(workerId).getDataIndex();
            tokenTable.newVersion(tokenId, finishedSegment, workerId);
        }else{
            //ERror condition
        }
    }


    public void tokenReceived(int workerId, int tokenId, int tokenVersion) throws RemoteException{
        log("tokenReceived("+workerId+", " + tokenId+ ", " + tokenVersion + ")");
        TokenTableEntry head = tokenTable.getLatestVersion(tokenId);
        if( head.getTokenVersion() == tokenVersion ){
            head.addHost(workerId);
            if( tokenTable.getNextWorker(tokenId) == workerId){
                tokenTable.startRunning(tokenId);
                tokenTable.setNextWorker(tokenId,-1);
                workerDataMapping.get(workerId).getWorkerInterface().startWork(tokenId,tokenVersion);

            }else{
                log("ERROR B: " + tokenTable.getNextWorker(tokenId) + " " + workerId);
            }
        }else{
            log("ERROR A: " + head.getTokenVersion() + " " + tokenVersion );
            //ERror condition
        }
    }


    public void dataLoaded(int workerId, String filePath, int offset, int count) throws RemoteException{
        log("dataLoaded("+workerId+", " + filePath+ ", " + offset + ", " + count + ")");
        // TODO clients start work
        for(int i = 0; i < dataMapping.size(); i++){
            if(dataMapping.get(i).start == offset){
                //If the data is loaded, we need to mark the corresponding workerDataMapping entry's indicator to 1
                workerDataMapping.get(workerId).setDataIndex(i);
            }
        }
    }


    public void ping() throws RemoteException {
        System.out.println("Controller node ping output");
    }


    public void syncBetweenController(List<WorkerDataTuple> workerDataMapping, TokenTable tokenTable){
        this.workerDataMapping = workerDataMapping;
        this.tokenTable = tokenTable;
    }

    public void heartbeatInit() throws RemoteException{
        HeartBeatThread hb = new HeartBeatThread();
        new Thread(hb).start();
    }

    private class controllerSyncThread implements  Runnable{
        public controllerSyncThread(){}
        public void run() {
            while(true){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for( int i = 0; i < controllerDataMapping.size(); i++){
                    ControllerInterface controllerInterface = controllerDataMapping.get(i).getControllerInterface();
                    System.out.println("Try to ping Controller " + i);
                    try{
                        controllerInterface.ping();
                        if(!controllerDataMapping.get(i).getLiveness()){
                            controllerAlive(i);
                            System.out.println("Controller alive " + i );
                        }
                    }catch (RemoteException e){
                        System.out.println("Controller not alive");
                        if(controllerDataMapping.get(i).getLiveness()){
                            controllerCrash(i);
                        }
                    }

                }
            }
            //controllerList
        }
    }


    public void crash(int index){
        workerDataMapping.get(index).setLiveness(false);
    }

    public void alive(int index){
        workerDataMapping.get(index).setLiveness(true);
    }

    public void controllerCrash(int index){ controllerDataMapping.get(index).setLiveness(false);}

    public void controllerAlive(int index){ controllerDataMapping.get(index).setLiveness(true);}

    public int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }


    private class MasterThread implements Runnable {
        public MasterThread(){
            log("Starting master");
        }
        public void run() {
            log("Running master");
            Random rand = new Random();
            while (true) {
                try {
                    //TODO: Figure out what data to put on workers that currently have no data
                    Set<Integer> notLoadedSegments = new HashSet<>();
                    for (Integer i = 0; i < dataMapping.size(); i++) {
                        notLoadedSegments.add(i);
                    }
                    for (WorkerDataTuple tuple : workerDataMapping) {
                        if (tuple.getLiveness()) {
                            notLoadedSegments.remove(tuple.dataIndex);
                        }
                    }
                    if(notLoadedSegments.size()>0){log("Not loaded " + notLoadedSegments);}
                    Iterator<Integer> notLoadedSegmentsIt = notLoadedSegments.iterator();
                    for (WorkerDataTuple tuple : workerDataMapping) {
                        if (tuple.getLiveness()) {
                            if (tuple.getDataIndex() < 0 && notLoadedSegmentsIt.hasNext()) {
                                //TODO put data on that worker
                                Integer loadIndex = notLoadedSegmentsIt.next();
                                try {
                                    log("Loading " + loadIndex + " on " + tuple.index);
                                    DataSegment ds = dataMapping.get(loadIndex);
                                    tuple.workerInterface.loadData(dataPath, ds.start, ds.length);
                                    tuple.setDataIndex(loadIndex);
                                } catch (Exception e) {
                                    System.out.println("loadData error " + e);
                                }
                            }
                        }
                    }
                    //TODO: Get tokens that are not running
                    LinkedList<TokenTableEntry> notRunning = new LinkedList<>();
                    for (int i = 0; i < tokenTable.size(); i++) {
                        if (!tokenTable.isRunning(i) && tokenTable.getNextWorker(i) < 0)
                            notRunning.addFirst(tokenTable.getLatestVersion(i));
                    }
                    if(notRunning.size()>0){log("Not running " + notRunning);}

                    //TODO: Figure out which token goes to each worker
                    for (TokenTableEntry token : notRunning) {
                        Set<Integer> notSeen = token.getNotSeen();
                        double possibleTargets = 1.0;
                        int targetWorker = -1;

                        for (Integer i = 0; i < workerDataMapping.size(); i++) {
                            WorkerDataTuple possibleTargetTuple = workerDataMapping.get(i);
                            if (possibleTargetTuple.getLiveness() && notSeen.contains(possibleTargetTuple.getDataIndex())) {
                                //This assigns equal probability to every worker that might be good to send to
                                if (rand.nextDouble() <= 1.0 / possibleTargets)
                                    targetWorker = i;
                                possibleTargets += 1;
                            }
                        }
                        if (targetWorker != -1) {
                            Integer sourceWorker = token.getHost();
                            if(sourceWorker >= 0) {
                                WorkerInterface worker = workerDataMapping.get(token.getHost()).getWorkerInterface();
                                try {
                                    tokenTable.setNextWorker(token.getTokenId(), targetWorker);
                                    log("Next worker: " + tokenTable.getNextWorker(token.getTokenId()));
                                    worker.sendToken(targetWorker, token.getTokenId(), token.getTokenVersion());
                                } catch (RemoteException e) {
                                    log("Send Token Exception: " + e.getMessage());
                                    tokenTable.setNextWorker(token.getTokenId(), -1);
                                }
                            }else{
                                log("Uploading token " + token.getTokenId() + " to " + targetWorker);
                                try {
                                    tokenTable.setNextWorker(token.getTokenId(), targetWorker);
                                    log("Next worker: " + tokenTable.getNextWorker(token.getTokenId()));
                                    workerDataMapping.get(targetWorker).getWorkerInterface().uploadToken(tokenList.get(token.getTokenId()));
                                } catch (RemoteException e) {
                                    log("Upload Token Exception: " + e.getMessage());
                                    tokenTable.setNextWorker(token.getTokenId(), -1);
                                }
                            }
                        }

                    }

                }catch(Exception e){
                    log("EXCEPTION: " + e.getMessage());
                    e.printStackTrace();
                }finally{
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {  }

                }
            }
        }
    }

    private class HeartBeatThread implements Runnable {
        public HeartBeatThread() {
            log("Starting heartbeat");
        }

        public void run() {
            log("Running heartbeat");
            while (true) {
                for (int i = 0; i < workerDataMapping.size(); i++) {
                    WorkerInterface workerInterface = workerDataMapping.get(i).getWorkerInterface();
                    try {
                        workerInterface.ping(i);
                        if (!workerDataMapping.get(i).getLiveness()) {
                            alive(i);
                            log("Worker alive " + i);
                        }
                    } catch (RemoteException e) {
                        if (workerDataMapping.get(i).getLiveness()) {
                            crash(i);
                            log("Worker dead " + i);
                        }
                    }

                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    private Integer controllerId;
    public List<WorkerConf> workerList;
    public List<ControllerConf> controllerList;
    public List<Token> tokenList;
    public Integer workerNum;
    public String dataPath;
    public Integer fileSize;
    public boolean primary;
    public String controllerPort;

    public List<DataSegment> dataMapping;
    public List<WorkerDataTuple> workerDataMapping;
    public List<ControllerDataTuple> controllerDataMapping;
    private TokenTable tokenTable;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private void log(String msg){
        Date date = new Date();

        String head = "Controller " + Integer.toString(controllerId) + " " + dateFormat.format(date);
        System.out.println(head + "::" + msg);
    }
}
