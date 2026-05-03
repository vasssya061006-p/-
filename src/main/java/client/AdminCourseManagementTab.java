package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.network.Request;
import server.network.Response;
import java.util.List;

public class AdminCourseManagementTab {

    private ServerConnection connection;
    private VBox content;

    private TextField nameField;
    private TextField creditsField;
    private TextField teacherIdField;
    private TextField departmentField;
    private TextArea descriptionArea;
    private ListView<String> courseList;
    private TextArea resultArea;

    private TextField editIdField;
    private TextField editNameField;
    private TextField editCreditsField;
    private TextField editTeacherIdField;
    private TextField editDepartmentField;
    private TextArea editDescriptionArea;

    public AdminCourseManagementTab(ServerConnection connection) {
        this.connection = connection;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Управление курсами");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TitledPane addPane = new TitledPane("➕ Добавить курс", createAddForm());
        addPane.setCollapsible(true);

        TitledPane editPane = new TitledPane("✏️ Редактировать курс", createEditForm());
        editPane.setCollapsible(true);

        Label listLabel = new Label("Существующие курсы:");
        listLabel.setStyle("-fx-font-weight: bold;");
        courseList = new ListView<>();
        courseList.setPrefHeight(200);

        HBox listButtons = new HBox(10);
        Button refreshBtn = new Button("🔄 Обновить список");
        refreshBtn.setOnAction(e -> loadCourses());

        Button deleteBtn = new Button("🗑️ Удалить выбранный курс");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteSelectedCourse());

        listButtons.getChildren().addAll(refreshBtn, deleteBtn);

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(80);
        resultArea.setStyle("-fx-font-family: monospace;");

        content.getChildren().addAll(
                title, addPane, editPane, new Separator(),
                listLabel, courseList, listButtons,
                new Separator(), resultArea
        );

        loadCourses();

        courseList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
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

        Label nameLabel = new Label("Название курса:");
        nameField = new TextField();
        nameField.setPromptText("Например: Algorithms");

        Label creditsLabel = new Label("Кредиты:");
        creditsField = new TextField();
        creditsField.setPromptText("3, 4, 5, 6");

        Label teacherLabel = new Label("ID преподавателя:");
        teacherIdField = new TextField();
        teacherIdField.setPromptText("ID учителя");

        Label departmentLabel = new Label("Кафедра:");
        departmentField = new TextField();
        departmentField.setPromptText("Например: Computer Science");

        Label descLabel = new Label("Описание:");
        descriptionArea = new TextArea();
        descriptionArea.setPrefHeight(60);
        descriptionArea.setPromptText("Краткое описание курса");

        Button addBtn = new Button("Добавить");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addCourse());

        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(creditsLabel, 0, 1);
        form.add(creditsField, 1, 1);
        form.add(teacherLabel, 0, 2);
        form.add(teacherIdField, 1, 2);
        form.add(departmentLabel, 0, 3);
        form.add(departmentField, 1, 3);
        form.add(descLabel, 0, 4);
        form.add(descriptionArea, 1, 4);
        form.add(addBtn, 1, 5);

        return form;
    }

    private GridPane createEditForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label idLabel = new Label("ID курса:");
        editIdField = new TextField();
        editIdField.setEditable(false);

        Label nameLabel = new Label("Название:");
        editNameField = new TextField();

        Label creditsLabel = new Label("Кредиты:");
        editCreditsField = new TextField();

        Label teacherLabel = new Label("ID преподавателя:");
        editTeacherIdField = new TextField();

        Label departmentLabel = new Label("Кафедра:");
        editDepartmentField = new TextField();

        Label descLabel = new Label("Описание:");
        editDescriptionArea = new TextArea();
        editDescriptionArea.setPrefHeight(60);

        Button updateBtn = new Button("Сохранить изменения");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> updateCourse());

        form.add(idLabel, 0, 0);
        form.add(editIdField, 1, 0);
        form.add(nameLabel, 0, 1);
        form.add(editNameField, 1, 1);
        form.add(creditsLabel, 0, 2);
        form.add(editCreditsField, 1, 2);
        form.add(teacherLabel, 0, 3);
        form.add(editTeacherIdField, 1, 3);
        form.add(departmentLabel, 0, 4);
        form.add(editDepartmentField, 1, 4);
        form.add(descLabel, 0, 5);
        form.add(editDescriptionArea, 1, 5);
        form.add(updateBtn, 1, 6);

        return form;
    }

    private void addCourse() {
        String name = nameField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (name.isEmpty() || creditsText.isEmpty()) {
            resultArea.setText("❌ Ошибка: Заполните название курса и кредиты");
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (NumberFormatException e) {
            resultArea.setText("❌ Ошибка: Кредиты должны быть числом");
            return;
        }

        Integer teacherId = null;
        if (!teacherIdField.getText().trim().isEmpty()) {
            teacherId = Integer.parseInt(teacherIdField.getText().trim());
        }

        String department = departmentField.getText().trim();
        String description = descriptionArea.getText().trim();

        Request request = new Request("ADD_COURSE", new Object[]{name, description, credits, teacherId, department});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Курс \"" + name + "\" успешно добавлен!");
            nameField.clear();
            creditsField.clear();
            teacherIdField.clear();
            departmentField.clear();
            descriptionArea.clear();
            loadCourses();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void fillEditForm(String selected) {
        // Формат: "ID: Название (X кредитов)"
        try {
            String[] parts = selected.split(":");
            int id = Integer.parseInt(parts[0].trim());
            editIdField.setText(String.valueOf(id));

            String namePart = parts[1].trim();
            String name = namePart.split(" \\(")[0];
            editNameField.setText(name);

            String creditsPart = namePart.split("\\(")[1].replace(" кредитов)", "");
            editCreditsField.setText(creditsPart);

            editTeacherIdField.clear();
            editDepartmentField.clear();
            editDescriptionArea.clear();
        } catch (Exception e) {
            resultArea.setText("❌ Ошибка при загрузке данных курса");
        }
    }

    private void updateCourse() {
        int id = Integer.parseInt(editIdField.getText().trim());
        String name = editNameField.getText().trim();
        int credits = Integer.parseInt(editCreditsField.getText().trim());

        Integer teacherId = null;
        if (!editTeacherIdField.getText().trim().isEmpty()) {
            teacherId = Integer.parseInt(editTeacherIdField.getText().trim());
        }

        String department = editDepartmentField.getText().trim();
        String description = editDescriptionArea.getText().trim();

        Request request = new Request("UPDATE_COURSE", new Object[]{id, name, description, credits, teacherId, department});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Курс успешно обновлён!");
            loadCourses();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void deleteSelectedCourse() {
        String selected = courseList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resultArea.setText("❌ Пожалуйста, выберите курс для удаления");
            return;
        }

        int id = Integer.parseInt(selected.split(":")[0].trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить этот курс?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Request request = new Request("DELETE_COURSE", new Object[]{id});
            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                resultArea.setText("✅ Курс успешно удалён!");
                loadCourses();
                editIdField.clear();
                editNameField.clear();
                editCreditsField.clear();
                editTeacherIdField.clear();
                editDepartmentField.clear();
                editDescriptionArea.clear();
            } else {
                resultArea.setText("❌ Ошибка: " + response.getMessage());
            }
        }
    }

    private void loadCourses() {
        Request request = new Request("GET_ALL_COURSES", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> courses = (List<String>) response.getData();
            courseList.getItems().clear();
            courseList.getItems().addAll(courses);
        } else {
            courseList.getItems().clear();
            courseList.getItems().add("Не удалось загрузить курсы");
        }
    }
}