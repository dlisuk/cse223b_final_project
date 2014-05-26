package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/26/14.
 */
public class WorkerConf {
    public WorkerConf(String ipAddr, int port, int thread){
        this.ipAddr = ipAddr;
        this.port = port;
        this.thread = thread;
    }

    public String getUrl(){
        return ipAddr + ":" + port;
    }

    public String ipAddr;
    public int port;
    public int thread;
}
