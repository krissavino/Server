package Server.Poker.Cards.Models;

import Server.Poker.Cards.Enums.CardColor;
import Server.Poker.Cards.Enums.CardName;
import Server.Poker.Cards.Interface.ICard;

public class CardModel implements ICard
{
    public CardColor Color = CardColor.values()[0];
    public CardName Name = CardName.values()[0];

    public boolean IsOpened = false;
    public CardModel(CardColor color, CardName name)
    {
        this.Color = color;
        this.Name = name;
    }
    public CardName GetName() {
        return Name;
    }

    public CardColor GetColor() {
        return Color;
    }

    public void setOpened(boolean opened) {
        IsOpened = opened;
    }

    public boolean isOpened() {
        return IsOpened;
    }
}
