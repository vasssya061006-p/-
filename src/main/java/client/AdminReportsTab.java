package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.network.Request;
import server.network.Response;

public class AdminReportsTab {

    private ServerConnection connection;
    private VBox content;
    private TextArea reportArea;

    public AdminReportsTab(ServerConnection connection) {
        this.connection = connection;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Генерация отчётов");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Кнопки для разных отчётов
        GridPane buttonsPanel = new GridPane();
        buttonsPanel.setHgap(15);
        buttonsPanel.setVgap(10);

        // Отчёт 1: Успеваемость по группам
        Button groupPerformanceBtn = new Button("📊 Успеваемость по группам");
        groupPerformanceBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-pref-width: 220;");
        groupPerformanceBtn.setOnAction(e -> generateGroupPerformanceReport());

        // Отчёт 2: Нагрузка преподавателей
        Button teacherLoadBtn = new Button("👨‍🏫 Нагрузка преподавателей");
        teacherLoadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-pref-width: 220;");
        teacherLoadBtn.setOnAction(e -> generateTeacherLoadReport());

        // Отчёт 3: Студенты с низкими оценками
        Button lowGradesBtn = new Button("⚠️ Студенты с низкими оценками");
        lowGradesBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 220;");
        lowGradesBtn.setOnAction(e -> generateLowGradesReport());

        // Отчёт 4: Посещаемость по группам
        Button attendanceBtn = new Button("📋 Посещаемость по группам");
        attendanceBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 220;");
        attendanceBtn.setOnAction(e -> generateAttendanceReport());

        // Отчёт 5: Полная аналитика
        Button fullAnalyticsBtn = new Button("📈 Полная аналитика института");
        fullAnalyticsBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-pref-width: 220;");
        fullAnalyticsBtn.setOnAction(e -> generateFullAnalytics());

        buttonsPanel.add(groupPerformanceBtn, 0, 0);
        buttonsPanel.add(teacherLoadBtn, 1, 0);
        buttonsPanel.add(lowGradesBtn, 0, 1);
        buttonsPanel.add(attendanceBtn, 1, 1);
        buttonsPanel.add(fullAnalyticsBtn, 0, 2);

        // Область для вывода отчёта
        Label reportLabel = new Label("Результат отчёта:");
        reportLabel.setStyle("-fx-font-weight: bold;");

        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(400);
        reportArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        // Кнопка экспорта
        Button exportBtn = new Button("💾 Сохранить отчёт в файл");
        exportBtn.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        exportBtn.setOnAction(e -> exportReportToFile());

        HBox exportBox = new HBox(10);
        exportBox.getChildren().add(exportBtn);

        content.getChildren().addAll(
                title, buttonsPanel, new Separator(),
                reportLabel, reportArea, exportBox
        );

        return content;
    }

    private void generateGroupPerformanceReport() {
        reportArea.setText("🔄 Загрузка отчёта об успеваемости групп...");

        Request request = new Request("REPORT_GROUP_PERFORMANCE", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            reportArea.setText((String) response.getData());
        } else {
            reportArea.setText("❌ Ошибка загрузки отчёта:\n" + response.getMessage());
        }
    }

    private void generateTeacherLoadReport() {
        reportArea.setText("🔄 Загрузка отчёта о нагрузке преподавателей...");

        Request request = new Request("REPORT_TEACHER_LOAD", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            reportArea.setText((String) response.getData());
        } else {
            reportArea.setText("❌ Ошибка загрузки отчёта:\n" + response.getMessage());
        }
    }

    private void generateLowGradesReport() {
        reportArea.setText("🔄 Поиск студентов с низкими оценками...");

        Request request = new Request("REPORT_LOW_GRADES", new Object[]{3.0});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            reportArea.setText((String) response.getData());
        } else {
            reportArea.setText("❌ Ошибка загрузки отчёта:\n" + response.getMessage());
        }
    }

    private void generateAttendanceReport() {
        reportArea.setText("🔄 Загрузка отчёта о посещаемости...");

        Request request = new Request("REPORT_ATTENDANCE", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            reportArea.setText((String) response.getData());
        } else {
            reportArea.setText("❌ Ошибка загрузки отчёта:\n" + response.getMessage());
        }
    }

    private void generateFullAnalytics() {
        reportArea.setText("🔄 Генерация полной аналитики института...");

        Request request = new Request("REPORT_FULL_ANALYTICS", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            reportArea.setText((String) response.getData());
        } else {
            reportArea.setText("❌ Ошибка загрузки отчёта:\n" + response.getMessage());
        }
    }

    private void exportReportToFile() {
        String reportText = reportArea.getText();
        if (reportText.isEmpty() || reportText.startsWith("❌") || reportText.startsWith("🔄")) {
            showAlert("Ошибка", "Нет данных для сохранения. Сначала сгенерируйте отчёт.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить отчёт");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Текстовые файлы", "*.txt")
        );
        fileChooser.setInitialFileName("report_" + System.currentTimeMillis() + ".txt");

        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.print(reportText);
                showAlert("Успех", "Отчёт сохранён в файл:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось сохранить файл: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}