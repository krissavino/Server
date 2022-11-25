package Server.Models;

import Server.Client;
import Server.Commands.*;
import Server.Commands.Enums.CommandEnum;
import Server.Commands.Interfaces.ICommand;
import Server.Interfaces.IThreadController;
import Server.ThreadController;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public final class ServerModel
{
    public int Port;
    public int MaxClients = 2;
    public boolean IsServerStarted = false;
    public ServerSocket Socket;
    public Map<Socket, Client> Clients = new HashMap(4);
    public Map<CommandEnum, ICommand> Commands = new HashMap();
    public IThreadController ThreadController = new ThreadController();

    public ServerModel(int maxClients)
    {
        if(maxClients < 2)
        {
            Clients = new HashMap(2);
            this.MaxClients = 2;
        }
        else
        {
            Clients = new HashMap(maxClients);
            this.MaxClients = maxClients;
        }

        Commands.put(CommandEnum.Empty,new Empty());
        Commands.put(CommandEnum.GiveAwayCards,new GiveAwayCards());
        Commands.put(CommandEnum.PlaceOnTable,new PlaceOnTable());
        Commands.put(CommandEnum.RegisterPokerPlayer,new RegisterPokerPlayer());
        Commands.put(CommandEnum.Stop,new Stop());
        Commands.put(CommandEnum.UpdateInfo,new UpdateInfo());
    }
}
