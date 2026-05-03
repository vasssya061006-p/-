package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.network.Request;
import server.network.Response;
import java.util.List;

public class AdminStudentManagementTab {

    private ServerConnection connection;
    private VBox content;

    private TextField loginField;
    private TextField fullNameField;
    private ComboBox<String> groupCombo;
    private TextField yearField;
    private TextField studentIdField;
    private ListView<String> studentList;
    private TextArea resultArea;

    private TextField editIdField;
    private TextField editLoginField;
    private TextField editFullNameField;
    private ComboBox<String> editGroupCombo;
    private TextField editYearField;
    private TextField editStudentIdField;
    private CheckBox editActiveCheck;

    public AdminStudentManagementTab(ServerConnection connection) {
        this.connection = connection;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Управление студентами");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TitledPane addPane = new TitledPane("➕ Добавить студента", createAddForm());
        addPane.setCollapsible(true);

        TitledPane editPane = new TitledPane("✏️ Редактировать студента", createEditForm());
        editPane.setCollapsible(true);

        Label listLabel = new Label("Список студентов:");
        listLabel.setStyle("-fx-font-weight: bold;");
        studentList = new ListView<>();
        studentList.setPrefHeight(200);

        HBox listButtons = new HBox(10);
        Button refreshBtn = new Button("🔄 Обновить список");
        refreshBtn.setOnAction(e -> loadStudents());

        Button deleteBtn = new Button("🗑️ Удалить выбранного студента");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteSelectedStudent());

        listButtons.getChildren().addAll(refreshBtn, deleteBtn);

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(80);
        resultArea.setStyle("-fx-font-family: monospace;");

        content.getChildren().addAll(
                title, addPane, editPane, new Separator(),
                listLabel, studentList, listButtons,
                new Separator(), resultArea
        );

        loadGroups();
        loadStudents();

