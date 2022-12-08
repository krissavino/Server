package Server.Poker.Models;

public final class PokerModel
{
    public TableModel Table = new TableModel();
    public int GamesFinished = 0;
    public int MinPlayersCountNeededToStartGame = 2;
    public int WaitingDelay = 5000;
    public int MoveDelay = 20000;
    public int RestartEndGameDelay = 8000;
}
