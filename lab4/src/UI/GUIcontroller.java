package UI;

import java.net.URL;
import java.util.ResourceBundle;

import Controller.Controller;
import Controller.ControllerInterface;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import Controller.UserCommand;
import Controller.UserCommand.Type;

public class GUIcontroller implements UI{
    private ControllerInterface controller;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<String> availableGamesList;

    @FXML
    private Button createNewGameButton;

    @FXML
    private BorderPane gamePanel;

    @FXML
    private Button joinGameButton;

    @FXML
    void availableGamesClicked(MouseEvent event) {
        System.out.println("available games button clicked");
        String item = availableGamesList.getSelectionModel().getSelectedItem();
        if (item != null) {
            controller.putUserCommand(new UserCommand.CHOOSE_GAME(item));
        }
    }

    @FXML
    void createNewGameClicked(MouseEvent event) {
        System.out.println("create new game button clicked");
        controller.putUserCommand(new UserCommand(Type.START_NEW_GAME));
    }

    @FXML
    void jonClicked(MouseEvent event) {
        System.out.println("join button clicked");
        controller.putUserCommand(new UserCommand(Type.JOIN_GAME));
    }

    @FXML
    void keyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        switch(code) {
            case W:
                System.out.println("w");
                controller.putUserCommand(new UserCommand(Type.KEY_UP));
                break;
            case S:
                System.out.println("s");
                controller.putUserCommand(new UserCommand(Type.KEY_DOWN));
                break;
            case A:
                System.out.println("a");
                controller.putUserCommand(new UserCommand(Type.KEY_LEFT));
                break;
            case D:
                System.out.println("d");
                controller.putUserCommand(new UserCommand(Type.KEY_RIGHT));
                break;
            default:
        }
    }

    @FXML
    void initialize() {
    }

    @Override
    public void setAppController(Controller appController) {
        this.controller = appController;
    }
}
