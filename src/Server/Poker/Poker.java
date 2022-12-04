package Server.Poker;

import Server.ClientSocket;
import Server.Commands.Interfaces.ICommand;
import Server.Commands.RegisterPokerPlayer;
import Server.Commands.UpdateInfo;
import Server.Poker.Cards.Cards;
import Server.Poker.Cards.Models.CardModel;
import Server.Poker.Enums.GameState;
import Server.Poker.Enums.LobbyState;
import Server.Poker.Enums.MoveType;
import Server.Poker.Enums.Role;
import Server.Poker.Interfaces.IQueue;
import Server.Poker.Models.ClientTableModel;
import Server.Poker.Models.PlayerModel;
import Server.Poker.Models.PokerModel;
import Server.ServerContainer;

import java.util.*;

public final class Poker implements IQueue
{
    private PokerModel Poker = new PokerModel();
    private Timer GameTimer = new Timer();
    private Timer WaitingTimer = new Timer();

    public boolean startGame()
    {
        if(Poker.Table.LobbyState == LobbyState.Started)
            return false;

        Poker.Table.Winner = null;
        Poker.Table.Pot = 0;
        Poker.Table.Bet = 0;
        Poker.Table.GameState = GameState.Preflop;
        Poker.Table.LobbyState = LobbyState.Started;
        Poker.Table.CardsForDrop = Cards.generateCards();

        PlacePlayersFromQueue();
        setRoles();
        resetBets();
        handOutCardsToPlayers(2);
        placeCardsOnTable();

        setFirstTurn(Role.SmallBlind);
        smallBlindFirstBet();
        bigBlindFirstBet();
        return true;
    }

    public void move(PlayerModel player, MoveType moveType, int moveBet)
    {
        boolean isPlayerCanMove = ValidatePlayerMove(player);

        if(isPlayerCanMove == false)
            return;

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
        var text = String.format("Игрок <%s> сделал ход: %s",player.NickName, player.LastMove);
        System.out.println(text);

        if(checkForEndGame() == true)
            return;
        else if(isNextStage() == true)
        {
            changeGameStage();
            System.out.println("GameStage изменился: " + Poker.Table.GameState);
        }

        Poker.Table.PlayerIndexTurn = getNextPlayerTurnId();

        restartGameTimer(Poker.MoveDelay);
        new UpdateInfo().sendToClient();
    }


    private void moveFold(PlayerModel player)
    {
        player.Bet = -1;
    }

    public void moveCheck(PlayerModel player)
    {
        if(player.Bet == -1)
            player.Bet = 0;

        if(Poker.Table.Bet > player.Bet)
            moveCall(player);
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

    public boolean checkForEndGame()
    {
        if(Poker.Table.LobbyState == LobbyState.Ended)
            return true;

        if(isEndGame() == true)
        {
            endGame();
            return true;
        }

        return false;
    }

    private int getNextPlayerTurnId()
    {
        var playersCount = Poker.Table.PlacePlayerMap.size();
        var nextTurnId = (Poker.Table.PlayerIndexTurn + 1) % Poker.Table.PlacePlayerMap.size();

        for (int counter = 0; counter < playersCount; counter++)
        {
            var player = Poker.Table.PlacePlayerMap.get(nextTurnId);

            if (player.LastMove != MoveType.Fold)
                break;

            nextTurnId = (nextTurnId + 1) % Poker.Table.PlacePlayerMap.size();
        }

        return nextTurnId;
    }

    private boolean ValidatePlayerMove(PlayerModel player)
    {
        var placeWitchCanMove = Poker.Table.PlayerIndexTurn;
        var playerPlace = player.Place;

        if(Poker.Table.LobbyState == LobbyState.Started)
            if(playerPlace == placeWitchCanMove)
                return true;

        var text = String.format("Игрок <%s> попытался походить, но было отказано сервером", player.NickName);
        System.out.println(text);

        return false;
    }

    public boolean isNextStage()
    {
        if(Poker.Table.GameState == GameState.River)
            return false;

        boolean nextGameStage = isAllPlayersChecked();

        if (nextGameStage == true)
            return true;

        nextGameStage = isAllPlayersCalled();

        if (nextGameStage == true)
            return true;

        return false;
    }

    private void setFirstTurn(Role role)
    {
        var player = getPlayer(role);

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
                System.out.println("Все свободные места заняты, рассаживание прекращено");
                return;
            }

            player.Place = freePlaceId;
            player.IsInQueue = false;
            Poker.Table.PlacePlayerMap.put(freePlaceId, player);

            var text = String.format("Игрок <%s> сел за место под номером: %s", player.NickName, freePlaceId);
            System.out.println(text);
        }

