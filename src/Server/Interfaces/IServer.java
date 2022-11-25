package Server.Interfaces;

import Server.Client;
import Server.Commands.Enums.CommandEnum;
import Server.Commands.Interfaces.ICommand;

import java.util.ArrayList;

public interface IServer
{
    void start();

    void stop();

    ArrayList<Client> getClients();

    void disconnectClient(Client client);

    void executeCommand(CommandEnum commandEnum);

    void executeCommand(ICommand command);
}
