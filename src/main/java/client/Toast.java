package client;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class Toast {

    public static void show(Window owner, String message, String type) {
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        Label label = new Label(message);
        label.setStyle("-fx-background-radius: 20; -fx-padding: 10 20; -fx-font-size: 13px; -fx-font-weight: bold;");

        switch (type) {
            case "success":
                label.setStyle(label.getStyle() + "-fx-background-color: #4caf50; -fx-text-fill: white;");
                break;
            case "error":
                label.setStyle(label.getStyle() + "-fx-background-color: #f44336; -fx-text-fill: white;");
                break;
            case "warning":
                label.setStyle(label.getStyle() + "-fx-background-color: #ff9800; -fx-text-fill: white;");
                break;
            default:
                label.setStyle(label.getStyle() + "-fx-background-color: #2c5a7a; -fx-text-fill: white;");
        }

        VBox container = new VBox(label);
        container.setStyle("-fx-padding: 10;");
        popup.getContent().add(container);

        popup.show(owner, owner.getX() + owner.getWidth() / 2 - 150, owner.getY() + owner.getHeight() - 100);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> popup.hide());
        delay.play();
    }

    public static void show(Window owner, String message) {
        show(owner, message, "info");
    }
}