        studentList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                fillEditForm(selected);
            }
        });

        return content;
    }

    private GridPane createAddForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label loginLabel = new Label("Логин:");
        loginField = new TextField();
        loginField.setPromptText("student_login");

        Label nameLabel = new Label("ФИО:");
        fullNameField = new TextField();
        fullNameField.setPromptText("Иванов Иван Иванович");

        Label groupLabel = new Label("Группа:");
        groupCombo = new ComboBox<>();
        groupCombo.setPromptText("Выберите группу");
        groupCombo.setPrefWidth(200);

        Label yearLabel = new Label("Курс:");
        yearField = new TextField();
        yearField.setPromptText("1, 2, 3, 4");

        Label studentIdLabel = new Label("Номер студенческого:");
        studentIdField = new TextField();
        studentIdField.setPromptText("STUXXX");

        Button addBtn = new Button("Добавить");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addStudent());

        form.add(loginLabel, 0, 0);
        form.add(loginField, 1, 0);
        form.add(nameLabel, 0, 1);
        form.add(fullNameField, 1, 1);
        form.add(groupLabel, 0, 2);
        form.add(groupCombo, 1, 2);
        form.add(yearLabel, 0, 3);
        form.add(yearField, 1, 3);
        form.add(studentIdLabel, 0, 4);
        form.add(studentIdField, 1, 4);
        form.add(addBtn, 1, 5);

        return form;
    }

    private GridPane createEditForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label idLabel = new Label("ID студента:");
        editIdField = new TextField();
        editIdField.setEditable(false);

        Label loginLabel = new Label("Логин:");
        editLoginField = new TextField();

        Label nameLabel = new Label("ФИО:");
        editFullNameField = new TextField();

        Label groupLabel = new Label("Группа:");
        editGroupCombo = new ComboBox<>();
        editGroupCombo.setPrefWidth(200);

        Label yearLabel = new Label("Курс:");
        editYearField = new TextField();

        Label studentIdLabel = new Label("Номер студенческого:");
        editStudentIdField = new TextField();

        Label activeLabel = new Label("Активен:");
        editActiveCheck = new CheckBox();

        Button updateBtn = new Button("Сохранить изменения");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> updateStudent());

        form.add(idLabel, 0, 0);
        form.add(editIdField, 1, 0);
        form.add(loginLabel, 0, 1);
        form.add(editLoginField, 1, 1);
        form.add(nameLabel, 0, 2);
        form.add(editFullNameField, 1, 2);
        form.add(groupLabel, 0, 3);
        form.add(editGroupCombo, 1, 3);
        form.add(yearLabel, 0, 4);
        form.add(editYearField, 1, 4);
        form.add(studentIdLabel, 0, 5);
        form.add(editStudentIdField, 1, 5);
        form.add(activeLabel, 0, 6);
        form.add(editActiveCheck, 1, 6);
        form.add(updateBtn, 1, 7);

        return form;
    }

    private void loadGroups() {
        Request request = new Request("GET_ALL_GROUPS", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> groups = (List<String>) response.getData();
            groupCombo.getItems().clear();
            groupCombo.getItems().addAll(groups);
            editGroupCombo.getItems().clear();
            editGroupCombo.getItems().addAll(groups);
        }
    }

    private void addStudent() {
        String login = loginField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String group = groupCombo.getValue();
        String yearText = yearField.getText().trim();
        String studentIdNumber = studentIdField.getText().trim();

        if (login.isEmpty() || fullName.isEmpty() || group == null || yearText.isEmpty()) {
            resultArea.setText("❌ Ошибка: Заполните все обязательные поля");
            return;
        }

        // Извлекаем только название группы (убираем ID и курс)
        if (group.contains(":")) {
            group = group.split(":")[1].trim().split(" \\(")[0];
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            resultArea.setText("❌ Ошибка: Курс должен быть числом");
            return;
        }

        Request request = new Request("ADD_STUDENT", new Object[]{login, fullName, group, year, studentIdNumber});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Студент \"" + fullName + "\" успешно добавлен!\nПароль: " + login);
            loginField.clear();
            fullNameField.clear();
            yearField.clear();
            studentIdField.clear();
            loadStudents();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void fillEditForm(String selected) {
        try {
            String[] parts = selected.split(":");
            int id = Integer.parseInt(parts[0].trim());
            editIdField.setText(String.valueOf(id));

            String namePart = parts[1].trim();
            String fullName = namePart.split(" \\(")[0];
            editFullNameField.setText(fullName);

            String loginPart = namePart.split("login: ")[1].replace(")", "");
            editLoginField.setText(loginPart);

            // Очищаем поля
            editYearField.clear();
            editStudentIdField.clear();
            editActiveCheck.setSelected(true);
        } catch (Exception e) {
            resultArea.setText("❌ Ошибка при загрузке данных студента");
        }
    }

    private void updateStudent() {
        int id = Integer.parseInt(editIdField.getText().trim());
        String login = editLoginField.getText().trim();
        String fullName = editFullNameField.getText().trim();
        String group = editGroupCombo.getValue();
        int year = Integer.parseInt(editYearField.getText().trim());
        String studentIdNumber = editStudentIdField.getText().trim();
        boolean isActive = editActiveCheck.isSelected();

        Request request = new Request("UPDATE_STUDENT", new Object[]{id, login, fullName, group, year, studentIdNumber, isActive});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Студент успешно обновлён!");
            loadStudents();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void deleteSelectedStudent() {
        String selected = studentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resultArea.setText("❌ Пожалуйста, выберите студента для удаления");
            return;
        }

        int id = Integer.parseInt(selected.split(":")[0].trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить этого студента?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Request request = new Request("DELETE_STUDENT", new Object[]{id});
            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                resultArea.setText("✅ Студент успешно удалён!");
                loadStudents();
                editIdField.clear();
                editLoginField.clear();
                editFullNameField.clear();
                editYearField.clear();
                editStudentIdField.clear();
            } else {
                resultArea.setText("❌ Ошибка: " + response.getMessage());
            }
        }
    }

    private void loadStudents() {
        Request request = new Request("GET_ALL_STUDENTS", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> students = (List<String>) response.getData();
            studentList.getItems().clear();
            studentList.getItems().addAll(students);
        } else {
            studentList.getItems().clear();
            studentList.getItems().add("Не удалось загрузить студентов");
        }
    }
}