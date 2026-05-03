package client;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class LoadingOverlay {

    private static Stage loadingStage;

    public static void show(String message) {
        loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Загрузка");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(60, 60);

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c5a7a;");

        VBox vbox = new VBox(15, progress, label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        Scene scene = new Scene(vbox, 250, 150);
        loadingStage.setScene(scene);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.show();
    }

    public static void hide() {
        if (loadingStage != null) {
            loadingStage.close();
            loadingStage = null;
        }
    }
}