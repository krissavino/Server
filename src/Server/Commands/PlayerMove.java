package Server.Commands;

import Json.JsonConverter;
import Server.Client;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.PokerContainer;

public class PlayerMove extends SimpleCommandModel implements ICommand
{
    protected int bet = -1;
    protected transient Client Receiver = null;

    public PlayerMove()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public Client getReceiver() { return Receiver; }

    public Object getReceivedObject()
    {
        return bet;
    }
    public void setObjectToSend(Object object) {
    }

    public void setReceiver(Client client) { Receiver = client; }

    public void execute()
    {
        var Poker = PokerContainer.getPoker();
        var player = Poker.getPlayer(Receiver);
        Poker.move(player, MoveType.Bet, bet);

    }

    public void send() {

    }
}
