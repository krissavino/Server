package Net;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

public class LocalNetManager
{
    public static ArrayList<InetAddress> GetLocalInetAddresses()
    {
        ArrayList<NetworkInterface> networkInterfaces;
        ArrayList<InetAddress> localInetAddresses = new ArrayList();

        try
        {
            networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for(var netInterface : networkInterfaces)
            {
                for(var inetAddress : Collections.list(netInterface.getInetAddresses()))
                {
                    if(inetAddress instanceof Inet4Address)
                        localInetAddresses.add(inetAddress);
                }
            }
        }
        catch (SocketException e)
        {
            throw new RuntimeException(e);
        }

        return localInetAddresses;
    }
}
