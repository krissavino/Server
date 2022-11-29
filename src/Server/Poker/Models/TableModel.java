package Server.Poker.Models;

import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameStage;
import Server.Poker.Enums.LobbyState;

import java.util.*;

public final class TableModel
{
    public int Pot = 0;
    public int Bet = 0;
    public int PlayerIndexTurn = 0;
    public int TimerStartTime = 0;
    public boolean CanBigBlindBet = true;
    public PlayerModel Winner = null;
    public GameStage GameStage = Server.Poker.Enums.GameStage.Preflop;
    public LobbyState LobbyState = Server.Poker.Enums.LobbyState.Waiting;
    public ArrayList<CardModel> CardsOnTable = new ArrayList();
    public ArrayList<CardModel> CardsForDrop = new ArrayList();
    public ArrayList<PlayerModel> PlayersInQueue = new ArrayList();
    public Map<Integer, PlayerModel> PlacePlayerMap = new HashMap(5);
}
