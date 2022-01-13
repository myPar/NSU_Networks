package Core;

import Controller.Controller;
import Controller.UserCommand;
import Model.Model;
import Net.ChannelProvider;
import Net.Socket;
import UI.GUIcontroller;
import UI.UI;
import Model.GameModel;

import java.io.IOException;
import java.util.List;

// core algorithm. Controls Server/Client mode switching
public class Core {
    private UI uiController;
    private Model gameModel;
    private GameProcess gameProcess;

    static final int groupPort = 9192;
    static final String groupAddress = "239.192.0.4";

    public Core(UI ui) {
        uiController = ui;
        // init app modules:
        gameModel = new GameModel(uiController);
        gameProcess = new GameProcess(gameModel, groupAddress, groupPort);
        // set app controller in UI instance; args - application controller with the constructor's argument - controller user
        uiController.setAppController(new Controller(gameProcess));
    }

    public void start() {

    }
}
