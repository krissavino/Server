package Server.Poker.Interfaces;

import Server.ClientSocket;
import Server.Poker.Models.PlayerModel;

public interface IQueue
{
    public void addPlayerToQueue(PlayerModel player);

    public void PlacePlayersFromQueue();
}
