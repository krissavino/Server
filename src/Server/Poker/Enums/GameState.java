package Server.Poker.Enums;

public enum GameState {
    Preflop,
    Flop,
    Turn,
    River;
    private static GameState[] stages = values();
    public GameState next()
    {
        return stages[(this.ordinal()+1) % stages.length];
    }
}
