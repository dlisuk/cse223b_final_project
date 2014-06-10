package edu.ucsd.map_fold.worker;

import Jama.Matrix;
import edu.ucsd.map_fold.common.*;
import edu.ucsd.map_fold.common.logistic_regression.LRFolder;
import edu.ucsd.map_fold.common.logistic_regression.LRMapper;
import edu.ucsd.map_fold.common.logistic_regression.LRState;
import edu.ucsd.map_fold.common.logistic_regression.Token;
import edu.ucsd.map_fold.worker.data.MatrixDataSource;
import javafx.util.Pair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* WorkerNode -- Main class to launch a worker node */
public class WorkerNode extends UnicastRemoteObject implements WorkerInterface{
    public WorkerNode(int _WorkerId, Config config) throws RemoteException, MalformedURLException {
        Config.WorkerConfig myConf = config.getWorker(_WorkerId);
        workerId=_WorkerId;
        nThreads = myConf.getNThreads();
        threadPool = Executors.newFixedThreadPool(nThreads);

        for( int i = 0; i < config.getNworkers(); i++){
            Config.WorkerConfig wConfig = config.getWorker(i);
            workers.put(i,WorkerClient.connectToWorker(wConfig.getAddr()));
        }
        for( int i = 0; i < config.getNcontrollers(); i++){
            Config.ControllerConfig cConfig = config.getController(i);
            controllers.add(ControllerClient.connectToController(cConfig.getAddr()));
        }
    }

    public void start(){
        for(int i = 0; i<nThreads; i++){
            threadPool.submit(new WorkerThread());
        }
    }

    private class WorkerThread implements Runnable{
        public WorkerThread(){}
        public void run() {
            log("Worker thread up");
            while(true){
                lock.lock();
                Token token = null;
                try {
                    token = workQueue.remove();
                    working++;
                }catch(NoSuchElementException e){
                    token = null;
                }
                lock.unlock();
                if (token != null) {
                    log("Starting work: " + Integer.toString(token.getId()));
                    LRState state = token.getState();
                    log("Starting folding: " + Integer.toString(token.getId()));
                    int i = 0;
                    for (Double[] rec : data) {
                        if( i%100==0) log("Line " + i);
                        state = folder.fold(state, rec);
                        i++;
                    }
                    Token outToken = token.setState(state);
                    try {
                        uploadToken(outToken);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } finally {
                        lock.lock();
                        working--;
                        lock.unlock();
                    }
                    for(ControllerInterface controller:controllers){
                        try {
                            controller.doneWithWork(workerId, token.getId(), token.getVersion());
                            break;
                        }catch(RemoteException e){
                            log(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void uploadToken(Token token) throws RemoteException{
        lock.lock();
        log("uploadToken(" + Integer.toString(token.getId()) + ", " + Integer.toString(token.getVersion()));
        if(data == null){
            lock.unlock();
            throw new RemoteException("Cannot upload tokens when there is no data");
        }
        Integer id = token.getId();
        Integer version = token.getVersion();
        tokenStore.put(new Pair<>(id, version), token);
        lock.unlock();

        for(ControllerInterface controller:controllers){
            try {
                controller.tokenReceived(workerId, token.getId(), token.getVersion());
                break;
            }catch(RemoteException e){
                log(e.getMessage());
            }
        }
    }
    public void startWork(int tokenId, int version) throws RemoteException{
        lock.lock();
        log("startWork(" + Integer.toString(tokenId) + ", " + Integer.toString(version) + ")");
        workQueue.add(tokenStore.get(new Pair<>(tokenId, version)));
        log("WQ Size In Start Work: " + workQueue.size());
        lock.unlock();
    }

    public void sendToken(int target, int tokenId, int version) throws RemoteException {
        log("sendToken(" + Integer.toString(target) + ", " + Integer.toString(tokenId) + ", " + Integer.toString(version));
        Token token = tokenStore.get(new Pair<>(tokenId, version));
        if(target  < 0 ){
            //Send to controller in this case
            for(ControllerInterface controller:controllers){
                try {
                    controller.uploadToken(workerId, token);
                    break;
                }catch(RemoteException e){
                    log(e.getMessage());
                }
            }
        }else {
            WorkerInterface targetWorker = workers.get(target);
            targetWorker.uploadToken(token);
        }
    }

    public void loadData(String filePath, int offset, int count) throws RemoteException{
        log("loadData(" + filePath + ", " + Integer.toString(offset) + ", " + Integer.toString(count));
        if(working>0 || workQueue.size()>0){
            throw new RemoteException("Work queued for loaded data, cannot load new data");
        }
        Runnable task = () -> {
            try{
                lock.lock();
                data = null;
                data = MatrixDataSource.fromFile(filePath, offset, count);
                boolean success = false;
                int i = 0;
                while (success == false && i < controllers.size()) {
                    try {
                        controllers.get(i).dataLoaded(workerId, filePath, offset, count);
                        success = true;
                    } catch (Exception e) {
                        i++;
                    }
                }
                if(success == false){
                    throw new Exception("No controller to inform");
                }
            } catch (Exception e) {
                lock.unlock();
                log(e.getMessage());
                return;
            }finally{
                lock.unlock();
            }
        };

        new Thread(task,"Data Loading Thread").start();
    }

    public void ping(int _workerId) throws RemoteException {
        if( workerId == _workerId ){
            return;
        }else {
            log("PING LOCK");
            lock.lock();
            if( workerId == -1 ){
                workerId = _workerId;
                lock.unlock();
                log("PING UNLOCK");
            }else{
                lock.unlock();
                log("PING UNLOCK");
                throw new RemoteException("Wrong ID Sent in Ping");
            }
        }
    }

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private void log(String msg){
        Date date = new Date();

        String head = "Worker " + Integer.toString(workerId) + " " + dateFormat.format(date);
        System.out.println(head + "::" + msg);
    }

    private Lock                             lock       = new ReentrantLock();
    private Integer                          working    = 0;
    private DataSet<Double[]>                data       = null;
    private Map<Pair<Integer,Integer>,Token> tokenStore = new HashMap<>();
    private Queue<Token>                     workQueue  = new LinkedBlockingQueue<>();
    private Map<Integer,WorkerInterface>     workers    = new HashMap<>();
    private List<ControllerInterface>        controllers= new ArrayList<>();
    private ExecutorService                  threadPool;
    private Folder<LRState,Double[]>         folder     = new LRFolder();
    private int                              nThreads;
    private int                              workerId=-1;

}
