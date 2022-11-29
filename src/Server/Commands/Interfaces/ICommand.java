package Server.Commands.Interfaces;

import Server.ClientSocket;

public interface ICommand
{
    String getName();

    ClientSocket getReceiver();

    Object getReceivedObject();

    void setReceiver(ClientSocket clientSocket);

    void setObjectToSend(Object object);

    void execute();

    void send();
}
