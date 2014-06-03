package edu.ucsd.map_fold;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ServerHelpers {
    //Sourced from http://stackoverflow.com/questions/494465/how-to-enumerate-ip-addresses-of-all-enabled-nic-cards-from-java
    public static Set<String> getIPs(){
        Set<String> ips = new HashSet<>();

        try {
            Enumeration<NetworkInterface> enumNI =  NetworkInterface.getNetworkInterfaces();
            while ( enumNI.hasMoreElements() ){
                NetworkInterface ifc             = enumNI.nextElement();
                if( ifc.isUp() ){
                    Enumeration<InetAddress> enumAdds     = ifc.getInetAddresses();
                    while ( enumAdds.hasMoreElements() ){
                        InetAddress addr                  = enumAdds.nextElement();
                        ips.add(addr.getHostAddress());
                        ips.add(addr.getCanonicalHostName());
                        ips.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e){
            e.printStackTrace();
        }
        return ips;
    }
}
