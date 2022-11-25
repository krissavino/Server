package Server.Poker.Cards.Interface;

import Server.Poker.Cards.Enums.CardColor;
import Server.Poker.Cards.Enums.CardName;

public interface ICard
{
    CardName GetName();

    CardColor GetColor();

    void setOpened(boolean opened);

    boolean isOpened();
}
