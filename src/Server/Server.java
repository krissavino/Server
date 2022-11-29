package Server;

import Server.Commands.*;
import Server.Commands.Enums.CommandEnum;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Interfaces.ISimpleCommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Interfaces.IServer;
import Server.Interfaces.IThreadController;
import Server.Models.ServerModel;
import Server.Poker.Models.PlayerModel;
import Server.Poker.PokerContainer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Server implements IServer
{
    private ServerModel Server = new ServerModel(4);

    private static Lock _locker = new ReentrantLock();

    public static void main(String[] args)
    {
        ServerContainer.getServer().start();
    }

    public Server(int maxClients, int port)
    {
        Server = new ServerModel(maxClients);
        Server.Port = port;
    }

    public void start()
    {
        if(Server.IsServerStarted == true)
        {
            System.out.println("Server already started!");
            return;
        }

        Server.ThreadController.reset();

        try
        {
            InetAddress localHostAddress = InetAddress.getLocalHost();
            Server.Socket = new ServerSocket(Server.Port, Server.MaxClients,localHostAddress);

            var threadServer = new Thread(()->waitForClient());
            threadServer.start();
            Server.IsServerStarted = true;

            System.out.println("Server started: " + localHostAddress.getHostAddress() + ", port: " + Server.Port);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void stop()
    {
        try
        {
            Server.ThreadController.Cancel();
            Server.Socket.close();
            Server.IsServerStarted = false;
            System.out.println("Server stopped");
        }
        catch (IOException e)
        {
            System.out.println("Server stopped, error:: " + e.getMessage());
        }
    }

    public void disconnectClient(ClientSocket client)
    {
        var clientSocket = client.getSocket();

        try
        {
            var poker = PokerContainer.getPoker();
            var player = poker.getPlayer(client);

            clientSocket.close();
            Server.Clients.remove(clientSocket);

            if(player == null)
                System.out.println("Server disconnected client with the same nickname");
            else
                System.out.println("Server disconnected client: " + player.NickName);
        }
        catch (IOException e)
        {
            System.out.println("Error while player disconnecting: " + e.getMessage());
        }
    }

    public ArrayList<ClientSocket> getClients()
    {
        var clients = new ArrayList<ClientSocket>();

        for (var client : Server.Clients.entrySet())
            clients.add(client.getValue());

        return clients;
    }

    private void waitForClient()
    {
        while(Server.ThreadController.isCancellationRequested() == false)
        {
            try
            {
                var clientSocket = Server.Socket.accept();
                var newClient = new ClientSocket(clientSocket);
                Server.Clients.put(clientSocket,newClient);

                new Thread(()-> listenForMessages(newClient, Server.ThreadController)).start();

                System.out.println("Server connected new client");
            }
            catch (Exception e)
            {
                if(e.getMessage().equals("Socket closed"))
                    return;

                throw new RuntimeException(e);
            }
        }
    }

    private void listenForMessages(ClientSocket clientSocket, IThreadController threadController)
    {
        String message;
        try
        {
            while (!threadController.isCancellationRequested())
            {
                message =  clientSocket.getBufferedReader().readLine();
                var command = tryGetCommand(message);
                command.setReceiver(clientSocket);
                executeCommand(command);
            }
        }
        catch (IOException e)
        {
            if(e.getMessage().equals("Connection reset") == false)
                return;

            _locker.lock();

            try
            {
                playerDisconnected(clientSocket);
            }
            finally
            {
                _locker.unlock();
            }
        }
    }

    private void playerDisconnected(ClientSocket clientSocket)
    {
        var player = PokerContainer.getPoker().getPlayer(clientSocket);

        if(player == null)
            player = new PlayerModel();

        System.out.println( String.format("Player <%s> disconnected",player.NickName));

        PokerContainer.getPoker().removePlayer(clientSocket);
        Server.Clients.remove(clientSocket.getSocket());
        PokerContainer.getPoker().setWinner();
    }

    private ICommand tryGetCommand(String jsonText)
    {
        Gson gson = new Gson();
        ISimpleCommand jCommand = gson.fromJson(jsonText, new TypeToken<SimpleCommandModel>(){}.getType());
        ICommand command = new Empty();

        if(jCommand == null)
            return command;

        for(var commandEnum : CommandEnum.values())
        {
            if (jCommand.getName().equals(commandEnum.toString()) == false)
                continue;

            if (Server.Commands.get(commandEnum) == null)
                continue;

            command = gson.fromJson(jsonText, Server.Commands.get(commandEnum).getClass());
            System.out.println("Command received: " + command.getName());
            break;
        }

        return command;
    }

    public void executeCommand(ICommand command)
    {
        command.execute();
    }

    public void executeCommand(CommandEnum commandEnum)
    {
        var command = Server.Commands.get(commandEnum);
        command.execute();
    }
}
