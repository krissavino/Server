package Server.Poker.Models;

import Server.Client;
import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameStage;
import Server.Poker.Enums.GameState;
import Server.Poker.Enums.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class TableModel
{
    public int Pot = 0;
    public int Bet = 0;
    public int PlayerIndexTurn = 0;
    public int TimerTime = 0;
    public boolean CanBigBlindBet = true;
    public PlayerModel Winner = null;
    public GameStage Stage = GameStage.Preflop;
    public GameState State = GameState.Waiting;
    public ArrayList<CardModel> CardsOnTable = new ArrayList();
    public ArrayList<CardModel> CardsForDrop = new ArrayList();
    public Map<Client,PlayerModel> PlayersInQueue = new HashMap();
    public Map<Client,PlayerModel> Players = new HashMap();
    public Map<Integer, PlayerModel> Places = new HashMap(5);
    public Map<Role, PlayerModel> SpecialPlayers = new HashMap(3);
}
