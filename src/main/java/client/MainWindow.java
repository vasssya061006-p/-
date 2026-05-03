package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import server.model.User;
import server.network.Request;
import server.network.Response;
import java.util.List;

import java.util.Arrays;

/**
 * Main application window with role-based access control.
 * Provides access to all 12+ use cases based on user role.
 */
public class MainWindow {
    private ServerConnection connection;
    private User currentUser;
    private Stage stage;

    // UI Components
    private BorderPane root;
    private Label userLabel;
    private TabPane tabPane;

    public MainWindow(ServerConnection connection, User currentUser) {
        this.connection = connection;
        this.currentUser = currentUser;
    }

    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Система образования - " + currentUser.getFullName() + " (" + translateRole(currentUser.getRole()) + ")");

        root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top bar with user info and logout
        VBox topBar = createTopBar();
        root.setTop(topBar);

        // Tab pane with role-based tabs
        tabPane = createTabPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/client/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    private String translateRole(String role) {
        switch (role) {
            case "ADMIN": return "Администратор";
            case "TEACHER": return "Преподаватель";
            case "STUDENT": return "Студент";
            default: return role;
        }
    }

    private VBox createTopBar() {
        VBox topBar = new VBox(10);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        // User info with avatar
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Аватар (круг с инициалами)
        Label avatar = createAvatar(currentUser.getFullName());

        // Приветствие по времени
        String greeting = getGreeting();
        userLabel = new Label(greeting + ", " + currentUser.getFullName() + "!");
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label roleLabel = new Label("Роль: " + translateRole(currentUser.getRole()));
        roleLabel.setStyle("-fx-background-color: #7eb6e0; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 15;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопка переключения темы
        Button themeToggleBtn = new Button("🌙");
        themeToggleBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        themeToggleBtn.setOnAction(e -> toggleTheme());

        Button logoutButton = new Button("🚪 Выход");
        logoutButton.setStyle("-fx-background-color: #e89f9f; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;");
        logoutButton.setOnAction(e -> logout());

        userInfo.getChildren().addAll(avatar, userLabel, roleLabel, spacer, themeToggleBtn, logoutButton);

        Label welcomeLabel = new Label("Добро пожаловать в систему управления образованием");
        welcomeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        topBar.getChildren().addAll(userInfo, new Separator(), welcomeLabel);
        return topBar;
    }

    private Label createAvatar(String fullName) {
        // Берём первые буквы слов
        String[] parts = fullName.split(" ");
        String initials;
        if (parts.length >= 2) {
            initials = String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0));
        } else {
            initials = String.valueOf(fullName.charAt(0));
        }
        initials = initials.toUpperCase();

