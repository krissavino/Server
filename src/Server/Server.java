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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Server implements IServer
{
    private ServerModel Server = new ServerModel(4);

    private static final Lock _locker = new ReentrantLock();

    public Server(int maxClients, int port)
    {
        Server = new ServerModel(maxClients);
        Server.Port = port;
    }

    public void start(InetAddress inetAddress)
    {
        if(Server.IsServerStarted)
        {
            System.out.println("Сервер уже запущен, отменён повторный запуск!");
            return;
        }

        Server.ThreadController.reset();

        try
        {
            Server.Socket = new ServerSocket(Server.Port, Server.MaxClients,inetAddress);

            var threadServer = new Thread(()->waitForClient());
            threadServer.start();
            Server.IsServerStarted = true;

            var text = String.format("Server started: %s, port: %s", inetAddress.getHostAddress(), Server.Port);
            System.out.println(text);
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

            var disconnect = new Disconnect();
            disconnect.setClientToSendCommand(client);
            disconnect.sendToClient();
            clientSocket.close();
            Server.Clients.remove(clientSocket);

            if(player == null)
                System.out.println("Сервер отключил игрока, который пытался зайти под именем уже зарегистрированного игрока");
            else
                System.out.println("Сервер отключил игрока: " + player.NickName);
        }
        catch (IOException e)
        {
            System.out.println("Ошибка при отключении игрока: " + e.getMessage());
        }
    }

    private void waitForClient()
    {
        while(!Server.ThreadController.isCancellationRequested())
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
                command.setClientToSendCommand(clientSocket);
                executeCommand(command);
            }
        }
        catch (IOException e)
        {
            if(!e.getMessage().equals("Connection reset"))
                return;

            _locker.lock();

            try
            {
                markPlayerAsDisconnected(clientSocket);
            }
            finally
            {
                _locker.unlock();
            }
        }
    }

    private void markPlayerAsDisconnected(ClientSocket clientSocket)
    {
        var player = PokerContainer.getPoker().getPlayer(clientSocket);

        if(player == null)
            player = new PlayerModel();

        player.Disconnected = true;

        var text = String.format("Игрок <%s> отключился",player.NickName);
        System.out.println(text);

        PokerContainer.getPoker().markPlayerAsDisconnected(player);
        Server.Clients.remove(clientSocket.getSocket());
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
            if (!jCommand.getCommandName().equals(commandEnum.toString()))
                continue;

            if (Server.Commands.get(commandEnum) == null)
                continue;

            command = gson.fromJson(jsonText, Server.Commands.get(commandEnum).getClass());
            break;
        }

        return command;
    }

    public void executeCommand(ICommand command)
    {
        command.executeOnServer();
    }
}
