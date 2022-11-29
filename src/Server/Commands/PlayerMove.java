package Server.Commands;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.PokerContainer;

public class PlayerMove extends SimpleCommandModel implements ICommand
{
    protected int bet = -1;
    protected transient ClientSocket Receiver = null;

    public PlayerMove()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public ClientSocket getReceiver() { return Receiver; }

    public Object getReceivedObject()
    {
        return bet;
    }
    public void setObjectToSend(Object object) {
    }

    public void setReceiver(ClientSocket clientSocket) { Receiver = clientSocket; }

    public void execute()
    {
        var Poker = PokerContainer.getPoker();
        var player = Poker.getPlayer(Receiver);
        Poker.move(player, MoveType.Bet, bet);

    }

    public void send() {

    }
}
