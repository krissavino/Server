package Server.Commands.Interfaces;

import Server.Client;

public interface ICommand
{
    String getName();

    Client getReceiver();

    Object getReceivedObject();

    void setReceiver(Client client);

    void setObjectToSend(Object object);

    void execute();

    void send();
}
