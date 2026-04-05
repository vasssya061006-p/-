package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import server.model.User;
import server.network.Request;
import server.network.Response;

/**
 * JavaFX login window for the education system.
 * Connects to server and authenticates users.
 */
public class LoginWindow extends Application {
    private TextField loginField;
    private PasswordField passwordField;
    private ServerConnection connection;

    @Override
    public void start(Stage primaryStage) {
        connection = new ServerConnection("localhost", 8080);
        if (!connection.connect()) {
            showAlert("Connection Error", "Failed to connect to server. Please ensure server is running.");
            System.exit(1);
            return;
        }

        // Create UI elements
        GridPane grid = createLoginForm();
        
        Scene scene = new Scene(grid, 400, 250);
        scene.getStylesheets().add(getClass().getResource("/client/styles.css").toExternalForm());
        primaryStage.setTitle("Education System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private GridPane createLoginForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Title
        Label titleLabel = new Label("Education Management System");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane.setConstraints(titleLabel, 0, 0, 2, 1);

        // Login field
        Label loginLabel = new Label("Login:");
        loginField = new TextField();
        loginField.setPromptText("Enter your login");
        
        // Password field
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        
        // Login button
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        loginButton.setDisable(true);
        
        // Enable button only when fields are not empty
        loginField.textProperty().addListener((obs, old, newVal) -> 
            loginButton.setDisable(newVal.trim().isEmpty()));
        passwordField.textProperty().addListener((obs, old, newVal) -> 
            loginButton.setDisable(newVal.trim().isEmpty()));

        // Layout
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(loginLabel, 0, 1);
        grid.add(loginField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);

        // Event handlers
        loginButton.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());

        return grid;
    }

    private void login() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both login and password");
            return;
        }

        // Send login request (password is sent in plain text, hashed on server)
        Request request = new Request("LOGIN", new Object[]{login, password});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            User user = (User) response.getData();
            System.out.println("Login successful: " + user.getFullName() + " (" + user.getRole() + ")");
            openMainWindow(user);
        } else {
            showAlert("Authentication Failed", response.getMessage());
            passwordField.clear();
        }
    }

    private void openMainWindow(User user) {
        try {
            Stage mainStage = new Stage();
            MainWindow mainWindow = new MainWindow(connection, user);
            mainWindow.start(mainStage);
            
            // Close login window
            Stage loginStage = (Stage) loginField.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            showAlert("Error", "Failed to open main window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}