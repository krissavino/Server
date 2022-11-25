package Server.Commands;

import Server.Client;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.PokerContainer;

public class Check extends SimpleCommandModel implements ICommand
{
    protected transient Client Receiver = null;

    public Check()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public Client getReceiver() {
        return null;
    }

    public Object getReceivedObject()
    {
        return null;
    }

    public void setReceiver(Client client) {
        Receiver = client;
    }

    public void setObjectToSend(Object object) {
    }

    public void execute()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

        poker.move(player, MoveType.Check, 0);
    }

    public void send()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Получатель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Получатель %s, команда: %s",player.NickName ,Name));
    }
}
