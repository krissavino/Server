package Server.Commands;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.Interfaces.IPokerMove;
import Server.Poker.PokerContainer;

public class MoveBet extends SimpleCommandModel implements ICommand
{
    protected int Bet = 0;
    protected transient ClientSocket Client = null;

    public MoveBet() { Name = this.getClass().getSimpleName(); }

    public String getCommandName() { return Name; }

    public ClientSocket getClient() { return Client; }

    public Object getClientObject() { return Bet; }

    public void setClientToSendCommand(ClientSocket clientSocket) { Client = clientSocket; }

    public void setObjectToSend(Object object) { Bet = (int) object; }

    public void executeOnServer()
    {
        var poker = PokerContainer.getPoker();
        var pokerMove = PokerContainer.getPokerMove();
        var player = poker.getPlayer(Client);

        if(player == null)
            System.out.printf("Отправитель: имя неизвестно, команда: %s%n, отказано в выполнении", Name);
        else
        {
            System.out.printf("Отправитель %s, команда: %s%n", player.NickName, Name);
            pokerMove.move(player, MoveType.Bet, Bet);
        }
    }

    public void sendToClient() {}
}
