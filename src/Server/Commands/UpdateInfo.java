package Server.Commands;

import Json.JsonConverter;
import Server.Client;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.PokerContainer;

public class UpdateInfo extends SimpleCommandModel implements ICommand
{
    protected ClientTableModel tableModel = new ClientTableModel();
    protected transient Client Receiver = null;

    public UpdateInfo()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public Client getReceiver()
    {
        return Receiver;
    }

    public Object getReceivedObject()
    {
        return tableModel;
    }

    public void setReceiver(Client client) {
        Receiver = client;
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


        var table = poker.getTable();
        tableModel = table;

        var jsonMessage = JsonConverter.toJson(this);
        Receiver.sendMessage(jsonMessage);
    }

    public void send()
    {
        var poker = PokerContainer.getPoker();
        var clients = poker.getPlayers().keySet();
        var table = poker.getTable();

        ClientTableModel clientTable = new ClientTableModel();
        clientTable.Pot             = table.Pot            ;
        clientTable.Bet             = table.Bet            ;
        clientTable.PlayerIndexTurn = table.PlayerIndexTurn;
        clientTable.TimerTime       = table.TimerTime      ;
        clientTable.Winner          = table.Winner         ;
        clientTable.Stage           = table.Stage          ;
        clientTable.State           = table.State          ;
        clientTable.CardsOnTable    = table.CardsOnTable   ;
        clientTable.Players         = table.Players        ;

        tableModel = clientTable;

        for(var client : clients)
        {
            var jsonMessage = JsonConverter.toJson(this);
            client.sendMessage(jsonMessage);
        }
    }
}
