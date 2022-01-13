package Core;

import Controller.Controller;
import Controller.UserCommand;
import Model.Model;
import UI.GUIcontroller;
import UI.UI;
import Model.GameModel;

import java.util.List;

// core algorithm. Controls Server/Client mode switching
public class Core {
    private UI uiController;
    private Model gameModel;
    private GameProcess gameProcess;

    // game process affects on the game model using Model interface
    private static class GameProcess implements ControllerUser, Runnable {
        private Model gameModerInterface;

        private GameProcess (Model model) {this.gameModerInterface = model;}

        @Override
        public void handleCommands(List<UserCommand> command) {

        }

        @Override
        public void run() {

        }
    }

    public Core(UI ui) {
        uiController = ui;
        // init app modules:
        gameModel = new GameModel(uiController);
        gameProcess = new GameProcess(gameModel);
        // set app controller in UI instance; args - application controller with the constructor's argument - controller user
        uiController.setAppController(new Controller(gameProcess));
    }

    public void start() {

    }
}
