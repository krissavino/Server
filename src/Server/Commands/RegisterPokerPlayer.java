package Server.Commands;

import Json.JsonConverter;
import Server.Client;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Models.PlayerModel;
import Server.Poker.PokerContainer;
import com.google.gson.reflect.TypeToken;

public final class RegisterPokerPlayer extends SimpleCommandModel implements ICommand
{
    protected PlayerModel Player = new PlayerModel();
    protected transient Client Receiver = null;

    public RegisterPokerPlayer()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getName()
    {
        return Name;
    }

    public Client getReceiver() { return Receiver; }

    public Object getReceivedObject() {
        return Player;
    }

    public void setReceiver(Client client) { Receiver = client; }

    public void setObjectToSend(Object object) {
        Player = (PlayerModel) object;
    }

    public void execute()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);
        if(player == null) {
            System.out.println(String.format("Sender: N\\A, command: %s", Name));
        }
        else
            System.out.println(String.format("Sender: %s, command: %s",player.NickName ,Name));

        poker.authorizePlayer(Receiver, Player);
    }

    public void send()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Receiver);

        if(player == null)
            System.out.println(String.format("Receiver: N\\A, command: %s", Name));
        else
            System.out.println(String.format("Receiver %s, command: %s",player.NickName ,Name));

        Player = player;

        var text = JsonConverter.toJson(this);
        Receiver.sendMessage(text);
    }
}
