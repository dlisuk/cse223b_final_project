package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class WorkerDataTuple {
    public WorkerDataTuple(WorkerInterface wi, int index, boolean liveness, boolean dataLoaded){
        this.workerInterface = wi;
        this.index = index;
        this.liveness = liveness;
        this.dataLoaded = dataLoaded;
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

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }

    public WorkerInterface workerInterface;
    public int index;
    public int dataIndex = -1;
    public boolean liveness;
    public boolean dataLoaded;
}
