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
        Parent rootMenu = FXMLLoader.load(getClass().getResource(rootMenuPath));
        stage.setScene(new Scene(rootMenu, appWindowWidth, appWindowHeight));
        stage.show();
    }
}
