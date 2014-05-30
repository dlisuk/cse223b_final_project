package edu.ucsd.map_fold.controller;

import edu.ucsd.map_fold.common.ControllerInterface;

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
    public ControllerNode(String jobConf, String workerConf) throws RemoteException, FileNotFoundException, IOException, ParseException{
        JsonParser parser = new JsonParser(jobConf);
        workerNum = parser.parseWorkerNum();
        dataPath = parser.parseDataPath();
        tokenList = parser.parseTokens();

        parser = new JsonParser(workerConf);
        workerList = parser.parseWorkerAddr();
        controllerPort = parser.parseControllerPort();

        dataMapping = parser.mapDataSegment(dataPath, workerList.size());
        tokenTable = new TokenTable(tokenList.size(),workerList.size());

        this.workerDataMapping = new ArrayList<>();

        for( int i = 0; i < workerList.size(); i++){
            WorkerConf wc = workerList.get(i);
            try{
                System.out.println(wc.getUrl());
                WorkerInterface workerRMI = WorkerClient.connectToWorker(wc.getUrl());
                WorkerDataTuple tuple = new WorkerDataTuple(workerRMI, i, false, false);
                workerDataMapping.add(tuple);

            } catch (MalformedURLException e) {
                throw new RemoteException("Malformed URL: " + wc.getUrl());
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
        TokenTableEntry head = tokenTable.getLatestVersion(tokenId);
        if( head.getTokenVersion() == tokenVersion ){
            head.addHost(workerId);
            if( tokenTable.getNextWorker(tokenId) == workerId){
                tokenTable.startRunning(tokenId);
                tokenTable.setNextWorker(tokenId,-1);
                workerDataMapping.get(workerId).getWorkerInterface().startWork(tokenId,tokenVersion);

            }
        }else{
            //ERror condition
        }
    }


    public void dataLoaded(int workerId, String filePath, int offset, int count) throws RemoteException{
        // TODO clients start work
        for(int i = 0; i < dataMapping.size(); i++){
            if(dataMapping.get(i).start == offset){
                //If the data is loaded, we need to mark the corresponding workerDataMapping entry's indicator to 1
                workerDataMapping.get(workerId).setDataIndex(i);
            }
        }
    }


    public void crash(int index){
        workerDataMapping.get(index).setLiveness(false);
    }

    public void alive(int index){
        workerDataMapping.get(index).setLiveness(true);
    }

    public int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    private class MasterThread implements Runnable {
        public MasterThread(){}
        public void run() {
            Random rand = new Random();
            while (true) {
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
                Iterator<Integer> notLoadedSegmentsIt = notLoadedSegments.iterator();
                for (WorkerDataTuple tuple : workerDataMapping) {
                    if (tuple.getLiveness()) {
                        if (tuple.getDataIndex() < 0 && notLoadedSegmentsIt.hasNext()) {
                            //TODO put data on that worker
                            Integer loadIndex = notLoadedSegmentsIt.next();
                            try {
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
                    if (!tokenTable.isRunning(i) && tokenTable.getNextWorker(i) >= 0)
                        notRunning.addFirst(tokenTable.getLatestVersion(i));
                }

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
                        WorkerInterface worker = workerDataMapping.get(token.getHost()).getWorkerInterface();
                        try {
                            worker.sendToken(targetWorker, token.getTokenId(), token.getTokenVersion());
                            tokenTable.setNextWorker(token.getTokenId(), targetWorker);
                        } catch (RemoteException e) {
                        }
                    }

                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    private class HeartBeatThread implements Runnable{
        public HeartBeatThread(){}
        public void run() {
            while(true){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for( int i = 0; i < workerDataMapping.size(); i++){
                    WorkerInterface workerInterface = workerDataMapping.get(i).getWorkerInterface();
                    System.out.println("Try to ping worker " + i);
                    try{
                        workerInterface.ping(i);
                        if(!workerDataMapping.get(i).getLiveness()){
                            alive(i);
                            System.out.println("Worker alive " + i );
                        }
                    }catch (RemoteException e){
                        System.out.println("Worker not alive");
                        if(workerDataMapping.get(i).getLiveness()){
                            crash(i);
                        }
                    }

                }
            }
        }
    }


    public List<WorkerConf> workerList;
    public List controllerList;
    public List<Token> tokenList;
    public long workerNum;
    public String dataPath;
    public long fileSize;
    public String controllerPort;

    public List<DataSegment> dataMapping;
    public List<WorkerDataTuple> workerDataMapping;
    private TokenTable tokenTable;
}
