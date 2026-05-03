package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.network.Request;
import server.network.Response;
import java.util.List;

public class AdminScheduleTab {

    private ServerConnection connection;
    private VBox content;
    private TextArea resultArea;
    private ListView<String> scheduleList;

    // Поля для добавления расписания
    private ComboBox<String> courseCombo;
    private ComboBox<String> groupCombo;
    private ComboBox<String> teacherCombo;
    private TextField roomField;
    private ComboBox<String> dayCombo;
    private TextField startTimeField;
    private TextField endTimeField;
    private ComboBox<String> typeCombo;
    private TextField semesterField;

    public AdminScheduleTab(ServerConnection connection) {
        this.connection = connection;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Управление расписанием");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ========== ФОРМА ДОБАВЛЕНИЯ ==========
        TitledPane addPane = new TitledPane("➕ Добавить занятие", createAddForm());
        addPane.setCollapsible(true);

        // ========== ПРОВЕРКА КОНФЛИКТОВ ==========
        TitledPane checkPane = new TitledPane("🔍 Проверка конфликтов", createCheckForm());
        checkPane.setCollapsible(true);

        // ========== СПИСОК РАСПИСАНИЯ ==========
        Label listLabel = new Label("Текущее расписание:");
        listLabel.setStyle("-fx-font-weight: bold;");

        scheduleList = new ListView<>();
        scheduleList.setPrefHeight(250);

        HBox listButtons = new HBox(10);
        Button refreshBtn = new Button("🔄 Обновить список");
        refreshBtn.setOnAction(e -> loadSchedules());

        Button deleteBtn = new Button("🗑️ Удалить выбранное занятие");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteSelectedSchedule());

        listButtons.getChildren().addAll(refreshBtn, deleteBtn);

        // Область для результата
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(100);
        resultArea.setStyle("-fx-font-family: monospace;");

        content.getChildren().addAll(
                title, addPane, checkPane, new Separator(),
                listLabel, scheduleList, listButtons,
                new Separator(), resultArea
        );

        // Загружаем данные для комбобоксов и список расписания
        loadCourses();
        loadGroups();
        loadTeachers();
        loadSchedules();

