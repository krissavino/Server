package Server.Commands;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.PokerContainer;

public class MoveRaise extends SimpleCommandModel implements ICommand
{
    protected int Bet = 0;
    protected transient ClientSocket Client = null;

    public MoveRaise() { Name = this.getClass().getSimpleName(); }

    public String getCommandName() { return Name; }

    public ClientSocket getClient() { return Client; }

    public Object getClientObject() { return Bet; }

    public void setClientToSendCommand(ClientSocket clientSocket) { Client = clientSocket; }

    public void setObjectToSend(Object object) { Bet = (int) object; }

    public void executeOnServer()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Client);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

        poker.move(player, MoveType.Raise, Bet);
    }

    public void sendToClient() {}
}
