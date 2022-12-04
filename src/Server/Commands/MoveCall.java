package Server.Commands;

import Json.JsonConverter;
import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.Models.SimpleCommandModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.PokerContainer;

public class MoveCall extends SimpleCommandModel implements ICommand
{
    protected transient ClientSocket Client = null;

    public MoveCall() { Name = this.getClass().getSimpleName(); }

    public String getCommandName() { return Name; }

    public ClientSocket getClient() { return Client; }

    public Object getClientObject() { return null;}

    public void setClientToSendCommand(ClientSocket clientSocket) { Client = clientSocket; }

    public void setObjectToSend(Object object) { }

    public void executeOnServer()
    {
        var poker = PokerContainer.getPoker();
        var player = poker.getPlayer(Client);

        if(player == null)
            System.out.println(String.format("Отправитель: имя неизвестно, команда: %s", Name));
        else
            System.out.println(String.format("Отправитель %s, команда: %s",player.NickName ,Name));

        poker.move(player, MoveType.Call, 0);
    }

    public void sendToClient() {}
}
