package Server.Poker.Interfaces;

import Server.Poker.Enums.MoveType;
import Server.Poker.Models.PlayerModel;

public interface IPokerMove
{
    void move(PlayerModel player, MoveType moveType, int moveBet);

    void moveBet(PlayerModel player, int moveBet);

    void moveFold(PlayerModel player);

    void moveCheck(PlayerModel player);

    void moveCall(PlayerModel player);

    void moveRaise(PlayerModel player, int moveBet);
}