        for (var player : placePlayerMap)
        {
            var isPlayerRemovedFrom = Poker.Table.PlayersInQueue.remove(player);

            if(isPlayerRemovedFrom == false)
                continue;

            var text = String.format("Игрок <%s> был удалён из очереди", player.NickName);
            System.out.println(text);
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

        System.out.println("Игрок <" + playerToRemove.NickName +">  был удалён из очереди");
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
        {
            if (placePlayer.getValue().IsDisconnected == true)
            {
                disconnectedPlayersPlaces.add(placePlayer.getKey());

                var text = String.format("Игрок <%s> отключился и поэтому будет удалён из игры", placePlayer.getValue().NickName);
                System.out.println(text);
            }
        }

        for(var placeId : disconnectedPlayersPlaces)
            Poker.Table.PlacePlayerMap.remove(placeId);
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
        Poker.Table.GameState = Poker.Table.GameState.next();

        int howMuchCardsToOpen = Poker.Table.GameState.ordinal() + 2;

        for (int counter = 0; counter < howMuchCardsToOpen; counter++)
            Poker.Table.CardsOnTable.get(counter).setOpened(true);

        var players = Poker.Table.PlacePlayerMap.values();

        for (var player : players)
        {
            player.Bet = -1;

            if(player.LastMove == MoveType.Fold)
                continue;

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
            if (player.Bet == Poker.Table.Bet || player.LastMove == MoveType.Fold) ;
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
            if (player.LastMove == MoveType.Call ||  player.LastMove == MoveType.Fold) ;
            else nextGameStage = false;
        }

        return nextGameStage;
    }

    private boolean isEndGame()
    {
        var isAllPlayersEndMoves = isAllPlayersCalled() || isAllPlayersChecked();

        if(getFoldWinner() != null)
            return true;

        if(Poker.Table.GameState == GameState.River && isAllPlayersEndMoves)
            return true;

        return false;
    }

