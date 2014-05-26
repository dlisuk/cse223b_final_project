package edu.ucsd.map_fold.common;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
/**
 * Created by max on 5/20/14.
 */
public class ControllerClient extends GenericClient<ControllerInterface> implements ControllerInterface{

    public static ControllerInterface connectToController(String IP) throws MalformedURLException {
        return new ControllerClient(IP);
    }

    public void doneWithWork() throws RemoteException {
        call( w -> {try{w.doneWithWork(); return null;}catch (RemoteException e){ return e; }} );
    }
    public void tokenReceived(int tokenId, int tokenVersion) throws RemoteException {
        call( w -> {try{w.tokenReceived(tokenId,tokenVersion); return null;}catch (RemoteException e){ return e; }} );
    }
    public void dataLoaded(String filePath, int offset, int count) throws RemoteException {
        call( w -> {try{w.dataLoaded(filePath,offset, count); return null;}catch (RemoteException e){ return e; }} );
    }
    private ControllerClient (String _addr) throws MalformedURLException {
        addr = "//" + _addr + "/Controller";
        checkAddress();
    }
}
