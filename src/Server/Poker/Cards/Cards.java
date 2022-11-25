package Server.Poker.Cards;

import Server.Poker.Cards.Enums.CardColor;
import Server.Poker.Cards.Enums.CardName;
import Server.Poker.Cards.Models.CardModel;

import java.util.ArrayList;
import java.util.Collections;

public class Cards
{
    public static ArrayList<CardModel> generateCards()
    {
        var cards = new ArrayList<CardModel>();

        for(CardColor cardColor : CardColor.values())
        {
            ArrayList<CardModel> newCardModels = createCards(cardColor);
            cards = summCards(newCardModels, cards);
        }

        shuffleCards(cards);

        return cards;
    }

    private static void shuffleCards(ArrayList<CardModel> cards)
    {
        Collections.shuffle(cards);
    }

    private static ArrayList<CardModel> createCards(CardColor cardColor)
    {
        ArrayList<CardModel> cardModels = new ArrayList();

        for (int counter = 0; counter < CardName.values().length; counter++)
        {
            CardModel cardModel = new CardModel(cardColor,CardName.values()[counter]);
            cardModels.add(cardModel);
        }

        return cardModels;
    }

    private static ArrayList<CardModel> summCards(ArrayList<CardModel> firstArray, ArrayList<CardModel> secondArray)
    {
        ArrayList<CardModel> newArrayOfCardModels = new ArrayList<CardModel>();

        for (CardModel cardModel : firstArray)
        {
            newArrayOfCardModels.add(cardModel);
        }

        for (CardModel cardModel : secondArray)
        {
            newArrayOfCardModels.add(cardModel);
        }

        return newArrayOfCardModels;
    }
}
