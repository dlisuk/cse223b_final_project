package edu.ucsd.map_fold.common;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.function.Function;

public abstract class GenericClient<ClientType extends Remote> {
    protected boolean call(Function<ClientType, RemoteException>f) throws RemoteException {
        int trys = 0;
        while (connection == null && trys <= 3)
            try {
                if (connection == null)
                    connection = (ClientType) Naming.lookup(addr);
                RemoteException e = f.apply(connection);
                if( e != null){
                    throw e;
                }
                return true;
            } catch (NotBoundException e) {
                connection = null;
                trys ++;
                try { Thread.sleep(500*trys); } catch (InterruptedException ignored) { }
            } catch (MalformedURLException ignored) {}
        if (connection == null){
            throw new ConnectException("Server not up");
        }
        return false;
    }
    protected void checkAddress() throws MalformedURLException {
        try {
            connection = (ClientType) Naming.lookup(addr);
        } catch (NotBoundException | RemoteException Ignored) {
            connection = null;
        }
    }

    protected String addr;

    private ClientType connection;
}
