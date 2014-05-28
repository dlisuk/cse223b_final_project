package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class WorkerDataTuple {
    public WorkerDataTuple(WorkerInterface wi, int index){
        this.workerInterface = wi;
        this.dataIndex = index;
    }

    public WorkerInterface getWorkerInterface(){
        return this.workerInterface;
    }

    public int getDataIndex(){
        return this.dataIndex;
    }

    public void setDataIndex(int index){
        this.dataIndex = index;
    }

    public void setLiveness(boolean liveness) {
        this.liveness = liveness;
    }

    public boolean getLiveness(){
        return this.liveness;
    }

    public WorkerInterface workerInterface;
    public int dataIndex;
    public boolean liveness;
}
