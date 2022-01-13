package Model;

import Protocol.Data;
import UI.UI;

// game model affects on UI by updating view
public class GameModel implements Model {
    private UI ui;

    public GameModel(UI ui) {

    }

    @Override
    public void initNewGame(Data.GameConfig initConfig) {

    }

    @Override
    public void setState(Data.GameState state) {

    }

    @Override
    public boolean canJoinNewPlayer() {
        return false;
    }
}
