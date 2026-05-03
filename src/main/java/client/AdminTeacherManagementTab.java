package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.network.Request;
import server.network.Response;
import java.util.List;

public class AdminTeacherManagementTab {

    private ServerConnection connection;
    private VBox content;

    private ListView<String> teacherList;
    private TextArea resultArea;

    // Поля для добавления
    private TextField addLoginField;
    private TextField addFullNameField;
    private TextField addPositionField;
    private TextField addDepartmentField;
    private TextField addSpecializationField;
    private TextField addHoursField;

    // Поля для редактирования
    private TextField editIdField;
    private TextField editLoginField;
    private TextField editFullNameField;
    private TextField editPositionField;
    private TextField editDepartmentField;
    private TextField editSpecializationField;
    private TextField editHoursField;
    private CheckBox editActiveCheck;

    public AdminTeacherManagementTab(ServerConnection connection) {
        this.connection = connection;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Управление преподавателями");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TitledPane addPane = new TitledPane("➕ Добавить преподавателя", createAddForm());
        addPane.setCollapsible(true);

        TitledPane editPane = new TitledPane("✏️ Редактировать преподавателя", createEditForm());
        editPane.setCollapsible(true);

        Label listLabel = new Label("Список преподавателей:");
        listLabel.setStyle("-fx-font-weight: bold;");

        teacherList = new ListView<>();
        teacherList.setPrefHeight(250);

        HBox listButtons = new HBox(10);
        Button refreshBtn = new Button("🔄 Обновить список");
        refreshBtn.setOnAction(e -> loadTeachers());

        Button deleteBtn = new Button("🗑️ Удалить выбранного преподавателя");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteTeacher());

        listButtons.getChildren().addAll(refreshBtn, deleteBtn);

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(100);
        resultArea.setStyle("-fx-font-family: monospace;");

        content.getChildren().addAll(
                title, addPane, editPane, new Separator(),
                listLabel, teacherList, listButtons,
                new Separator(), resultArea
        );

        loadTeachers();

        teacherList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
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
        addLoginField = new TextField();
        addLoginField.setPromptText("teacher_login");

        Label nameLabel = new Label("ФИО:");
        addFullNameField = new TextField();
        addFullNameField.setPromptText("Иванов Иван Иванович");

        Label positionLabel = new Label("Должность:");
        addPositionField = new TextField();
        addPositionField.setPromptText("Профессор, Доцент, Старший преподаватель");

        Label departmentLabel = new Label("Кафедра:");
        addDepartmentField = new TextField();
        addDepartmentField.setPromptText("Computer Science, Математика");

        Label specLabel = new Label("Специализация:");
        addSpecializationField = new TextField();
        addSpecializationField.setPromptText("Java, Python, Базы данных");

        Label hoursLabel = new Label("Часов в неделю:");
        addHoursField = new TextField();
        addHoursField.setPromptText("18, 20, 24");

