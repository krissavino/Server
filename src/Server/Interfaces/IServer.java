package Server.Interfaces;

import Server.ClientSocket;
import Server.Commands.Enums.CommandEnum;
import Server.Commands.Interfaces.ICommand;

import java.net.InetAddress;
import java.util.ArrayList;

public interface IServer
{
    void start(InetAddress inetAddress);

    void stop();

    ArrayList<ClientSocket> getClients();

    void disconnectClient(ClientSocket clientSocket);

    void executeCommand(CommandEnum commandEnum);

    void executeCommand(ICommand command);
}