        Label avatar = new Label(initials);
        avatar.setMinWidth(40);
        avatar.setMinHeight(40);
        avatar.setMaxWidth(40);
        avatar.setMaxHeight(40);
        avatar.setStyle(
                "-fx-background-radius: 20; " +
                        "-fx-background-color: linear-gradient(to bottom, #7eb6e0, #5a9ecf); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-alignment: center; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);"
        );
        return avatar;
    }

    private String getGreeting() {
        java.time.LocalTime now = java.time.LocalTime.now();
        if (now.isBefore(java.time.LocalTime.NOON)) {
            return "Доброе утро";
        } else if (now.isBefore(java.time.LocalTime.of(18, 0))) {
            return "Добрый день";
        } else {
            return "Добрый вечер";
        }
    }

    private void toggleTheme() {
        Scene scene = stage.getScene();
        if (scene.getRoot().getStyleClass().contains("dark-theme")) {
            scene.getRoot().getStyleClass().remove("dark-theme");
            Toast.show(stage, "🌞 Светлая тема включена", "info");
        } else {
            scene.getRoot().getStyleClass().add("dark-theme");
            Toast.show(stage, "🌙 Тёмная тема включена", "info");
        }
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Common tab - My Profile
        tabPane.getTabs().add(createProfileTab());

        // Role-specific tabs
        switch (currentUser.getRole()) {
            case "STUDENT":
                tabPane.getTabs().add(createStudentGradesTab());
                tabPane.getTabs().add(createStudentAttendanceTab());
                tabPane.getTabs().add(createStudentEligibilityTab());
                break;

            case "TEACHER":
                tabPane.getTabs().add(createTeacherGradesTab());
                tabPane.getTabs().add(createTeacherAnalyticsTab());
                tabPane.getTabs().add(createTeacherAlertsTab());
                break;

            case "ADMIN":
                tabPane.getTabs().add(createAdminAnalyticsTab());
                tabPane.getTabs().add(createAdminGroupManagementTab());
                tabPane.getTabs().add(createAdminCourseManagementTab());
                tabPane.getTabs().add(createAdminStudentManagementTab());
                tabPane.getTabs().add(createAdminTeacherManagementTab());
                tabPane.getTabs().add(createAdminScheduleTab());
                tabPane.getTabs().add(createAdminArchiveTab());
                tabPane.getTabs().add(createAdminReportsTab());
                break;
        }

        return tabPane;
    }

    // =========================================================================
    // TAB: Profile (All users)
    // =========================================================================
    private Tab createProfileTab() {
        Tab tab = new Tab("Мой профиль");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                createInfoField("ФИО:", currentUser.getFullName()),
                createInfoField("Логин:", currentUser.getLogin()),
                createInfoField("Роль:", translateRole(currentUser.getRole()))
        );

        if (currentUser.getGroupId() != null) {
            content.getChildren().add(createInfoField("ID группы:", currentUser.getGroupId().toString()));
        }

        tab.setContent(content);
        return tab;
    }

    private HBox createInfoField(String label, String value) {
        HBox box = new HBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-min-width: 100px;");
        TextField field = new TextField(value);
        field.setEditable(false);
        HBox.setHgrow(field, Priority.ALWAYS);
        box.getChildren().addAll(lbl, field);
        return box;
    }

    // =========================================================================
    // TAB: Student - My Grades
    // =========================================================================
    private Tab createStudentGradesTab() {
        Tab tab = new Tab("Мои оценки");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Button refreshBtn = new Button("Обновить оценки");
        refreshBtn.setOnAction(e -> loadStudentGrades(content));

        Button gpaBtn = new Button("Рассчитать GPA");
        gpaBtn.setOnAction(e -> calculateGPA(content));

        HBox buttonBar = new HBox(10);
        buttonBar.getChildren().addAll(refreshBtn, gpaBtn);

        content.getChildren().addAll(buttonBar, new Label("Загрузка оценок..."));
        tab.setContent(content);

        // Auto-load
        loadStudentGrades(content);

        return tab;
    }

    private void loadStudentGrades(VBox content) {
        Request request = new Request("GENERATE_GRADE_REPORT", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            var result = (server.service.EducationService.GradeReportResult) response.getData();

            TextArea reportArea = new TextArea(result.report);
            reportArea.setEditable(false);
            reportArea.setStyle("-fx-font-family: monospace;");

            content.getChildren().clear();
            Button refreshBtn = new Button("Обновить оценки");
            refreshBtn.setOnAction(e -> loadStudentGrades(content));

            Button gpaBtn = new Button("Рассчитать GPA");
            gpaBtn.setOnAction(e -> calculateGPA(content));

            HBox buttonBar = new HBox(10);
            buttonBar.getChildren().addAll(refreshBtn, gpaBtn);

            String avgText = (result.recordCount > 0) ?
                    String.format("Средний балл: %.2f", result.averageGrade) :
                    "Оценок пока нет";
            Label avgLabel = new Label(avgText);
            avgLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            content.getChildren().addAll(buttonBar, avgLabel, reportArea);
        } else {
            content.getChildren().clear();
            Button refreshBtn = new Button("Обновить оценки");
            refreshBtn.setOnAction(e -> loadStudentGrades(content));

            Button gpaBtn = new Button("Рассчитать GPA");
            gpaBtn.setOnAction(e -> calculateGPA(content));

            HBox buttonBar = new HBox(10);
            buttonBar.getChildren().addAll(refreshBtn, gpaBtn);

            Label infoLabel = new Label("У этого студента пока нет оценок.");
            infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

            content.getChildren().addAll(buttonBar, infoLabel);
        }
    }

    private void calculateGPA(VBox content) {
        Request request = new Request("CALCULATE_GPA", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            var result = (server.service.EducationService.GPACalculatorResult) response.getData();

            if (result.totalGrades == 0) {
                showAlert("Расчёт GPA", "Нет оценок для расчёта GPA.");
                return;
            }

            String message = String.format(
                    "GPA: %.2f\nВсего оценок: %d\nЭкзаменов: %d\nТестов: %d\nКурсовых: %d\n\nСредние по курсам:\n%s",
                    result.averageGrade, result.totalGrades, result.examCount,
                    result.testCount, result.courseworkCount,
                    result.courseAverages.entrySet().stream()
                            .map(e -> "  " + e.getKey() + ": " + String.format("%.2f", e.getValue()))
                            .reduce("", (a, b) -> a + b + "\n", (a, b) -> a + b)
            );

            showAlert("Расчёт GPA", message);
        } else {
            showAlert("Расчёт GPA", "Нет оценок для расчёта GPA.");
        }
    }

    // =========================================================================
    // TAB: Student - Attendance
    // =========================================================================
    private Tab createStudentAttendanceTab() {
        Tab tab = new Tab("Моя посещаемость");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Button loadBtn = new Button("Загрузить статистику посещаемости");
        loadBtn.setOnAction(e -> loadAttendance(content));

        content.getChildren().addAll(loadBtn, new Label("Нажмите для загрузки данных о посещаемости"));
        tab.setContent(content);

        return tab;
    }

    private void loadAttendance(VBox content) {
        Request request = new Request("GET_ATTENDANCE_SUMMARY", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            var result = (server.service.EducationService.AttendanceSummaryResult) response.getData();

            String message = String.format(
                    "Сводка посещаемости\n\n" +
                            "Всего занятий: %d\n" +
                            "Присутствовал: %d\n" +
                            "Отсутствовал: %d\n" +
                            "Пропуск по уваж. причине: %d\n" +
                            "Процент пропусков: %.2f%%\n" +
                            "Уровень риска: %s",
                    result.totalSessions, result.present, result.absent,
                    result.excused, result.absenceRate, result.riskLevel
            );

            TextArea area = new TextArea(message);
            area.setEditable(false);

            content.getChildren().clear();
            Button loadBtn = new Button("Загрузить статистику посещаемости");
            loadBtn.setOnAction(e -> loadAttendance(content));
            content.getChildren().addAll(loadBtn, area);
        } else {
            content.getChildren().clear();
            Button loadBtn = new Button("Загрузить статистику посещаемости");
            loadBtn.setOnAction(e -> loadAttendance(content));
            Label infoLabel = new Label("Данные о посещаемости пока отсутствуют.");
            infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            content.getChildren().addAll(loadBtn, infoLabel);
        }
    }

    // =========================================================================
    // TAB: Student - Session Eligibility
    // =========================================================================
    private Tab createStudentEligibilityTab() {
        Tab tab = new Tab("Допуск к сессии");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Button checkBtn = new Button("Проверить допуск");
        checkBtn.setOnAction(e -> checkEligibility(content));

        content.getChildren().addAll(checkBtn, new Label("Проверьте, допущены ли вы к экзаменационной сессии"));
        tab.setContent(content);

        return tab;
    }

    private void checkEligibility(VBox content) {
        Request request = new Request("CHECK_SESSION_ELIGIBILITY", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            var result = (server.service.EducationService.SessionEligibilityResult) response.getData();

            String status = result.eligible ? "✓ ДОПУЩЕН" : "✗ НЕ ДОПУЩЕН";
            String message = String.format(
                    "Допуск к сессии: %s\n\n" +
                            "Средний балл: %.2f\n" +
                            "Количество пропусков: %d\n" +
                            "Всего записей: %d\n",
                    status, result.averageGrade, result.absenceCount, result.totalRecords
            );

            if (!result.issues.isEmpty()) {
                message += "\nПроблемы:\n" + String.join("\n", result.issues);
            }

            TextArea area = new TextArea(message);
            area.setEditable(false);

            content.getChildren().clear();
            Button checkBtn = new Button("Проверить допуск");
            checkBtn.setOnAction(e -> checkEligibility(content));
            content.getChildren().addAll(checkBtn, area);
        } else {
            showAlert("Ошибка", "Не удалось проверить допуск: " + response.getMessage());
        }
    }

    // =========================================================================
    // TAB: Teacher - Grade Management
    // =========================================================================
    private Tab createTeacherGradesTab() {
        Tab tab = new Tab("Управление оценками");
        TeacherGradeManagementTab gradeTab = new TeacherGradeManagementTab(connection, currentUser);
        tab.setContent(gradeTab.createContent());
        return tab;
    }

    // =========================================================================
    // TAB: Teacher - Analytics
    // =========================================================================
    private Tab createTeacherAnalyticsTab() {
        Tab tab = new Tab("Аналитика группы");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Аналитика успеваемости группы");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox selectionBox = new HBox(10);
        Label groupLabel = new Label("Выберите группу:");
        ComboBox<String> groupCombo = new ComboBox<>();
        groupCombo.setPromptText("Выберите группу");
        groupCombo.setPrefWidth(200);

        Button loadBtn = new Button("Загрузить аналитику");
        loadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        selectionBox.getChildren().addAll(groupLabel, groupCombo, loadBtn);

        TextArea analyticsArea = new TextArea();
        analyticsArea.setEditable(false);
        analyticsArea.setPrefHeight(400);
        analyticsArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        content.getChildren().addAll(title, selectionBox, new Separator(), analyticsArea);
        tab.setContent(content);

        loadTeacherGroups(groupCombo);

        loadBtn.setOnAction(e -> {
            String group = groupCombo.getValue();
            if (group == null) {
                showAlert("Ошибка", "Пожалуйста, выберите группу");
                return;
            }
            loadGroupAnalytics(group, analyticsArea);
        });

        return tab;
    }

    private void loadTeacherGroups(ComboBox<String> groupCombo) {
        Request request = new Request("GET_TEACHER_GROUPS", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> groups = (List<String>) response.getData();
            groupCombo.getItems().clear();
            groupCombo.getItems().addAll(groups);
        }
    }

    private void loadGroupAnalytics(String groupName, TextArea analyticsArea) {
        Request request = new Request("GET_GROUP_PERFORMANCE", new Object[]{groupName});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            String analytics = (String) response.getData();
            analyticsArea.setText(analytics);
        } else {
            analyticsArea.setText("Не удалось загрузить аналитику: " + response.getMessage());
        }
    }

    // =========================================================================
    // TAB: Teacher - Alerts
    // =========================================================================
    private Tab createTeacherAlertsTab() {
        Tab tab = new Tab("Оповещения о низких оценках");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Button loadAlerts = new Button("Загрузить оповещения");
        loadAlerts.setOnAction(e -> loadAlerts(content));

        content.getChildren().addAll(loadAlerts, new Label("Нажмите для просмотра студентов с неудовлетворительными оценками"));
        tab.setContent(content);

        return tab;
    }

    private void loadAlerts(VBox content) {
        Request request = new Request("GET_LOW_GRADE_ALERTS", new Object[]{3.0});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            var alerts = (java.util.List<server.service.EducationService.AlertResult>) response.getData();

            StringBuilder sb = new StringBuilder("Оповещения о низких оценках\n\n");
            if (alerts.isEmpty()) {
                sb.append("Нет студентов с неудовлетворительными оценками. Отличная работа!");
            } else {
                for (var alert : alerts) {
                    sb.append(alert.message).append("\n\n");
                }
            }

            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);

            content.getChildren().clear();
            Button loadAlerts = new Button("Загрузить оповещения");
            loadAlerts.setOnAction(e -> loadAlerts(content));
            content.getChildren().addAll(loadAlerts, area);
        } else {
            content.getChildren().clear();
            Button loadAlerts = new Button("Загрузить оповещения");
            loadAlerts.setOnAction(e -> loadAlerts(content));
            Label errorLabel = new Label("Не удалось загрузить оповещения: " + response.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            content.getChildren().addAll(loadAlerts, errorLabel);
        }
    }

    // =========================================================================
    // TAB: Admin - Analytics
    // =========================================================================
    private Tab createAdminAnalyticsTab() {
        Tab tab = new Tab("Институциональная аналитика");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Button exportBtn = new Button("Экспортировать аналитический отчёт");
        exportBtn.setOnAction(e -> exportAnalytics(content));

        content.getChildren().addAll(exportBtn, new Label("Сформировать комплексный институциональный отчёт"));
        tab.setContent(content);

        return tab;
    }

    private void exportAnalytics(VBox content) {
        Request request = new Request("EXPORT_ANALYTICS", new Object[]{});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            var result = (server.service.EducationService.AnalyticsExportResult) response.getData();

            TextArea area = new TextArea(result.report);
            area.setEditable(false);

            content.getChildren().clear();
            Button exportBtn = new Button("Экспортировать аналитический отчёт");
            exportBtn.setOnAction(e -> exportAnalytics(content));
            content.getChildren().addAll(exportBtn, area);
        } else {
            showAlert("Ошибка", "Не удалось экспортировать аналитику: " + response.getMessage());
        }
    }

    // =========================================================================
    // TAB: Admin - Schedule
    // =========================================================================
    private Tab createAdminScheduleTab() {
        Tab tab = new Tab("Управление расписанием");
        AdminScheduleTab scheduleTab = new AdminScheduleTab(connection);
        tab.setContent(scheduleTab.createContent());
        return tab;
    }

    // =========================================================================
    // TAB: Admin - Archive
    // =========================================================================
    private Tab createAdminArchiveTab() {
        Tab tab = new Tab("Архив записей");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label instruction = new Label("Архивация записей за завершённый семестр");
        TextField semesterField = new TextField();
        semesterField.setPromptText("Введите семестр (например, 2025-2026-fall)");

        Button archiveBtn = new Button("Архивировать записи");
        archiveBtn.setOnAction(e -> {
            String semester = semesterField.getText().trim();
            if (semester.isEmpty()) {
                showAlert("Ошибка", "Пожалуйста, введите семестр");
                return;
            }
            archiveRecords(content, semester);
        });

        content.getChildren().addAll(instruction, semesterField, archiveBtn);
        tab.setContent(content);

        return tab;
    }

    private void archiveRecords(VBox content, String semester) {
        Request request = new Request("ARCHIVE_RECORDS", new Object[]{semester});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            var result = (server.service.EducationService.ArchiveResult) response.getData();
            showAlert("Результат архивации", result.summary);
        } else {
            showAlert("Ошибка", "Не удалось архивировать записи: " + response.getMessage());
        }
    }

    // =========================================================================
    // TAB: Admin - Reports
    // =========================================================================
    private Tab createAdminReportsTab() {
        Tab tab = new Tab("Отчёты");
        AdminReportsTab reportsTab = new AdminReportsTab(connection);
        tab.setContent(reportsTab.createContent());
        return tab;
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================
    private void logout() {
        try {
            Request request = new Request("LOGOUT", null);
            request.setUserId(currentUser.getId());
            connection.sendRequest(request);
        } catch (Exception e) {
            System.err.println("Ошибка при выходе: " + e.getMessage());
        }

        connection.disconnect();

        try {
            stage.close();
            LoginWindow loginWindow = new LoginWindow();
            Stage loginStage = new Stage();
            loginWindow.start(loginStage);
        } catch (Exception e) {
            System.err.println("Ошибка при открытии окна входа: " + e.getMessage());
            System.exit(0);
        }
    }

    private void showAlert(String title, String message) {
        String type = "info";
        if (title.equals("Успех")) type = "success";
        else if (title.equals("Ошибка")) type = "error";
        Toast.show(stage.getScene().getWindow(), message, type);
    }

    private Tab createAdminGroupManagementTab() {
        Tab tab = new Tab("Управление группами");
        AdminGroupManagementTab groupTab = new AdminGroupManagementTab(connection);
        tab.setContent(groupTab.createContent());
        return tab;
    }

    private Tab createAdminCourseManagementTab() {
        Tab tab = new Tab("Управление курсами");
        AdminCourseManagementTab courseTab = new AdminCourseManagementTab(connection);
        tab.setContent(courseTab.createContent());
        return tab;
    }

    private Tab createAdminStudentManagementTab() {
        Tab tab = new Tab("Управление студентами");
        AdminStudentManagementTab studentTab = new AdminStudentManagementTab(connection);
        tab.setContent(studentTab.createContent());
        return tab;
    }

    private Tab createAdminTeacherManagementTab() {
        Tab tab = new Tab("Управление преподавателями");
        AdminTeacherManagementTab teacherTab = new AdminTeacherManagementTab(connection);
        tab.setContent(teacherTab.createContent());
        return tab;
    }
}