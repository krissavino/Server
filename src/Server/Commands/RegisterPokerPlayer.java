package Server.Commands;

import Json.JsonConverter;
import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Models.PlayerModel;
import Server.Poker.PokerContainer;

public final class RegisterPokerPlayer extends SimpleCommandModel implements ICommand
{
    protected PlayerModel Player = new PlayerModel();
    protected transient ClientSocket Receiver = null;

    public RegisterPokerPlayer()
    {
        Name = this.getClass().getSimpleName();
    }

    public String getCommandName()
    {
        return Name;
    }

    public ClientSocket getClient() { return Receiver; }

    public Object getClientObject() {
        return Player;
    }

    public void setClientToSendCommand(ClientSocket clientSocket) { Receiver = clientSocket; }

    public void setObjectToSend(Object object) {
        Player = (PlayerModel) object;
    }

    public void executeOnServer()
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

    public void sendToClient()
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
