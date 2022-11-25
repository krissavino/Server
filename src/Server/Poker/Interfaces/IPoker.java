package Server.Poker.Interfaces;

import Server.Client;
import Server.Poker.Enums.MoveType;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.Models.PlayerModel;

import java.util.Map;

public interface IPoker
{
    ClientTableModel getTable();

    Map<Client, PlayerModel> getPlayers();

    PlayerModel getPlayer(Client client);

    void move(PlayerModel player, MoveType moveType, int moveBet);

    boolean authorizePlayer(Client client, PlayerModel newPlayer);

    void removePlayer(Client client);

    void startGameTimer(int delay);
    void setPlayerBet(Client client, int bet);

    boolean setWinner();
}
