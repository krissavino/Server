package Server.Interfaces;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;

import java.net.InetAddress;

public interface IServer
{
    void start(InetAddress inetAddress);

    void stop();

    void disconnectClient(ClientSocket clientSocket);

    void executeCommand(ICommand command);
}
