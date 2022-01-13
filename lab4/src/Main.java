import Core.Core;
import UI.GUIcontroller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    private static final String rootMenuPath = "/UI/start.fxml";
    private static final int appWindowWidth = 800;
    private static final int appWindowHeight = 500;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Parent rootMenu = FXMLLoader.load(getClass().getResource(rootMenuPath));

        FXMLLoader loader = new FXMLLoader();
        Parent rootMenu = loader.load(getClass().getResource(rootMenuPath).openStream());
        stage.setScene(new Scene(rootMenu, appWindowWidth, appWindowHeight));
        GUIcontroller guiController = loader.getController();

        Core gameCore = new Core(guiController);
        gameCore.start();

        stage.show();
    }
}
