package Server.Poker;

import Server.Client;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.RegisterPokerPlayer;
import Server.Commands.UpdateInfo;
import Server.Poker.Cards.Cards;
import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameStage;
import Server.Poker.Enums.GameState;
import Server.Poker.Enums.MoveType;
import Server.Poker.Enums.Role;
import Server.Poker.Interfaces.IPoker;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.Models.PlayerModel;
import Server.Poker.Models.PokerModel;
import Server.ServerContainer;

import java.util.*;

public final class Poker
{
    private PokerModel Poker = new PokerModel();
    private Timer GameTimer = new Timer();

    public boolean startGame()
    {
        var clientsCount = ServerContainer.getServer().getClients().size();

        if(clientsCount < 2) {
            Poker.Table.State = GameState.Waiting;
            new UpdateInfo().send();
            return false;
        }
        acceptPlayersFromQueue();
        resetBets();
        setPlaces();
        setRoles();
        Poker.Table.Winner = null;
        Poker.Table.CardsForDrop = Cards.generateCards();
        handOutCardsToPlayers(2);
        placeCardsOnTable();
        Poker.Table.Stage = GameStage.Preflop;
        Poker.Table.State = GameState.Started;

        Poker.Table.CanBigBlindBet = true;

        restartGameTimer(6000);
        new UpdateInfo().send();

        return true;
    }

    public void acceptPlayersFromQueue() {
        int counter = 5 - Poker.Table.Players.size();
        for(var key : Poker.Table.PlayersInQueue.keySet()) {
            if(counter == 0) break;
            Poker.Table.Players.put(key, Poker.Table.PlayersInQueue.get(key));

            ICommand registerPokerPlayer = new RegisterPokerPlayer();
            registerPokerPlayer.setReceiver(key);
            registerPokerPlayer.setObjectToSend(Poker.Table.PlayersInQueue.get(key));
            registerPokerPlayer.send();

            Poker.Table.PlayersInQueue.remove(key);
            counter--;
        }
    }

    public void setPlaces() {
        Poker.Table.Places.clear();
        var places = Poker.Table.Places;
        var players = Poker.Table.Players;
        for(PlayerModel player : players.values()) {
            places.put(player.Place, player);
        }
        for(int j = 0; j < 4; j++)
            for(int i = 0; i < places.size(); i++) {
                if(places.get(i) == null) {
                    if(places.get(i+1) != null) {
                        places.put(i, places.get(i + 1));
                        places.remove(i+1);
                    }
                    if(places.get(i) != null)
                        places.get(i).Place = i;
                }
            }
        for(PlayerModel place : places.values()) {
            for(PlayerModel player : players.values()) {
                if(place != null)
                    if(place.NickName.equals(player.NickName)) {
                        player.Place = place.Place;
                    }
            }
        }
    }

    public void setRoles() {
        for(PlayerModel player : Poker.Table.Players.values()) {
            player.Role = Role.Player;
            if(player.Place == (Poker.Table.PlayerIndexTurn + 2) % Poker.Table.Players.size()) {
                player.Role = Role.BigBlind;
            }
            if(player.Place == (Poker.Table.PlayerIndexTurn + 1) % Poker.Table.Players.size()) {
                player.Role = Role.SmallBlind;
            }
            if(player.Place == Poker.Table.PlayerIndexTurn) {
                player.Role = Role.Dealer;
            }
            Poker.Table.Places.put(player.Place, player);
        }

    }

    public void setRoles2()
    {
        var dealer = Poker.Table.SpecialPlayers.get(Role.Dealer);
        if(dealer == null)
        {
            int counter = Role.Dealer.ordinal();
            for(PlayerModel player : Poker.Table.Players.values()) {
                player.Place = counter;
                if(counter >= Role.Player.ordinal()) {
                    counter = Role.Player.ordinal();
                } else {
                    Poker.Table.SpecialPlayers.put(Role.values()[counter], player);
                }
                player.Role = Role.values()[counter];
                Poker.Table.Places.put(counter, player);
                counter++;
            }
            return;
        }
        //if(Poker.Table.Players.size() > Poker.Table.Places.size())

        var dealerIndex = Poker.Table.SpecialPlayers.get(Role.Dealer).Place;
        var smallBlindIndex = Poker.Table.SpecialPlayers.get(Role.SmallBlind).Place;
        for(PlayerModel player : Poker.Table.Players.values()) {
            Poker.Table.Places.put(player.Place, player);
        }
        if(Poker.Table.Players.size() > 2) {
            int bigBlindIndex = (smallBlindIndex + 1) % Poker.Table.Players.size();
            var nexBigBlindIndex = (bigBlindIndex + 1) % Poker.Table.Players.size();
            Poker.Table.Places.get(dealerIndex).Role = Role.Player;
            Poker.Table.Places.get(smallBlindIndex).Role = Role.Dealer;
            Poker.Table.Places.get(bigBlindIndex).Role = Role.SmallBlind;
            Poker.Table.Places.get(nexBigBlindIndex).Role = Role.BigBlind;
        } else {
            Poker.Table.Places.get(smallBlindIndex).Role = Role.Dealer;
            Poker.Table.Places.get(dealerIndex).Role = Role.SmallBlind;
        }
        for(PlayerModel place : Poker.Table.Places.values()) {
            Poker.Table.SpecialPlayers.put(place.Role, place);
        }

        Poker.Table.PlayerIndexTurn = Poker.Table.SpecialPlayers.get(Role.Dealer).Place;
    }

