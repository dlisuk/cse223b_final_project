package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/26/14.
 */
public class WorkerConf {
    public WorkerConf(String ipAddr, String port, int thread){
        this.ipAddr = ipAddr;
        this.port = port;
        this.thread = thread;
    }

    public String getUrl(){
        return ipAddr + ":" + port;
    }

    public String getRmiPath(){
        return getUrl() + "/Worker";
    }

    public String ipAddr;
    public String port;
    public int thread;
}
