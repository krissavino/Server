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

    public String getCommandName()
    {
        return Name;
    }

    public ClientSocket getClient() {
        return Receiver;
    }

    public void setClientToSendCommand(ClientSocket clientSocket) {
        Receiver = clientSocket;
    }

    public void setObjectToSend(Object object) {

    }

    public Object getClientObject() {
        return null;
    }

    public void executeOnServer()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

    }

    public void sendToClient()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Получатель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Получатель %s, команда: %s",player.NickName ,Name));
    }
}
