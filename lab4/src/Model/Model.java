package Model;

import Protocol.Data;

// Game model interface for model updating
public interface Model {
    void initNewGame(Data.GameConfig initConfig);
    void setState(Data.GameState state);
    boolean canJoinNewPlayer();
}
