package Server;

import Server.Interfaces.IServer;

public class ServerContainer
{
    private static IServer server;

    public static IServer getServer()
    {
        if(server == null)
            server = new Server(2,2121);

        return server;
    }
}
