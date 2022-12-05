package Server.Poker.Interfaces;

import Server.ClientSocket;
import Server.Poker.Models.PlayerModel;

public interface IQueue
{
    void addPlayerToQueue(PlayerModel player);

    void placePlayersFromQueue();
}
