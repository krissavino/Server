package Server.Poker;

import Server.Poker.Interfaces.IPokerMove;
import Server.Poker.Interfaces.IPokerPlayers;

public class PokerContainer
{
    private static Poker Poker;

    public static Poker getPoker()
    {
        if(Poker == null)
            Poker = new Poker();

        return Poker;
    }

    public static IPokerMove getPokerMove()
    {
        var poker = getPoker();

        return (IPokerMove)poker;
    }

    public static IPokerPlayers getPokerPlayers()
    {
        var poker = getPoker();

        return (IPokerPlayers)poker;
    }
}
