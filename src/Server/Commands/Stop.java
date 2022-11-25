package Server.Commands;

import Server.Client;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.PokerContainer;
import Server.ServerContainer;

public class Stop extends SimpleCommandModel implements ICommand
{
    protected transient Client Receiver = null;
    public Stop()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public void setReceiver(Client client) {
        Receiver = client;
    }

    public void setObjectToSend(Object object) {

    }

    public Object getReceivedObject() {
        return null;
    }

    public Client getReceiver() {
        return Receiver;
    }

    public void execute()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

        ServerContainer.getServer().stop();
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