        Button addBtn = new Button("Добавить преподавателя");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addTeacher());

        form.add(loginLabel, 0, 0);
        form.add(addLoginField, 1, 0);
        form.add(nameLabel, 0, 1);
        form.add(addFullNameField, 1, 1);
        form.add(positionLabel, 0, 2);
        form.add(addPositionField, 1, 2);
        form.add(departmentLabel, 0, 3);
        form.add(addDepartmentField, 1, 3);
        form.add(specLabel, 0, 4);
        form.add(addSpecializationField, 1, 4);
        form.add(hoursLabel, 0, 5);
        form.add(addHoursField, 1, 5);
        form.add(addBtn, 1, 6);

        return form;
    }

    private GridPane createEditForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label idLabel = new Label("ID:");
        editIdField = new TextField();
        editIdField.setEditable(false);
        editIdField.setPrefWidth(100);

        Label loginLabel = new Label("Логин:");
        editLoginField = new TextField();
        editLoginField.setPrefWidth(200);

        Label nameLabel = new Label("ФИО:");
        editFullNameField = new TextField();
        editFullNameField.setPrefWidth(250);

        Label positionLabel = new Label("Должность:");
        editPositionField = new TextField();
        editPositionField.setPrefWidth(200);

        Label departmentLabel = new Label("Кафедра:");
        editDepartmentField = new TextField();
        editDepartmentField.setPrefWidth(200);

        Label specLabel = new Label("Специализация:");
        editSpecializationField = new TextField();
        editSpecializationField.setPrefWidth(200);

        Label hoursLabel = new Label("Часов в неделю:");
        editHoursField = new TextField();
        editHoursField.setPrefWidth(100);

        Label activeLabel = new Label("Активен:");
        editActiveCheck = new CheckBox();

        Button updateBtn = new Button("Сохранить изменения");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> updateTeacher());

        form.add(idLabel, 0, 0);
        form.add(editIdField, 1, 0);
        form.add(loginLabel, 0, 1);
        form.add(editLoginField, 1, 1);
        form.add(nameLabel, 0, 2);
        form.add(editFullNameField, 1, 2);
        form.add(positionLabel, 0, 3);
        form.add(editPositionField, 1, 3);
        form.add(departmentLabel, 0, 4);
        form.add(editDepartmentField, 1, 4);
        form.add(specLabel, 0, 5);
        form.add(editSpecializationField, 1, 5);
        form.add(hoursLabel, 0, 6);
        form.add(editHoursField, 1, 6);
        form.add(activeLabel, 0, 7);
        form.add(editActiveCheck, 1, 7);
        form.add(updateBtn, 1, 8);

        return form;
    }

    private void addTeacher() {
        String login = addLoginField.getText().trim();
        String fullName = addFullNameField.getText().trim();
        String position = addPositionField.getText().trim();
        String department = addDepartmentField.getText().trim();
        String specialization = addSpecializationField.getText().trim();
        String hoursText = addHoursField.getText().trim();

        if (login.isEmpty() || fullName.isEmpty()) {
            resultArea.setText("❌ Ошибка: Заполните логин и ФИО");
            return;
        }

        int teachingHours = 18;
        if (!hoursText.isEmpty()) {
            try {
                teachingHours = Integer.parseInt(hoursText);
            } catch (NumberFormatException e) {
                resultArea.setText("❌ Ошибка: Часы должны быть числом");
                return;
            }
        }

        Request request = new Request("ADD_TEACHER", new Object[]{
                login, fullName, position, department, specialization, teachingHours
        });
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Преподаватель \"" + fullName + "\" успешно добавлен!\nПароль: " + login);
            addLoginField.clear();
            addFullNameField.clear();
            addPositionField.clear();
            addDepartmentField.clear();
            addSpecializationField.clear();
            addHoursField.clear();
            loadTeachers();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void deleteTeacher() {
        String selected = teacherList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resultArea.setText("❌ Пожалуйста, выберите преподавателя для удаления");
            return;
        }

        int id = Integer.parseInt(selected.split(":")[0].trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить этого преподавателя?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Request request = new Request("DELETE_TEACHER", new Object[]{id});
            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                resultArea.setText("✅ Преподаватель успешно удалён!");
                loadTeachers();
                clearEditForm();
            } else {
                resultArea.setText("❌ Ошибка: " + response.getMessage());
            }
        }
    }

    private void loadTeachers() {
        Request request = new Request("GET_ALL_TEACHERS_FOR_MANAGEMENT", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> teachers = (List<String>) response.getData();
            teacherList.getItems().clear();
            teacherList.getItems().addAll(teachers);
        } else {
            teacherList.getItems().clear();
            teacherList.getItems().add("Не удалось загрузить преподавателей");
        }
    }

    private void fillEditForm(String selected) {
        try {
            String[] parts = selected.split(":");
            int id = Integer.parseInt(parts[0].trim());
            editIdField.setText(String.valueOf(id));

            String rest = parts[1].trim();
            String fullName = rest.split(" \\(")[0];
            editFullNameField.setText(fullName);

            String loginPart = rest.split("login: ")[1].replace(")", "");
            editLoginField.setText(loginPart);

            loadTeacherDetails(id);
        } catch (Exception e) {
            resultArea.setText("❌ Ошибка при загрузке данных преподавателя");
        }
    }

    private void clearEditForm() {
        editIdField.clear();
        editLoginField.clear();
        editFullNameField.clear();
        editPositionField.clear();
        editDepartmentField.clear();
        editSpecializationField.clear();
        editHoursField.clear();
        editActiveCheck.setSelected(false);
    }

    private void loadTeacherDetails(int teacherId) {
        Request request = new Request("GET_TEACHER_DETAILS", new Object[]{teacherId});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            Object[] details = (Object[]) response.getData();
            editPositionField.setText((String) details[0]);
            editDepartmentField.setText((String) details[1]);
            editSpecializationField.setText((String) details[2]);
            editHoursField.setText(String.valueOf(details[3]));
            editActiveCheck.setSelected((Boolean) details[4]);
        }
    }

    private void updateTeacher() {
        int id = Integer.parseInt(editIdField.getText().trim());
        String login = editLoginField.getText().trim();
        String fullName = editFullNameField.getText().trim();
        String position = editPositionField.getText().trim();
        String department = editDepartmentField.getText().trim();
        String specialization = editSpecializationField.getText().trim();
        int teachingHours = Integer.parseInt(editHoursField.getText().trim());
        boolean isActive = editActiveCheck.isSelected();

        Request request = new Request("UPDATE_TEACHER", new Object[]{
                id, login, fullName, position, department, specialization, teachingHours, isActive
        });
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Преподаватель успешно обновлён!");
            loadTeachers();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }
}