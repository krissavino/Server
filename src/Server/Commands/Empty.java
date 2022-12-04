package Server.Commands;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.PokerContainer;

public class Empty extends SimpleCommandModel implements ICommand
{
    protected transient ClientSocket Client = null;

    public Empty() { Name = this.getClass().getSimpleName(); }

    public String getCommandName() { return Name; }

    public ClientSocket getClient() { return Client; }

    public void setClientToSendCommand(ClientSocket clientSocket) { Client = clientSocket; }

    public void setObjectToSend(Object object) {}

    public Object getClientObject() { return null; }

    public void executeOnServer() {}

    public void sendToClient() {}
}