    private void endGame()
    {
        GameTimer.cancel();
        Poker.GamesFinished += 1;
        Poker.Table.LobbyState = LobbyState.Ended;

        System.out.println("!!! Игра окончена !!!");

        removeDisconnectedPlayers();
        var winner = getWinner();
        Poker.Table.Winner = winner;

        var winnerText = "";

        if(winner == null)
            winnerText = String.format("Победитель: игрок покинул игру");
        else
        {
            var chips = Poker.Table.Pot;
            winner.Chips += chips;
            winnerText = String.format("Победитель: <%s>, получает выигрышь %s фишек", winner.NickName, chips);
        }

        System.out.println(winnerText);

        restartGameTimer(Poker.RestartEndGameDelay);

        new UpdateInfo().sendToClient();
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

    private PlayerModel getFoldWinner()
    {
        int foldCounter = 0;
        PlayerModel possiblyWinner = null;

        var players = Poker.Table.PlacePlayerMap.values();

        for (var player : players)
            if(player.LastMove == MoveType.Fold)
                foldCounter++;
            else
                possiblyWinner = player;

        if(foldCounter == players.size()-1)
            return possiblyWinner;

        return null;
    }

    public PlayerModel getWinner()
    {
        var players = Poker.Table.PlacePlayerMap.values();
        PlayerModel winner = null;

        if(players.size() == 0)
            return winner;

        if(players.size() == 1)
        {
            winner = (PlayerModel) players.toArray()[0];
            return winner;
        }

        winner = getFoldWinner();

        if(winner != null)
            return winner;

        var winners = getWinnersByScore();
        winner = winners.get(0);
        return winner;
    }

    private ArrayList<PlayerModel> getWinnersByScore()
    {
        countPlayersScore();
        var firstWinner = findFirstWinner();
        var winners = findSameWinners(firstWinner);

        winners.add(firstWinner);

        return winners;
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
            if(player.NickName.equals(nickName) == true)
                return player;

        return null;
    }

    private PlayerModel getPlayerFromPlacePlayerMap(String nickName)
    {
        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.values();

        for (var player : playersFromPlacePlayerMap)
            if(player.NickName.equals(nickName) == true)
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

    private void countPlayersScore()
    {
        var players = Poker.Table.PlacePlayerMap.values();

        for(var player : players)
            if(player.LastMove != MoveType.Fold)
                countPlayerScore(player);
    }

    private void countPlayerScore(PlayerModel player)
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

    public void authorizePlayer(ClientSocket clientSocket, PlayerModel unauthorizedPlayer)
    {
        var nickName = unauthorizedPlayer.NickName;

        if(isPlayerExist(nickName) == true)
        {
            var server = ServerContainer.getServer();
            server.disconnectClient(clientSocket);
            return;
        }

        PlayerModel player = new PlayerModel();
        player.Socket = clientSocket;
        player.NickName = unauthorizedPlayer.NickName;
        player.Chips = 500;
        player.IsInQueue = true;

        addPlayerToQueue(player);

        ICommand registerPokerPlayer = new RegisterPokerPlayer();
        registerPokerPlayer.setClientToSendCommand(clientSocket);
        registerPokerPlayer.setObjectToSend(player);
        registerPokerPlayer.sendToClient();

        playerAuthorized(player.NickName);
    }

    private boolean isPlayerExist(String nickName)
    {
        var player = getPlayer(nickName);

        if(player == null)
            return false;

        return true;
    }

    public void addPlayerToQueue(PlayerModel player)
    {
        var nickName = player.NickName;

        Poker.Table.PlayersInQueue.add(player);
        var text = String.format("Игрок <%s> был добавлен в очередь", player.NickName);
        System.out.println(text);
    }

    private void playerAuthorized(String nickName)
    {
        var text = String.format("Игрок <%s> авторизовался", nickName);
        System.out.println(text);

        if(isGameCanBeStarted() == true)
            restartWaitingTimer(Poker.WaitingDelay);

        new UpdateInfo().sendToClient();
    }

    private boolean isGameCanBeStarted()
    {
        removeDisconnectedPlayers();

        var playersFromPlacePlayerMap = Poker.Table.PlacePlayerMap.size();
        var playersFromQueueCount = Poker.Table.PlayersInQueue.size();

        var playersCount = playersFromPlacePlayerMap + playersFromQueueCount;

        if(playersCount >= Poker.MinPlayersCountNeededToStartGame)
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

    public ClientTableModel getClientTable()
    {
        ClientTableModel clientTable = new ClientTableModel();
        clientTable.Pot             = Poker.Table.Pot                  ;
        clientTable.Bet             = Poker.Table.Bet                  ;
        clientTable.PlayerIndexTurn = Poker.Table.PlayerIndexTurn      ;
        clientTable.TimerStartTime  = Poker.Table.TimerStartTime;
        clientTable.Winner          = Poker.Table.Winner               ;
        clientTable.GameState = Poker.Table.GameState;
        clientTable.LobbyState = Poker.Table.LobbyState;
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

        var text = String.format("Таймер ожиданияя игроков начался: %s (delay), LobbyState: %s, GameStage: %s", delay, Poker.Table.LobbyState, Poker.Table.GameState);
        System.out.println(text);

        WaitingTimer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                WaitingTimerElapsed();}
        }, delay);
    }

    private void WaitingTimerElapsed() { startGame(); }

    public void restartGameTimer(int delay)
    {
        GameTimer.cancel();
        Poker.Table.TimerStartTime = delay;
        GameTimer = new Timer();

        var text = String.format("Таймер начался: %s (delay), LobbyState: %s, GameStage: %s", delay, Poker.Table.LobbyState, Poker.Table.GameState);
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
        System.out.println("\nТаймер хода завершился");

        if(Poker.Table.LobbyState == LobbyState.Ended)
        {
            if (isGameCanBeStarted() == true)
                startGame();
            else
                System.out.println("Подключённых игроков меньше 2, игра не может быть начата");

            return;
        }

        if(Poker.Table.LobbyState == LobbyState.Started)
        {
            var player = Poker.Table.PlacePlayerMap.get(Poker.Table.PlayerIndexTurn);

            if (player == null)
            {
                System.out.println("Игрок, который должен был ходить, вышел из игры");
                move(new PlayerModel(), MoveType.Check, 0);
            }
            else
                move(player, MoveType.Check, 0);
                //move(player, MoveType.Fold, 0);
        }
    }
}
