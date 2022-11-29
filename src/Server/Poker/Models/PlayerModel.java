package Server.Poker.Models;

import Server.ClientSocket;
import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.Enums.Role;

import java.util.ArrayList;
import java.util.List;

public class PlayerModel
{
    public transient ClientSocket Socket = null;

    public String NickName = "Unknown";
    public MoveType LastMove = MoveType.None;
    public Role Role = Server.Poker.Enums.Role.Player;
    public int Chips = 0;
    public int Place = 0;
    public int Bet = -1;
    public int Score = 0;

    public boolean IsDisconnected = false;

    public boolean IsInQueue = true;
    public List<CardModel> Cards = new ArrayList<>();
}