    public void setPlayerBet(Client client, int bet) {
        var player = getPlayer(client);
        player.Bet = bet;
        new UpdateInfo().send();
    }

    public void removePlayer(Client client)
    {
        Poker.Table.Places.remove(Poker.Table.Players.get(client).Place);
        Poker.Table.Players.remove(client);
    }

    public boolean checkForEndGame() {
        if(Poker.Table.State == GameState.Ended) return true;
        if(isEndGame() == true) {
            endGame();
            return true;
        }
        return false;
    }

    public void move(PlayerModel player, MoveType moveType, int moveBet)
    {
        if(moveType == MoveType.Bet)
            moveBet(player, moveBet);

        if(moveType == MoveType.Fold)
            moveFold(player);

        if(moveType == MoveType.Check)
            moveCheck(player);

        if(moveType == MoveType.Raise)
            moveRaise(player, moveBet);

        if(moveType == MoveType.Call)
            moveCall(player);

        player.LastMove = moveType;
        Poker.Table.PlayerIndexTurn = (Poker.Table.PlayerIndexTurn + 1) % Poker.Table.Players.size();
        if(checkForEndGame() == true)
            return;

        if(isNextStage() == true) {
            changeGameStage();
        }
        new UpdateInfo().send();
        restartGameTimer(6000);
    }

    private boolean isEndGame()
    {
        var isAllPlayersEndMoves = isAllPlayersCalled() || isAllPlayersChecked();

        if(Poker.Table.Stage == GameStage.River &&
                isAllPlayersEndMoves)
            return true;
        if(checkForFoldWinner() != null)
            return true;

        return false;
    }

    private void endGame()
    {
        setWinner();
        Poker.Table.State = GameState.Ended;
        restartGameTimer(4000);
        new UpdateInfo().send();
    }

    private void moveBet(PlayerModel player, int moveBet)
    {
        player.Chips -= moveBet;

        if(player.Bet == -1)
            player.Bet = moveBet;
        else
            player.Bet += moveBet;

        Poker.Table.Pot += moveBet;
        Poker.Table.Bet = moveBet;
        player.LastMove = MoveType.Bet;
    }

    private void moveFold(PlayerModel player)
    {
        player.Bet = -1;
        player.LastMove = MoveType.Fold;
    }

    public void moveCheck(PlayerModel player)
    {
        if(player.Bet == -1)
            player.Bet = 0;

        player.LastMove = MoveType.Check;
    }

    private void moveCall(PlayerModel player)
    {
        if(player.Bet > 0)
        {
            player.Chips -= (Poker.Table.Bet - player.Bet);
            Poker.Table.Pot += (Poker.Table.Bet - player.Bet);
        }
        else
        {
            player.Chips -= Poker.Table.Bet;
            Poker.Table.Pot += Poker.Table.Bet;
        }

        player.Bet = Poker.Table.Bet;
        player.LastMove = MoveType.Call;
    }

    private void moveRaise(PlayerModel player, int moveBet)
    {
        player.Chips -= moveBet;

        if(player.Bet == -1)
            player.Bet = moveBet;
        else
            player.Bet += moveBet;

        if(Poker.Table.Bet == 0)
            Poker.Table.Bet = moveBet;
        else
            Poker.Table.Bet += moveBet;

        Poker.Table.Pot += moveBet;
        player.LastMove = MoveType.Raise;
    }

    public boolean isNextStage()
    {
        boolean nextGameStage = isAllPlayersChecked();

        if (nextGameStage == true)
            return true;

        nextGameStage = isAllPlayersCalled();

        if (nextGameStage == true)
            return true;

        var currentPlayer = Poker.Table.Places.get(Poker.Table.PlayerIndexTurn);

        if (currentPlayer.Role == Role.BigBlind &&
                Poker.Table.Stage == GameStage.Preflop &&
                currentPlayer.LastMove == MoveType.Check)
        {
            Poker.Table.CanBigBlindBet = false;
            return true;
        }

        return false;
    }

