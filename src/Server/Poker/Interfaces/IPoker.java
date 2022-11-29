package Server.Poker.Interfaces;

import Server.ClientSocket;
import Server.Poker.Enums.MoveType;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.Models.PlayerModel;

import java.util.Map;

public interface IPoker
{
    ClientTableModel getTable();

    Map<ClientSocket, PlayerModel> getPlayers();

    PlayerModel getPlayer(ClientSocket clientSocket);

    void move(PlayerModel player, MoveType moveType, int moveBet);

    boolean authorizePlayer(ClientSocket clientSocket, PlayerModel newPlayer);

    void removePlayer(ClientSocket clientSocket);

    void startGameTimer(int delay);
    void setPlayerBet(ClientSocket clientSocket, int bet);

    boolean setWinner();
}
