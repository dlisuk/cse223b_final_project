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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        //log(tokenList.toString());

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
        fileSize = file.length();
        Long start = 0l;
        Long length = fileSize / workerNum;

        for (int i = 0; i < workerNum; i++) {
            if(i==(workerNum-1)){
                length=fileSize-start;
            }
            DataSegment ds = new DataSegment(start, length);
            dataMapping.add(ds);
            start += length;
        }
        tokenTable = new TokenTable(tokenList.size(),workerNum);

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

    public void uploadToken(int workerId, Token token) throws RemoteException {
        lock.lock();
        try {
            Token head = tokenList.get(token.getId());
            if (head.getVersion() < token.getVersion()) {
                tokenList.set(token.getId(), token);
            }
        }finally {
            lock.unlock();
        }
    }

    public void doneWithWork(int workerId, int tokenId, int tokenVersion) throws RemoteException{
        log("doneWithWork("+workerId+", " + tokenId+ ", " + tokenVersion + ")");
    }


    public void tokenReceived(int workerId, int tokenId, int tokenVersion) throws RemoteException{
        log("tokenReceived("+workerId+", " + tokenId+ ", " + tokenVersion + ")");
        lock.lock();
        try {
            TokenTableEntry head = tokenTable.getLatestVersion(tokenId);
            if (head.getTokenVersion() == tokenVersion) {
                head.addHost(workerId);
                if (tokenTable.getNextWorker(tokenId) == workerId) {
                    tokenTable.startRunning(tokenId);
                    tokenTable.setNextWorker(tokenId, -1);
                    workerDataMapping.get(workerId).getWorkerInterface().startWork(tokenId, tokenVersion);
                    syncController(2);
                } else {
                    log("ERROR B: " + tokenTable.getNextWorker(tokenId) + " " + workerId);
                }
            } else if (tokenVersion == head.getTokenVersion() + 1) {
                tokenTable.stopRunning(tokenId);
                int finishedSegment = workerDataMapping.get(workerId).getDataIndex();
                tokenTable.newVersion(tokenId, finishedSegment, workerId);
                head = tokenTable.getLatestVersion(tokenId);
               // log(head.toString());
            } else {
                log("ERROR A: " + head.getTokenVersion() + " " + tokenVersion);
                //ERror condition
            }
        }finally {
            lock.unlock();
        }
    }


    public void dataLoaded(int workerId, String filePath, Long offset, Long count) throws RemoteException{
        log("dataLoaded("+workerId+", " + filePath+ ", " + offset + ", " + count + ")");
        // TODO clients start work
        int ind = workerDataMapping.get(workerId).getDataIndex();
        if(dataMapping.get(ind).start.equals(offset)){
            //If the data is loaded, we need to mark the corresponding workerDataMapping entry's indicator to 1
            lock.lock();
            workerDataMapping.get(workerId).setDataLoaded(true);
            lock.unlock();
        }else{
            lock.lock();
            log("BAD LOAD: " + offset + " " + dataMapping.get(ind).start);
            workerDataMapping.get(workerId).setDataLoaded(false);
            workerDataMapping.get(workerId).setDataIndex(-1);
            lock.unlock();
        }
        syncController(1);
    }


    public void ping() throws RemoteException {
        log("Controller node ping output");
    }

    public void syncController(int stage) throws RemoteException{

        switch (stage){
            case 1:
                for( int i = 0; i < controllerDataMapping.size(); i++){
                    ControllerDataTuple controller = controllerDataMapping.get(i);
                    if(!controller.isPrimary()){
                        ControllerInterface controllerInterface = controller.getControllerInterface();
                        System.out.println("Try to sync worker data tuple with Controller " + i);
                        try{
                            controllerInterface.syncWorkerDataLoading(workerDataMapping);

                        }catch (RemoteException e){
                            System.out.println("Sync worker data with controller failed");
                        }
                    }
                }
                break;

            case 2:
                for( int i = 0; i < controllerDataMapping.size(); i++){
                    ControllerDataTuple controller = controllerDataMapping.get(i);
                    if(!controller.isPrimary()){
                        ControllerInterface controllerInterface = controller.getControllerInterface();
                        System.out.println("Try to sync Token data with Controller " + i);
                        try{
                            controllerInterface.syncTokenData(tokenTable);

                        }catch (RemoteException e){
                            System.out.println("Sync worker data with controller failed");
                        }
                    }
                }
                break;
        }

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
                    Thread.sleep(500);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for( int i = 0; i < controllerDataMapping.size(); i++){
                    ControllerInterface controllerInterface = controllerDataMapping.get(i).getControllerInterface();
                    log("Try to ping Controller " + i);
                    try{
                        controllerInterface.ping();
                        if(!controllerDataMapping.get(i).getLiveness()){
                            controllerAlive(i);
                            log("Controller alive " + i );
                        }
                    }catch (RemoteException e){
                        log("Controller not alive");
                        if(controllerDataMapping.get(i).getLiveness()){
                            controllerCrash(i);
                        }
                    }

                }
            }
            //controllerList
        }
    }


    //TODO: when crashe we need to update token stuff a bit
    public void crash(int index){
        lock.lock();
        workerDataMapping.get(index).setLiveness(false);
        workerDataMapping.get(index).setDataIndex(-1);
        workerDataMapping.get(index).setDataLoaded(false);
        tokenTable.removeHost(index);
        lock.unlock();
    }

    public void alive(int index){
        lock.lock();
        workerDataMapping.get(index).setLiveness(true);
        lock.unlock();
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
            tik();
            Random rand = new Random();
            boolean done = false;
            while (done == false) {
                try {
                    Set<Integer> notLoadedSegments = new HashSet<>();
                    for (Integer i = 0; i < dataMapping.size(); i++) {
                        notLoadedSegments.add(i);
                    }
                    for (WorkerDataTuple tuple : workerDataMapping) {
                        if (tuple.getLiveness()) {
                            notLoadedSegments.remove(tuple.dataIndex);
                        }
                    }
                    //TODO: remove segments that have been seen by ALL tokens from notLoaded Segments
                    Set<Integer> seen = new HashSet<>();
                    for (Integer i = 0;i<dataMapping.size();i++) {
                        seen.add(i);
                    }
                    for(Token token: tokenList)
                    {
                        for(Integer notSeen : tokenTable.getLatestVersion(token.getId()).getNotSeen())
                        {
                            seen.remove(notSeen);
                        }
                    }
                    notLoadedSegments.removeAll(seen);
                    if(seen.size()>0){log(seen.toString());}
                    //TODO: create a set of tokens that need to be seen still
                    Iterator<Integer> notLoadedSegmentsIt = notLoadedSegments.iterator();
                    for (WorkerDataTuple tuple : workerDataMapping) {
                        if (tuple.getLiveness()) {
                            //TODO: Add condition that if tuple currently has data that does not need to be seen, reload
                            if((tuple.getDataIndex() < 0 || seen.contains(tuple.getDataIndex())) && notLoadedSegmentsIt.hasNext())
                            {
                                try{
                                    Integer loadIndex = notLoadedSegmentsIt.next();
                                    //log("Loading " + loadIndex + " on " + tuple.index);
                                    DataSegment ds = dataMapping.get(loadIndex);
                                    tuple.workerInterface.loadData(dataPath,ds.start, ds.length);
                                    tuple.setDataIndex(loadIndex);
                                }catch (Exception e){
                                    log("loadData error " + e);
                                }
                            }
                        }
                    }
                    LinkedList<TokenTableEntry> notRunning = new LinkedList<>();
                    for (int i = 0; i < tokenTable.size(); i++) {
                        if (!tokenTable.isRunning(i) && tokenTable.getNextWorker(i) < 0 && !tokenTable.isDone(i)) {
                            notRunning.addFirst(tokenTable.getLatestVersion(i));
                        }
                        if(tokenTable.isDone(i) && tokenList.get(i).getVersion() < 1){
                            Integer hostNum = tokenTable.getLatestVersion(i).getHost();
                            workerDataMapping.get(hostNum).getWorkerInterface().sendToken(-1,i,tokenTable.getLatestVersion(i).getTokenVersion());
                        }
                    }

                    //TODO: Figure out which token goes to each worker
                    for (TokenTableEntry token : notRunning) {
                        Set<Integer> notSeen = token.getNotSeen();
                        double possibleTargets = 1.0;
                        int targetWorker = -1;

                        for (Integer i = 0; i < workerDataMapping.size(); i++) {
                            WorkerDataTuple possibleTargetTuple = workerDataMapping.get(i);
                            if(possibleTargetTuple.getLiveness() ) {
                                //log("Possible token = " + i);
                                //log(possibleTargetTuple.getLiveness() + " " + possibleTargetTuple.isDataLoaded() + " " + notSeen.contains(possibleTargetTuple.getDataIndex() + " " + possibleTargetTuple.getDataIndex()));
                            }
                            if (possibleTargetTuple.getLiveness() && possibleTargetTuple.isDataLoaded() && notSeen.contains(possibleTargetTuple.getDataIndex())) {
                                //log("Passes test " + i);
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
                        System.out.println("token allocation finished");
                    }
                    done = true;
                    for(int i = 0; i<tokenTable.size(); i++){
                        if(!tokenTable.isDone(i)){
                            done = false;
                        }
                    }

                }catch(Exception e){
                    log("EXCEPTION: " + e.getMessage());
                    e.printStackTrace();
                }finally{
                    try {
                        if(!done)
                            Thread.sleep(100);
                    } catch (InterruptedException e) {  }

                }
            }
            /*for(Token token : tokenList){
                System.out.println(token.toJson());
            }*/
            toc();
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
    public Long fileSize;
    public boolean primary;
    public String controllerPort;
    private Lock lock = new ReentrantLock();

    public List<DataSegment> dataMapping;
    public List<WorkerDataTuple> workerDataMapping;
    public List<ControllerDataTuple> controllerDataMapping;
    private TokenTable tokenTable;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Stack<Long> tiks = new Stack<>();
    private void tik(){
        tiks.add(new Date().getTime());
    }
    private void toc(){
        Long now = new Date().getTime();
        System.out.println("Total Time:" + (now - tiks.pop()));
    }
    private void log(String msg){
        Date date = new Date();

        String head = "Controller " + Integer.toString(controllerId) + " " + dateFormat.format(date);
        System.out.println(head + "::" + msg);
    }
}
