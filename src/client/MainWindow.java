package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.model.User;
import server.model.Grade;
import server.network.Request;
import server.network.Response;
import java.util.List;

public class MainWindow extends Application {
    private ServerConnection connection;
    private User currentUser;
    private TableView<Grade> gradeTable;

    public MainWindow() {}

    public MainWindow(ServerConnection connection, User currentUser) {
        this.connection = connection;
        this.currentUser = currentUser;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Система успеваемости - " + currentUser.getFullName());

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Верхняя панель с информацией
        Label infoLabel = new Label("Роль: " + currentUser.getRole() + " | " + currentUser.getFullName());
        ToolBar toolBar = new ToolBar(infoLabel);
        root.setTop(toolBar);

        // Таблица с оценками
        gradeTable = new TableView<>();
        setupGradeTable();

        // Кнопки
        VBox buttonBox = new VBox(10);
        buttonBox.setPadding(new Insets(10));

        Button refreshButton = new Button("Обновить оценки");
        refreshButton.setOnAction(e -> loadGrades());

        buttonBox.getChildren().add(refreshButton);

        // Если преподаватель - добавляем кнопку добавления оценки
        if ("TEACHER".equals(currentUser.getRole())) {
            Button addButton = new Button("Добавить оценку");
            addButton.setOnAction(e -> showAddGradeDialog());
            buttonBox.getChildren().add(addButton);
        }

        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(e -> logout(primaryStage));
        buttonBox.getChildren().add(logoutButton);

        root.setCenter(gradeTable);
        root.setRight(buttonBox);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadGrades();
    }

    private void setupGradeTable() {
        TableColumn<Grade, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        TableColumn<Grade, String> courseCol = new TableColumn<>("Предмет");
        courseCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCourseName()));

        TableColumn<Grade, Integer> gradeCol = new TableColumn<>("Оценка");
        gradeCol.setCellValueFactory(cellData -> javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getGradeValue()).asObject());

        TableColumn<Grade, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGradeType()));

        TableColumn<Grade, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate()));

        gradeTable.getColumns().addAll(idCol, courseCol, gradeCol, typeCol, dateCol);
        gradeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadGrades() {
        Request request = new Request("GET_GRADES", currentUser.getId());
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            List<Grade> grades = (List<Grade>) response.getData();
            ObservableList<Grade> data = FXCollections.observableArrayList(grades);
            gradeTable.setItems(data);
        } else {
            showAlert("Ошибка", response.getMessage());
        }
    }

    private void showAddGradeDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Добавить оценку");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField studentIdField = new TextField();
        TextField studentNameField = new TextField();
        TextField courseNameField = new TextField();
        TextField gradeValueField = new TextField();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Экзамен", "Зачёт", "Курсовая", "Лабораторная");
        typeCombo.setValue("Экзамен");
        DatePicker datePicker = new DatePicker();

        grid.add(new Label("ID студента:"), 0, 0);
        grid.add(studentIdField, 1, 0);
        grid.add(new Label("Имя студента:"), 0, 1);
        grid.add(studentNameField, 1, 1);
        grid.add(new Label("Предмет:"), 0, 2);
        grid.add(courseNameField, 1, 2);
        grid.add(new Label("Оценка (1-10):"), 0, 3);
        grid.add(gradeValueField, 1, 3);
        grid.add(new Label("Тип:"), 0, 4);
        grid.add(typeCombo, 1, 4);
        grid.add(new Label("Дата:"), 0, 5);
        grid.add(datePicker, 1, 5);

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            try {
                int studentId = Integer.parseInt(studentIdField.getText());
                String studentName = studentNameField.getText();
                String courseName = courseNameField.getText();
                int gradeValue = Integer.parseInt(gradeValueField.getText());
                String gradeType = typeCombo.getValue();
                String date = datePicker.getValue().toString();

                Request request = new Request("ADD_GRADE", new Object[]{
                        studentId, studentName, courseName, gradeValue, gradeType, date
                });
                Response response = connection.sendRequest(request);

                if (response.isSuccess()) {
                    showAlert("Успех", "Оценка добавлена");
                    dialog.close();
                    loadGrades();
                } else {
                    showAlert("Ошибка", response.getMessage());
                }
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Введите корректные числа");
            }
        });

        grid.add(saveButton, 1, 6);

        Scene scene = new Scene(grid, 350, 300);
        dialog.setScene(scene);
        dialog.show();
    }

    private void logout(Stage stage) {
        Request request = new Request("LOGOUT", null);
        connection.sendRequest(request);
        connection.disconnect();

        stage.close();
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.start(new Stage());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}