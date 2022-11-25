package Server.Poker.Models;

import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.MoveType;
import Server.Poker.Enums.Role;

import java.util.ArrayList;
import java.util.List;

public class PlayerModel
{
    public String NickName = "Unknown";
    public MoveType LastMove = MoveType.None;
    public Role Role = Server.Poker.Enums.Role.Player;
    public int Chips = 0;
    public int Place = 0;
    public int Bet = -1;
    public int Score = 0;
    public List<CardModel> Cards = new ArrayList<>();
}