    private void changeGameStage()
    {
        Poker.Table.Stage = Poker.Table.Stage.next();

        int howMuchCardsToOpen = Poker.Table.Stage.ordinal() + 2;

        for (int counter = 0; counter < howMuchCardsToOpen; counter++)
            Poker.Table.CardsOnTable.get(counter).setOpened(true);

        var players = Poker.Table.Places.values();

        for (var player : players)
        {
            player.Bet = -1;
            player.LastMove = MoveType.None;
        }


        Poker.Table.Bet = 0;
    }

    private boolean isAllPlayersChecked()
    {
        return false;
        /*var players = Poker.Table.Places.values();
        var nextGameStage = true;

        for (var player : players)
        { // IF SOMEONE CALLED AFTER BIG BLIND BET
            if (player.Bet == Poker.Table.Bet) ;
            else nextGameStage = false;
        }

        return nextGameStage;*/
    }

    private boolean isAllPlayersCalled()
    {
        var players = Poker.Table.Places.values();
        var nextGameStage = true;

        // IF SOMEONE CALLED AFTER BIG BLIND BET
        for (var player : players)
        {
            if (player.LastMove == MoveType.Call) ;
            else nextGameStage = false;
        }

        return nextGameStage;
    }

    public PlayerModel checkForFoldWinner()
    {
        int foldCounter = 0;
        var players = Poker.Table.Places.values();
        var winner = new PlayerModel();
        for (var player : players) {
            if (player.LastMove == MoveType.Fold) {
                foldCounter++;
            } else {
                winner = player;
            }
        }

        if(foldCounter == players.size()-1)
        {
            return winner;
        }

        return null;
    }

    public boolean setWinner()
    {
        var players = Poker.Table.Places.values();

        if(players.size() == 0)
            return false;

        if(players.size() == 1)
        {
            Poker.Table.Winner = (PlayerModel) players.toArray()[0];
            return true;
        }

        var winner = checkForFoldWinner();
        if(winner != null) {
            Poker.Table.Winner = winner;
            return true;
        }

        сountPlayersScore();
        var winners = getWinners();

        if(winners.size() == 0)
            return false;

        //if(winners.size() > 1)
        //    DoDoubleCheck();
        //else
        Poker.Table.Winner = winners.get(0);
        return true;
    }

    public PlayerModel getPlayer(Client client)
    {
        var player = Poker.Table.Players.get(client);

        return player;
    }

    private void сountPlayersScore()
    {
        var players = Poker.Table.Players.values();

        for(var player : players)
            сountPlayerScore(player);
    }

    private void сountPlayerScore(PlayerModel player)
    {
        player.Score = 0;

        //high card
        if(player.Cards.get(0).Name.ordinal() >= player.Cards.get(1).Name.ordinal())
            player.Score = player.Cards.get(0).Name.ordinal();
        else
            player.Score = player.Cards.get(1).Name.ordinal();
        //high card
            /*//pairs
            int pairs = 0;
            for(int i = 0; i < 5; i++)
            {
                if(p.hand.get(0).GetName().toString().equals(cardsOnTable.get(i).GetName().toString()))
                    pairs++;
                if(p.hand.get(1).GetName().toString().equals(cardsOnTable.get(i).GetName().toString()))
                    pairs++;
            }
            if(p.hand.get(0).GetName().toString().equals(p.hand.get(1).GetName().toString())) pairs++;
            if(pairs == 1)
                p.setScore(15);
            if(pairs > 1)
                p.setScore(16);
            //pairs*/
    }

    private ArrayList<PlayerModel> getWinners()
    {
        var firstWinner = findFirstWinner();
        var winners = findSameWinners(firstWinner);

        winners.add(firstWinner);

        return winners;
    }

    private PlayerModel findFirstWinner()
    {
        var players = Poker.Table.Players.values();
        PlayerModel winner = null;

        for(PlayerModel player : players)
        {
            if(winner == null)
                winner = player;

            if(player.Score > winner.Score)
                winner = player;
        }

        return winner;
    }

    private ArrayList<PlayerModel> findSameWinners(PlayerModel winner)
    {
        var players = Poker.Table.Players.values();
        var winners = new ArrayList<PlayerModel>();

        for(var player : players)
        {
            if(player.Score == winner.Score)
                winners.add(player);
        }

        return winners;
    }

