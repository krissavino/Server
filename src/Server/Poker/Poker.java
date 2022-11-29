package Server.Poker;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.RegisterPokerPlayer;
import Server.Commands.UpdateInfo;
import Server.Poker.Cards.Cards;
import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameStage;
import Server.Poker.Enums.LobbyState;
import Server.Poker.Enums.MoveType;
import Server.Poker.Enums.Role;
import Server.Poker.Interfaces.IQueue;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.Models.PlayerModel;
import Server.Poker.Models.PokerModel;
import Server.Poker.Models.TableModel;
import Server.ServerContainer;

import java.util.*;

public final class Poker implements IQueue
{
    private PokerModel Poker = new PokerModel();
    private Timer GameTimer = new Timer();
    private Timer WaitingTimer = new Timer();
    private int MinPlayersCountNeededToStartGame = 2;

    public boolean startGame()
    {
        Poker.Table.Winner = null;
        Poker.Table.CardsForDrop = Cards.generateCards();

        PlacePlayersFromQueue();
        setRoles();
        resetBets();

        handOutCardsToPlayers(2);
        placeCardsOnTable();
        Poker.Table.GameStage = GameStage.Preflop;
        Poker.Table.LobbyState = LobbyState.Started;
        setFirstTurn(Role.SmallBlind);

        restartGameTimer(10000);
        new UpdateInfo().send();

        smallBlindFirstBet();
        bigBlindFirstBet();

        return true;
    }

    private void setFirstTurn(Role role)
    {
        var player = getPlayer(Role.SmallBlind);

        if(player == null)
            return;

        Poker.Table.PlayerIndexTurn = player.Place;
    }

    public void PlacePlayersFromQueue()
    {
        var playersFromQueue = Poker.Table.PlayersInQueue;
        var placePlayerMap = Poker.Table.PlacePlayerMap.values();

        for (var player : playersFromQueue)
        {
            var freePlaceId =  getFreePlaceId();

            if(freePlaceId == -1)
            {
                System.out.println("No free places");
                return;
            }

            player.Place = freePlaceId;
            player.IsInQueue = false;
            Poker.Table.PlacePlayerMap.put(freePlaceId, player);
            System.out.println(player.NickName + " took place with number " + freePlaceId);
        }

        for (var player : placePlayerMap)
        {
            var isPlayerInQueue = playersFromQueue.contains(player);

            if(isPlayerInQueue == false)
                continue;

            System.out.println(player.NickName + " was removed from queue");
            Poker.Table.PlayersInQueue.remove(player);
        }
    }

    private void resetBets()
    {
        var players = Poker.Table.PlacePlayerMap.values();

        for(var player : players)
        {
            player.Bet = -1;
            player.LastMove = MoveType.None;
        }

        for(var player : players)
        {
            player.Bet = -1;
            player.LastMove = MoveType.None;
        }
    }

    public void setRoles()
    {
        var playersByTable = Poker.Table.PlacePlayerMap.values();

        for(int counter = 0; counter < playersByTable.size(); counter++)
        {
            var playerFromPlace = Poker.Table.PlacePlayerMap.get(counter);
            var roleId = (counter + Poker.GamesFinished) % playersByTable.size();

            if(roleId > 3)
                roleId = 3;

            playerFromPlace.Role = Role.values()[roleId];
        }
    }

    public void removePlayer(ClientSocket clientSocket)
    {
        var isPlayerRemoved = removePlayerFromQueue(clientSocket);

        if(isPlayerRemoved == true)
            return;

        removePlayerFromPlacePlayerMap(clientSocket);

        if(Poker.Table.LobbyState == LobbyState.Ended)
            return;

        if(Poker.Table.PlacePlayerMap.size() == 0)
            endGame();
    }

    private boolean removePlayerFromQueue(ClientSocket clientSocket)
    {
        var playersFromQueue = Poker.Table.PlayersInQueue;
        PlayerModel playerToRemove = null;

        for(var player : playersFromQueue)
        {
            if (player.Socket == clientSocket)
            {
                playerToRemove = player;
                break;
            }
        }

        if(playerToRemove == null)
            return false;

        System.out.println("Player <" + playerToRemove.NickName +">  was removed from Poker queue");
        Poker.Table.PlayersInQueue.remove(playerToRemove);
        return true;
    }

