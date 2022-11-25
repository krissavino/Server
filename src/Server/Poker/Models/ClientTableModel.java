package Server.Poker.Models;

import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameStage;
import Server.Poker.Enums.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ClientTableModel
{
        public int Pot = 0;
        public int Bet = 0;
        public int PlayerIndexTurn = 0;
        public int TimerTime = 0;
        public PlayerModel Winner = null;
        public GameStage Stage = GameStage.Preflop;
        public GameState State = GameState.Waiting;
        public ArrayList<CardModel> CardsOnTable = new ArrayList();
        public Map<Integer, PlayerModel> Players = new HashMap(5);
}
