package Server.Poker.Cards.Models;

import Server.Poker.Cards.Enums.CardColor;
import Server.Poker.Cards.Enums.CardName;
import Server.Poker.Cards.Interface.ICard;

public class CardModel implements ICard
{
    public CardColor Color = CardColor.values()[0];
    public CardName Name = CardName.values()[0];

    public boolean Opened = false;
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
        Opened = opened;
    }

    public boolean isOpened() {
        return Opened;
    }
    @Override
    public boolean equals(Object obj) {
        super.equals(obj);
        if(this.Color == ((CardModel)obj).Color && this.Name == ((CardModel)obj).Name)
            return true;
        else
            return false;
    }
}
