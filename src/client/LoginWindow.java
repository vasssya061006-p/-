package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import server.model.User;
import server.network.Request;
import server.network.Response;

public class LoginWindow extends Application {
    private TextField loginField;
    private PasswordField passwordField;
    private ServerConnection connection;

    @Override
    public void start(Stage primaryStage) {
        connection = new ServerConnection("localhost", 8080);
        if (!connection.connect()) {
            showAlert("Ошибка", "Не удалось подключиться к серверу");
            return;
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label loginLabel = new Label("Логин:");
        loginField = new TextField();
        Label passwordLabel = new Label("Пароль:");
        passwordField = new PasswordField();
        Button loginButton = new Button("Войти");

        grid.add(loginLabel, 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        loginButton.setOnAction(e -> login());

        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void login() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Введите логин и пароль");
            return;
        }

        Request request = new Request("LOGIN", new Object[]{login, password});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            User user = (User) response.getData();
            openMainWindow(user);
        } else {
            showAlert("Ошибка", response.getMessage());
        }
    }

    private void openMainWindow(User user) {
        Stage stage = (Stage) loginField.getScene().getWindow();
        stage.close();

        MainWindow mainWindow = new MainWindow(connection, user);
        mainWindow.start(new Stage());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}