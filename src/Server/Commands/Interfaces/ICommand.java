package Server.Commands.Interfaces;

import Server.ClientSocket;

public interface ICommand
{
    String getCommandName();

    ClientSocket getClient();

    Object getClientObject();

    void setClientToSendCommand(ClientSocket clientSocket);

    void setObjectToSend(Object object);

    void executeOnServer();

    void sendToClient();
}
