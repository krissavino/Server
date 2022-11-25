package Server.Poker.Enums;

public enum GameStage {
    Preflop,
    Flop,
    Turn,
    River;
    private static GameStage[] stages = values();
    public GameStage next()
    {
        return stages[(this.ordinal()+1) % stages.length];
    }
}
