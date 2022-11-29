package Server.Commands;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.PokerContainer;

public class GiveAwayCards extends SimpleCommandModel implements ICommand
{
    protected transient ClientSocket Receiver = null;

    public GiveAwayCards()
    {
        Name = GiveAwayCards.class.getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public ClientSocket getReceiver() {
        return Receiver;
    }

    public void setReceiver(ClientSocket clientSocket) {
        Receiver = clientSocket;
    }

    public void setObjectToSend(Object object) {

    }

    public Object getReceivedObject() {
        return null;
    }

    public void execute()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

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
