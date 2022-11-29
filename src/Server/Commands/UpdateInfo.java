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

    public String getName()
    {
        return Name;
    }

    public ClientSocket getReceiver()
    {
        return Receiver;
    }

    public Object getReceivedObject()
    {
        return tableModel;
    }

    public void setReceiver(ClientSocket clientSocket) {
        Receiver = clientSocket;
    }

    public void setObjectToSend(Object object)
    {
        tableModel = (ClientTableModel)object;
    }

    public void execute()
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

    public void send()
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