        return content;
    }

    private GridPane createAddForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label courseLabel = new Label("Курс:");
        courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Выберите курс");
        courseCombo.setPrefWidth(200);

        Label groupLabel = new Label("Группа:");
        groupCombo = new ComboBox<>();
        groupCombo.setPromptText("Выберите группу");
        groupCombo.setPrefWidth(200);

        Label teacherLabel = new Label("Преподаватель:");
        teacherCombo = new ComboBox<>();
        teacherCombo.setPromptText("Выберите преподавателя");
        teacherCombo.setPrefWidth(200);

        Label roomLabel = new Label("Аудитория:");
        roomField = new TextField();
        roomField.setPromptText("Например: 301");

        Label dayLabel = new Label("День недели:");
        dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        dayCombo.setPromptText("Выберите день");

        Label startLabel = new Label("Время начала:");
        startTimeField = new TextField();
        startTimeField.setPromptText("Например: 10:00");

        Label endLabel = new Label("Время окончания:");
        endTimeField = new TextField();
        endTimeField.setPromptText("Например: 11:30");

        Label typeLabel = new Label("Тип занятия:");
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("lecture", "practice", "lab", "consultation", "exam");
        typeCombo.setPromptText("Выберите тип");

        Label semesterLabel = new Label("Семестр:");
        semesterField = new TextField();
        semesterField.setPromptText("Например: 2025-2026-spring");
        semesterField.setText("2025-2026-spring");

        Button addBtn = new Button("Добавить");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addSchedule());

        form.add(courseLabel, 0, 0);
        form.add(courseCombo, 1, 0);
        form.add(groupLabel, 0, 1);
        form.add(groupCombo, 1, 1);
        form.add(teacherLabel, 0, 2);
        form.add(teacherCombo, 1, 2);
        form.add(roomLabel, 0, 3);
        form.add(roomField, 1, 3);
        form.add(dayLabel, 0, 4);
        form.add(dayCombo, 1, 4);
        form.add(startLabel, 0, 5);
        form.add(startTimeField, 1, 5);
        form.add(endLabel, 0, 6);
        form.add(endTimeField, 1, 6);
        form.add(typeLabel, 0, 7);
        form.add(typeCombo, 1, 7);
        form.add(semesterLabel, 0, 8);
        form.add(semesterField, 1, 8);
        form.add(addBtn, 1, 9);

        return form;
    }

    private GridPane createCheckForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label idLabel = new Label("ID расписания для проверки:");
        TextField scheduleIdField = new TextField();
        scheduleIdField.setPromptText("Введите ID");

        Button checkBtn = new Button("Проверить конфликты");
        checkBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        checkBtn.setOnAction(e -> checkConflicts(scheduleIdField.getText().trim()));

        form.add(idLabel, 0, 0);
        form.add(scheduleIdField, 1, 0);
        form.add(checkBtn, 1, 1);

        return form;
    }

    private void loadCourses() {
        Request request = new Request("GET_ALL_COURSES", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> courses = (List<String>) response.getData();
            courseCombo.getItems().clear();
            courseCombo.getItems().addAll(courses);
        }
    }

    private void loadGroups() {
        Request request = new Request("GET_ALL_GROUPS", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> groups = (List<String>) response.getData();
            groupCombo.getItems().clear();
            groupCombo.getItems().addAll(groups);
        }
    }

    private void loadTeachers() {
        Request request = new Request("GET_ALL_TEACHERS", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> teachers = (List<String>) response.getData();
            teacherCombo.getItems().clear();
            teacherCombo.getItems().addAll(teachers);
        }
    }

    private void loadSchedules() {
        Request request = new Request("GET_ALL_SCHEDULES_FOR_LIST", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> schedules = (List<String>) response.getData();
            scheduleList.getItems().clear();
            scheduleList.getItems().addAll(schedules);
        } else {
            scheduleList.getItems().clear();
            scheduleList.getItems().add("Не удалось загрузить расписание");
        }
    }

    private void addSchedule() {
        String course = courseCombo.getValue();
        String group = groupCombo.getValue();
        String teacher = teacherCombo.getValue();
        String room = roomField.getText().trim();
        String day = dayCombo.getValue();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();
        String type = typeCombo.getValue();
        String semester = semesterField.getText().trim();

        if (course == null || group == null || teacher == null || room.isEmpty() ||
                day == null || startTime.isEmpty() || endTime.isEmpty() || type == null) {
            resultArea.setText("❌ Ошибка: Заполните все поля");
            return;
        }

        Request request = new Request("ADD_SCHEDULE", new Object[]{
                course, group, teacher, room, day, startTime, endTime, type, semester
        });
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Занятие успешно добавлено!");
            roomField.clear();
            startTimeField.clear();
            endTimeField.clear();
            loadSchedules();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void checkConflicts(String scheduleIdText) {
        if (scheduleIdText.isEmpty()) {
            resultArea.setText("❌ Введите ID расписания для проверки");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(scheduleIdText);
        } catch (NumberFormatException e) {
            resultArea.setText("❌ ID должен быть числом");
            return;
        }

        Request request = new Request("VALIDATE_SCHEDULE", new Object[]{id});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            var result = (server.service.EducationService.ScheduleValidationResult) response.getData();

            StringBuilder sb = new StringBuilder();
            sb.append("Результат проверки расписания ID: ").append(id).append("\n");
            sb.append("Действительно: ").append(result.valid ? "✅ ДА" : "❌ НЕТ").append("\n");
            sb.append("Сообщение: ").append(result.message).append("\n");

            if (!result.conflicts.isEmpty()) {
                sb.append("\nКонфликты:\n");
                for (String conflict : result.conflicts) {
                    sb.append("  - ").append(conflict).append("\n");
                }
            }

            resultArea.setText(sb.toString());
        } else {
            resultArea.setText("❌ Ошибка проверки: " + response.getMessage());
        }
    }

    private void deleteSelectedSchedule() {
        String selected = scheduleList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resultArea.setText("❌ Пожалуйста, выберите занятие для удаления");
            return;
        }

        int id = Integer.parseInt(selected.split(":")[0].trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить это занятие?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Request request = new Request("DELETE_SCHEDULE", new Object[]{id});
            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                resultArea.setText("✅ Занятие успешно удалено!");
                loadSchedules();
            } else {
                resultArea.setText("❌ Ошибка: " + response.getMessage());
            }
        }
    }
}