    public boolean authorizePlayer(Client client, PlayerModel newPlayer)
    {
        if(isPlayerExist(newPlayer) == true)
        {
            var server = ServerContainer.getServer();
            server.disconnectClient(client);
            return false;
        }

        PlayerModel player = new PlayerModel();
        player.NickName = newPlayer.NickName;
        player.Role = newPlayer.Role;
        player.Chips = newPlayer.Chips;
        player.Place = getFreePlaceId();
        if(Poker.Table.State != GameState.Waiting)
        {
            Poker.Table.PlayersInQueue.put(client, player);
            System.out.println(player + " added to queue");
        } else
        {
            System.out.println(player + " added to players");
            Poker.Table.Players.put(client,player);
        }

        ICommand registerPokerPlayer = new RegisterPokerPlayer();
        registerPokerPlayer.setReceiver(client);
        registerPokerPlayer.setObjectToSend(player);
        registerPokerPlayer.send();

        var playersCount = Poker.Table.Players.size();

        if(playersCount > 1 && Poker.Table.State == GameState.Waiting)
            startGame();
        else
            new UpdateInfo().send();

        return true;
    }

    private int getFreePlaceId()
    {
        int maxPlace = -1;
        for(var player : Poker.Table.Players.values()) {
            if(player.Place > maxPlace)
                maxPlace = player.Place;
        }
        return (maxPlace + 1) % 5;
    }

    private boolean isPlayerExist(PlayerModel newPlayer)
    {
        var tablePlayers = Poker.Table.Players.values();

        for (var player : tablePlayers)
        {
            if(player.NickName.equals(newPlayer.NickName))
                return true;
        }

        return false;
    }

    private void handOutCardsToPlayers(int timesToGiveCard)
    {
        if(timesToGiveCard < 2)
            timesToGiveCard = 2;

        for(var player : Poker.Table.Players.values())
        {
            player.Cards.clear();
            for(int counter = 0; counter < timesToGiveCard; counter++)
            {
                if(Poker.Table.CardsForDrop.size() == 0)
                    return;
                givePlayerCard(player, Poker.Table.CardsForDrop.get(0));
                Poker.Table.CardsForDrop.remove(0);
            }
        }
    }

    private void placeCardsOnTable()
    {
        Poker.Table.CardsOnTable.clear();
        for(int counter = 0; counter < 5; counter++)
        {
            if(Poker.Table.CardsForDrop.size() == 0)
                return;
            Poker.Table.CardsOnTable.add(Poker.Table.CardsForDrop.get(0));
            Poker.Table.CardsForDrop.remove(0);
        }
    }

    private boolean givePlayerCard(PlayerModel player, CardModel cardModel)
    {
        if(player.Cards.size() < 6)
        {
            player.Cards.add(cardModel);
            return true;
        }
        return false;
    }

    public ClientTableModel getTable()
    {
        ClientTableModel clientTable = new ClientTableModel();
        clientTable.Pot             = Poker.Table.Pot            ;
        clientTable.Bet             = Poker.Table.Bet            ;
        clientTable.PlayerIndexTurn = Poker.Table.PlayerIndexTurn;
        clientTable.TimerTime       = Poker.Table.TimerTime      ;
        clientTable.Winner          = Poker.Table.Winner         ;
        clientTable.Stage           = Poker.Table.Stage          ;
        clientTable.State           = Poker.Table.State          ;
        clientTable.CardsOnTable    = Poker.Table.CardsOnTable   ;
        clientTable.Players         = Poker.Table.Places         ;

        return clientTable;
    }

    public Map<Client, PlayerModel> getPlayers()
    {
        return Poker.Table.Players;
    }

    private void resetBets()
    {
        for(var player : Poker.Table.Players.values()) {
            player.Bet = -1;
            player.LastMove = MoveType.None;
        }
        for(var player : Poker.Table.Places.values()) {
            player.Bet = -1;
            player.LastMove = MoveType.None;
        }
    }

    public void restartGameTimer(int delay)
    {
        GameTimer.cancel();
        GameTimer = new Timer();
        System.out.println("Timer started: " + delay + " (delay) " + Poker.Table.State + " " + Poker.Table.Winner);
        GameTimer.schedule(new TimerTask()
        {
            @Override
            public void run() {TimerElapsed();}
        }, delay);
        Poker.Table.TimerTime = delay;
    }

    private void TimerElapsed()
    {
        System.out.println("Timer elapsed: ");
        if(Poker.Table.Winner != null) {
            startGame();
            return;
        }
        var player = Poker.Table.Places.get(Poker.Table.PlayerIndexTurn);
        System.out.println("player " + player + " are going to move.");

        if(player != null)
            move(player, MoveType.Call, 0);
    }
}
