package edu.ucsd.map_fold.worker;

import Jama.Matrix;
import edu.ucsd.map_fold.common.DataSet;
import edu.ucsd.map_fold.common.Folder;
import edu.ucsd.map_fold.common.Mapper;
import edu.ucsd.map_fold.common.logistic_regression.LRFolder;
import edu.ucsd.map_fold.common.logistic_regression.LRMapper;
import edu.ucsd.map_fold.common.logistic_regression.LRState;
import edu.ucsd.map_fold.common.logistic_regression.Token;
import edu.ucsd.map_fold.common.WorkerInterface;
import edu.ucsd.map_fold.worker.data.MatrixDataSource;
import javafx.util.Pair;

import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/* WorkerNode -- Main class to launch a worker node */
public class WorkerNode extends UnicastRemoteObject implements WorkerInterface{
    public WorkerNode(int nThreads) throws RemoteException{
        threadPool = Executors.newFixedThreadPool(nThreads);
        for(int i = 0; i<nThreads; i++){
            threadPool.submit(new WorkerThread());
        }
        //todo:populate workers table
        while(true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
    }
    private DataSet<Matrix>                  data       = null;
    private Map<Pair<Integer,Integer>,Token> tokenStore = new HashMap<>();
    private Queue<Token>                     workQueue  = new SynchronousQueue<>();
    private Map<Integer,WorkerInterface>     workers    = new HashMap<>();
    private ExecutorService                  threadPool;
    private Folder<LRState,Matrix>           folder     = new LRFolder();
}
