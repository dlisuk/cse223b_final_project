package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/29/14.
 */
public class ControllerConf {
    public ControllerConf(String ipAddr, String port, boolean primary){
        this.ipAddr = ipAddr;
        this.port = port;
        this.primary = primary;
    }

    public String getUrl(){
        return ipAddr + ":" + port;
    }

    public String getRmiPath(){
        return getUrl() + "/Controller";
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String ipAddr;
    public String port;
    public boolean primary;
}
