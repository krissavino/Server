package Server.Poker.Models;

import Server.Poker.Enums.GameState;

public final class PokerModel
{
    public boolean CanBigBlindBet = true;
    public GameState LobbyState = GameState.Waiting;
    public TableModel Table = new TableModel();
}
