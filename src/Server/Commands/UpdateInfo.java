package Server.Commands;

import Json.JsonConverter;
import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.PokerContainer;

public class UpdateInfo extends SimpleCommandModel implements ICommand
{
    protected ClientTableModel tableModel = new ClientTableModel();
    protected transient ClientSocket Receiver = null;

    public UpdateInfo()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getCommandName()
    {
        return Name;
    }

    public ClientSocket getClient()
    {
        return Receiver;
    }

    public Object getClientObject()
    {
        return tableModel;
    }

    public void setClientToSendCommand(ClientSocket clientSocket) {
        Receiver = clientSocket;
    }

    public void setObjectToSend(Object object)
    {
        tableModel = (ClientTableModel)object;
    }

    public void executeOnServer()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

        tableModel = poker.getClientTable();;

        var jsonMessage = JsonConverter.toJson(this);
        Receiver.sendMessage(jsonMessage);
    }

    public void sendToClient()
    {
        var poker = PokerContainer.getPoker();
        var players = poker.getPlayers();

        ClientTableModel clientTable = poker.getClientTable();

        tableModel = clientTable;

        for(var player : players)
        {
            var jsonMessage = JsonConverter.toJson(this);
            player.Socket.sendMessage(jsonMessage);
        }
    }
}
