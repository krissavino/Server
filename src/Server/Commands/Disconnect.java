package Server.Commands;

import Json.JsonConverter;
import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.PokerContainer;

public class Disconnect extends SimpleCommandModel implements ICommand
{
    protected transient ClientSocket Client = null;

    public Disconnect() { Name = this.getClass().getSimpleName(); }

    public String getCommandName() { return Name; }

    public ClientSocket getClient() { return Client; }

    public void setClientToSendCommand(ClientSocket clientSocket) { Client = clientSocket; }

    public void setObjectToSend(Object object) {}
    
    public Object getClientObject() { return null; }

    public void executeOnServer() {}

    public void sendToClient()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Client);

        if(player == null)
            System.out.printf("Получатель: имя неизвестно, команда: %s%n", Name);
        else
            System.out.printf("Получатель %s, команда: %s%n",player.NickName ,Name);

        var text = JsonConverter.toJson(this);
        Client.sendMessage(text);
    }
}
