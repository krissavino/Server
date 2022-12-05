package Server.Poker.Models;

import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameState;
import Server.Poker.Enums.LobbyState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ClientTableModel
{
        public int Pot = 0;
        public int Bet = 0;
        public int PlayerIndexTurn = 0;
        public int TimerStartTime = 0;
        public int PlayersInQueue = 0;
        public PlayerModel Winner = null;
        public GameState GameState = Server.Poker.Enums.GameState.Preflop;
        public LobbyState LobbyState = Server.Poker.Enums.LobbyState.Waiting;
        public ArrayList<CardModel> CardsOnTable = new ArrayList();
        public Map<Integer, PlayerModel> PlacePlayerMap = new HashMap(5);
}
