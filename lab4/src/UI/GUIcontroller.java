package UI;

import java.net.URL;
import java.util.ResourceBundle;

import Controller.Controller;
import Controller.ControllerInterface;
import Core.Core;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

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
        // System.out.println("available games button clicked");
        availableGamesList.getSelectionModel().getSelectedItem();
    }

    @FXML
    void createNewGameClicked(MouseEvent event) {
        System.out.println("create new game button clicked");
    }

    @FXML
    void jonClicked(MouseEvent event) {
        System.out.println("join button clicked");
    }

    @FXML
    void initialize() {
    }

    @Override
    public void setAppController(Controller appController) {
        this.controller = appController;
    }
}
