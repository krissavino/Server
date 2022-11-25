package Server.Poker;

import Server.Poker.Interfaces.IPoker;

public class PokerContainer
{
    private static Poker Poker;

    public static Poker getPoker()
    {
        if(Poker == null)
            Poker = new Poker();

        return Poker;
    }
}
