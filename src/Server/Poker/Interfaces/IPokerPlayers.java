package Server.Poker.Interfaces;

import Server.Poker.Models.PlayerModel;

public interface IPokerPlayers
{
    void markPlayerAsDisconnected(PlayerModel player);
}
