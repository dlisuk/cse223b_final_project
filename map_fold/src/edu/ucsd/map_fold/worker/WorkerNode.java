package edu.ucsd.map_fold.worker;

import Jama.Matrix;
import edu.ucsd.map_fold.common.*;
import edu.ucsd.map_fold.common.logistic_regression.LRFolder;
import edu.ucsd.map_fold.common.logistic_regression.LRMapper;
import edu.ucsd.map_fold.common.logistic_regression.LRState;
import edu.ucsd.map_fold.common.logistic_regression.Token;
import edu.ucsd.map_fold.worker.data.MatrixDataSource;
import javafx.util.Pair;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/* WorkerNode -- Main class to launch a worker node */
public class WorkerNode extends UnicastRemoteObject implements WorkerInterface{
    public WorkerNode(int workerId, Config config) throws RemoteException, MalformedURLException {
        Config.WorkerConfig myConf = config.getWorker(workerId);
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
            while(true){
                if( workQueue.isEmpty() ){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    Token token = workQueue.remove();
                    Mapper<Matrix,Matrix> mapper = new LRMapper(token.getFields());
                    LRState state = token.getState();
                    for (Matrix memRec : data) {
                        Matrix rec = mapper.map(memRec);
                        state = folder.fold(state, rec);
                    }
                    Token outToken = token.setState(state);
                    try {
                        uploadToken(outToken);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void uploadToken(Token token) throws RemoteException{
        Integer id = token.getId();
        Integer version = token.getVersion();
        tokenStore.put(new Pair<>(id, version), token);
    }
    public void startWork(int tokenId, int version) throws RemoteException{
        workQueue.add(tokenStore.get(new Pair<>(tokenId, version)));
    }

    public void sendToken(int target, int tokenId, int version) throws RemoteException {
        WorkerInterface targetWorker = workers.get(target);
        Token           token        = tokenStore.get(new Pair<>(tokenId, version));
        targetWorker.uploadToken(token);
    }

    public void loadData(String filePath, int offset, int count) throws RemoteException{
        data = MatrixDataSource.fromFile(filePath,offset,count);
        boolean success = false;
        int i = 0;
        while(success == false && i < controllers.size()){
            try{
                controllers.get(i).dataLoaded(workerId,filePath,offset,count);
                success = true;
            }catch (Exception e){
                i++;
            }
        }
        if( success == false ){
            throw new RemoteException("No controller to inform");
        }
    }

    public void ping(int _workerId) throws RemoteException {
        if( workerId == _workerId ){
            return;
        }else if( workerId == -1 ){
            workerId = _workerId;
        }else{
            throw new RemoteException("Wrong ID Sent in Ping");
        }
    }

    private DataSet<Matrix>                  data       = null;
    private Map<Pair<Integer,Integer>,Token> tokenStore = new HashMap<>();
    private Queue<Token>                     workQueue  = new SynchronousQueue<>();
    private Map<Integer,WorkerInterface>     workers    = new HashMap<>();
    private List<ControllerInterface>        controllers= new ArrayList<>();
    private ExecutorService                  threadPool;
    private Folder<LRState,Matrix>           folder     = new LRFolder();
    private int                              nThreads;
    private int                              workerId=-1;
}