    private boolean removePlayerFromPlacePlayerMap(ClientSocket clientSocket)
    {
        var playersFromPlaces = Poker.Table.PlacePlayerMap.entrySet();
        var playerToRemovePlaceId = 0;
        PlayerModel playerToRemove = null;

        for(var placePlayer : playersFromPlaces)
        {
            var playerSoket = placePlayer.getValue().Socket;

            if (playerSoket == clientSocket)
            {
                playerToRemovePlaceId = placePlayer.getKey();
                playerToRemove = placePlayer.getValue();
                break;
            }
        }

        if(playerToRemove == null)
            return false;


            playerToRemove.IsDisconnected = true;
        return true;
    }

    public void removeDisconnectedPlayers()
    {
        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.entrySet();
        var disconnectedPlayersPlaces = new ArrayList<Integer>();

        for(var placePlayer : playersFromPlacePlayerMap)
            if(placePlayer.getValue().IsDisconnected == true)
            {
                disconnectedPlayersPlaces.add(placePlayer.getKey());
                System.out.println("Player <" + placePlayer.getValue().NickName +">  was removed from Poker game");
            }

        for(var placeId : disconnectedPlayersPlaces)
            Poker.Table.PlacePlayerMap.remove(placeId);
    }

    public boolean checkForEndGame()
    {
        if(Poker.Table.LobbyState == LobbyState.Ended) return true;

        if(isEndGame() == true)
        {
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
        System.out.println("        " + "Player <" + player.NickName + "> moved: " + moveType);

        if(isNextStage() == true)
        {
            changeGameStage();
            System.out.println("GameStage changed: " + Poker.Table.GameStage);
        }
        else if(checkForEndGame() == true)
            return;

        Poker.Table.PlayerIndexTurn = (Poker.Table.PlayerIndexTurn + 1) % Poker.Table.PlacePlayerMap.size();

        restartGameTimer(10000);
        new UpdateInfo().send();
    }

    public boolean isNextStage()
    {
        if(Poker.Table.GameStage == GameStage.River)
            return false;

        boolean nextGameStage = isAllPlayersChecked();

        if (nextGameStage == true)
            return true;

        nextGameStage = isAllPlayersCalled();

        if (nextGameStage == true)
            return true;

        return false;
    }

    private void bigBlindFirstBet()
    {
        var playerBigBlind = getPlayer(Role.BigBlind);

        if(playerBigBlind == null)
            return;

        var playerSmallBlind = getPlayer(Role.SmallBlind);

        move(playerBigBlind, MoveType.Bet, playerSmallBlind.Bet*2);
    }

    private void smallBlindFirstBet()
    {
        var playerSmallBlind = getPlayer(Role.SmallBlind);

        move(playerSmallBlind, MoveType.Bet, 5);
    }

    private void changeGameStage()
    {
        Poker.Table.GameStage = Poker.Table.GameStage.next();

        int howMuchCardsToOpen = Poker.Table.GameStage.ordinal() + 2;

        for (int counter = 0; counter < howMuchCardsToOpen; counter++)
            Poker.Table.CardsOnTable.get(counter).setOpened(true);

        var players = Poker.Table.PlacePlayerMap.values();

        for (var player : players)
        {
            player.Bet = -1;
            player.LastMove = MoveType.None;
        }

        Poker.Table.Bet = 0;
    }

    private boolean isAllPlayersChecked()
    {
        var players = Poker.Table.PlacePlayerMap.values();
        var nextGameStage = true;

        for (var player : players)
        {
            // IF SOMEONE CALLED AFTER BIG BLIND BET
            if (player.Bet == Poker.Table.Bet) ;
            else nextGameStage = false;
        }

        return nextGameStage;
    }

    private boolean isAllPlayersCalled()
    {
        var players = Poker.Table.PlacePlayerMap.values();
        var nextGameStage = true;

        // IF SOMEONE CALLED AFTER BIG BLIND BET
        for (var player : players)
        {
            if (player.LastMove == MoveType.Call) ;
            else nextGameStage = false;
        }

        return nextGameStage;
    }


    private boolean isEndGame()
    {
        var isAllPlayersEndMoves = isAllPlayersCalled() || isAllPlayersChecked();

        if(Poker.Table.GameStage == GameStage.River && isAllPlayersEndMoves)
            return true;

        if(checkForFoldWinner() != null)
            return true;

        return false;
    }

    private void endGame()
    {
        GameTimer.cancel();

        Poker.GamesFinished += 1;
        Poker.Table.LobbyState = LobbyState.Ended;
        System.out.println("Poker game ended");

        removeDisconnectedPlayers();
        setWinner();
        restartGameTimer(10000);

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
    }

    private void moveFold(PlayerModel player)
    {
        player.Bet = -1;
    }

    public void moveCheck(PlayerModel player)
    {
        if(player.Bet == -1)
            player.Bet = 0;
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
    }

    public PlayerModel checkForFoldWinner()
    {
        int foldCounter = 0;
        var players = Poker.Table.PlacePlayerMap.values();
        var winner = new PlayerModel();

        for (var player : players)
        {
            if (player.LastMove == MoveType.Fold)
                foldCounter++;
            else
                winner = player;
        }

        if(foldCounter == players.size()-1)
        {
            System.out.println("FOLD WINNER: " + winner.NickName);
            return winner;
        }

        return null;
    }

    public boolean setWinner()
    {
        var players = Poker.Table.PlacePlayerMap.values();
        PlayerModel winner = null;

        if(players.size() == 0)
            return false;

        if(players.size() == 1)
        {
            winner = (PlayerModel) players.toArray()[0];
            Poker.Table.Winner = winner;
            return true;
        }

        winner = checkForFoldWinner();
        if(winner != null)
        {
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

    public PlayerModel getPlayer(Role playerRole)
    {
        var placePlayerMap = Poker.Table.PlacePlayerMap.values();

        for (var player : placePlayerMap)
        {

            if(player.Role == playerRole)
                return player;
        }

        return null;
    }

    public PlayerModel getPlayer(String nickName)
    {
        var player = getPlayerFromQueue(nickName);

        if(player == null)
            return getPlayerFromPlacePlayerMap(nickName);

        return player;
    }

    private PlayerModel getPlayerFromQueue(String nickName)
    {
        var playersFromQueue = Poker.Table.PlayersInQueue;

        for (var player : playersFromQueue)
            if(player.NickName == nickName)
                return player;

        return null;
    }

    private PlayerModel getPlayerFromPlacePlayerMap(String nickName)
    {
        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.values();

        for (var player : playersFromPlacePlayerMap)
            if(player.NickName == nickName)
                return player;

        return null;
    }

    public PlayerModel getPlayer(ClientSocket clientSocket)
    {
        var player = getPlayerFromQueue(clientSocket);

        if(player == null)
            return getPlayerFromPlacePlayerMap(clientSocket);

        return player;
    }

    private PlayerModel getPlayerFromQueue(ClientSocket clientSocket)
    {
        var playersFromQueue = Poker.Table.PlayersInQueue;

        for (var player : playersFromQueue)
            if(player.Socket == clientSocket)
                return player;

        return null;
    }

    private PlayerModel getPlayerFromPlacePlayerMap(ClientSocket clientSocket)
    {
        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.values();

        for (var player : playersFromPlacePlayerMap)
            if(player.Socket == clientSocket)
                return player;

        return null;
    }

    private void сountPlayersScore()
    {
        var players = Poker.Table.PlacePlayerMap.values();

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
        var players = Poker.Table.PlacePlayerMap.values();
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
        var players = Poker.Table.PlacePlayerMap.values();
        var winners = new ArrayList<PlayerModel>();

        for(var player : players)
        {
            if(player.Score == winner.Score)
                winners.add(player);
        }

        return winners;
    }

    public boolean authorizePlayer(ClientSocket clientSocket, PlayerModel unauthorizedPlayer)
    {
        var nickName = unauthorizedPlayer.NickName;

        if(isPlayerExist(clientSocket) == true)
        {
            var server = ServerContainer.getServer();
            server.disconnectClient(clientSocket);
            return false;
        }

        PlayerModel player = new PlayerModel();
        player.Socket = clientSocket;
        player.NickName = unauthorizedPlayer.NickName;
        player.Chips = unauthorizedPlayer.Chips;
        player.IsInQueue = true;

        addPlayerToQueue(player);

        ICommand registerPokerPlayer = new RegisterPokerPlayer();
        registerPokerPlayer.setReceiver(clientSocket);
        registerPokerPlayer.setObjectToSend(player);
        registerPokerPlayer.send();

        playerAuthorized(player.NickName);

        return true;
    }

    private boolean isPlayerExist(ClientSocket clientSocket)
    {
        var player = getPlayer(clientSocket);

        if(player == null)
            return false;

        return true;
    }

    public void addPlayerToQueue(PlayerModel player)
    {
        var nickName = player.NickName;

        Poker.Table.PlayersInQueue.add(player);
        System.out.println(nickName + " added to queue");
    }

    private void playerAuthorized(String nickName)
    {
        System.out.println(nickName + " authorized");

        if(isGameCanBeStarted() == true)
            restartWaitingTimer(6000);

        new UpdateInfo().send();
    }

    private boolean isGameCanBeStarted()
    {
        removeDisconnectedPlayers();

        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.size();
        var playersFromQueueCount = Poker.Table.PlayersInQueue.size();

        var playersCount = playersFromPlacePlayerMap + playersFromQueueCount;

        if(playersCount >= MinPlayersCountNeededToStartGame)
           if(Poker.Table.LobbyState == LobbyState.Waiting || Poker.Table.LobbyState == LobbyState.Ended)
               return true;

        return false;
    }

    private int getFreePlaceId()
    {
        var places = Poker.Table.PlacePlayerMap;
        var freePlaces = 5 - places.size();

        if(freePlaces == 0)
            return -1;

        for (int counter = 0; counter < 5; counter++)
        {
            var playerFromPlace = places.get(counter);

            if(playerFromPlace == null)
                return counter;
        }

        return -1;
    }

    private void handOutCardsToPlayers(int timesToGiveCard)
    {
        var players = Poker.Table.PlacePlayerMap.values();

        if(timesToGiveCard < 2)
            timesToGiveCard = 2;

        for(var player : players)
        {
            player.Cards.clear();

            for(int counter = 0; counter < timesToGiveCard; counter++)
            {
                if(Poker.Table.CardsForDrop.size() == 0)
                    return;

                var card = Poker.Table.CardsForDrop.get(0);
                givePlayerCard(player, card);

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

            var card = Poker.Table.CardsForDrop.get(0);
            Poker.Table.CardsOnTable.add(card);
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

    public TableModel getTable()
    {
        return Poker.Table;
    }

    public ClientTableModel getClientTable()
    {
        ClientTableModel clientTable = new ClientTableModel();
        clientTable.Pot             = Poker.Table.Pot                  ;
        clientTable.Bet             = Poker.Table.Bet                  ;
        clientTable.PlayerIndexTurn = Poker.Table.PlayerIndexTurn      ;
        clientTable.TimerStartTime  = Poker.Table.TimerStartTime;
        clientTable.Winner          = Poker.Table.Winner               ;
        clientTable.Stage           = Poker.Table.GameStage;
        clientTable.State           = Poker.Table.LobbyState;
        clientTable.CardsOnTable    = Poker.Table.CardsOnTable         ;
        clientTable.PlacePlayerMap  = Poker.Table.PlacePlayerMap;
        clientTable.PlayersInQueue  = Poker.Table.PlayersInQueue.size();

        return clientTable;
    }

    public ArrayList<PlayerModel> getPlayers()
    {
        var playersFromQueue = Poker.Table.PlayersInQueue;
        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.values();
        var players = new ArrayList<PlayerModel>();

        for (var player : playersFromQueue)
            players.add(player);

        for (var player : playersFromPlacePlayerMap)
            players.add(player);

        return players;
    }

    public void restartWaitingTimer(int delay)
    {
        WaitingTimer.cancel();
        WaitingTimer = new Timer();

        var text = String.format("WaitingTimer started: %s (delay), LobbyState: %s, GameStage: %s", delay, Poker.Table.LobbyState, Poker.Table.GameStage);
        System.out.println(text);

        GameTimer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                WaitingTimerElapsed();}
        }, delay);
    }

    private void WaitingTimerElapsed()
    {
        startGame();
    }

    public void restartGameTimer(int delay)
    {
        GameTimer.cancel();
        Poker.Table.TimerStartTime = delay;
        GameTimer = new Timer();

        var text = String.format("Timer started: %s (delay), LobbyState: %s, GameStage: %s", delay, Poker.Table.LobbyState, Poker.Table.GameStage);
        System.out.println(text);

        GameTimer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                GameTimerElapsed();}
        }, delay);
    }

    private void GameTimerElapsed()
    {
        System.out.println("\nTimer elapsed");

        if(Poker.Table.LobbyState == LobbyState.Ended)
        {
            if (isGameCanBeStarted() == true)
                startGame();
            else
                System.out.println("Game can't be started, current is less then 2 players");
            return;
        }

        if(Poker.Table.LobbyState == LobbyState.Started)
        {
            var player = Poker.Table.PlacePlayerMap.get(Poker.Table.PlayerIndexTurn);

            if (player == null)
            {
                System.out.println("Player that should make move left from the Poker game");
                move(new PlayerModel(), MoveType.Check, 0);
            }
            else
                move(player, MoveType.Fold, 0);
        }
    }
}
