package Server.Commands;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.PokerContainer;

public class MoveFold extends SimpleCommandModel implements ICommand
{
    protected transient ClientSocket Client = null;

    public MoveFold() { Name = this.getClass().getSimpleName(); }

    public String getCommandName() { return Name; }

    public ClientSocket getClient()  { return null; }

    public Object getClientObject() { return null; }

    public void setClientToSendCommand(ClientSocket clientSocket) {
        Client = clientSocket;
    }

    public void setObjectToSend(Object object) {}

    public void executeOnServer()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Client);

        if(player == null)
            System.out.printf("Отправитель: имя неизвестно, команда: %s%n", Name);
        else
            System.out.printf("Отправитель %s, команда: %s%n",player.NickName ,Name);

        poker.move(player, MoveType.Fold, 0);
    }

    public void sendToClient() {}
}
