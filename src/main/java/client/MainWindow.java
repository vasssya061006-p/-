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
        stage.setTitle("Education System - " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        
        root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Top bar with user info and logout
        VBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Tab pane with role-based tabs
        tabPane = createTabPane();
        root.setCenter(tabPane);
        
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }
    
    private VBox createTopBar() {
        VBox topBar = new VBox(10);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        
        // User info
        HBox userInfo = new HBox(10);
        userLabel = new Label("User: " + currentUser.getFullName());
        userLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label roleLabel = new Label("Role: " + currentUser.getRole());
        roleLabel.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> logout());
        
        userInfo.getChildren().addAll(userLabel, roleLabel, spacer, logoutButton);
        
        // Welcome message
        Label welcomeLabel = new Label("Welcome to Education Management System");
        welcomeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        topBar.getChildren().addAll(userInfo, new Separator(), welcomeLabel);
        return topBar;
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
        Tab tab = new Tab("My Profile");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        content.getChildren().addAll(
            createInfoField("Full Name:", currentUser.getFullName()),
            createInfoField("Login:", currentUser.getLogin()),
            createInfoField("Role:", currentUser.getRole())
        );
        
        if (currentUser.getGroupId() != null) {
            content.getChildren().add(createInfoField("Group ID:", currentUser.getGroupId().toString()));
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
        Tab tab = new Tab("My Grades");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button refreshBtn = new Button("Refresh Grades");
        refreshBtn.setOnAction(e -> loadStudentGrades(content));
        
        Button gpaBtn = new Button("Calculate GPA");
        gpaBtn.setOnAction(e -> calculateGPA(content));
        
        HBox buttonBar = new HBox(10);
        buttonBar.getChildren().addAll(refreshBtn, gpaBtn);
        
        content.getChildren().addAll(buttonBar, new Label("Loading grades..."));
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
            
            // Remove old content and add new
            content.getChildren().clear();
            Button refreshBtn = new Button("Refresh Grades");
            refreshBtn.setOnAction(e -> loadStudentGrades(content));
            
            Button gpaBtn = new Button("Calculate GPA");
            gpaBtn.setOnAction(e -> calculateGPA(content));
            
            HBox buttonBar = new HBox(10);
            buttonBar.getChildren().addAll(refreshBtn, gpaBtn);
            
            Label avgLabel = new Label(String.format("Average Grade: %.2f", result.averageGrade));
            avgLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            content.getChildren().addAll(buttonBar, avgLabel, reportArea);
        } else {
            showAlert("Error", "Failed to load grades: " + response.getMessage());
        }
    }
    
    private void calculateGPA(VBox content) {
        Request request = new Request("CALCULATE_GPA", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);
        
        if (response.isSuccess()) {
            var result = (server.service.EducationService.GPACalculatorResult) response.getData();
            
            String message = String.format(
                "GPA: %.2f\nTotal Grades: %d\nExams: %d\nTests: %d\nCoursework: %d\n\nCourse Averages:\n%s",
                result.averageGrade, result.totalGrades, result.examCount, 
                result.testCount, result.courseworkCount,
                result.courseAverages.entrySet().stream()
                    .map(e -> "  " + e.getKey() + ": " + String.format("%.2f", e.getValue()))
                    .reduce("", (a, b) -> a + b + "\n", (a, b) -> a + b)
            );
            
            showAlert("GPA Calculation", message);
        } else {
            showAlert("Error", "Failed to calculate GPA: " + response.getMessage());
        }
    }
    
    // =========================================================================
    // TAB: Student - Attendance
    // =========================================================================
    private Tab createStudentAttendanceTab() {
        Tab tab = new Tab("My Attendance");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button loadBtn = new Button("Load Attendance Summary");
        loadBtn.setOnAction(e -> loadAttendance(content));
        
        content.getChildren().addAll(loadBtn, new Label("Click to load attendance data"));
        tab.setContent(content);
        
        return tab;
    }
    
    private void loadAttendance(VBox content) {
        Request request = new Request("GET_ATTENDANCE_SUMMARY", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);
        
        if (response.isSuccess()) {
            var result = (server.service.EducationService.AttendanceSummaryResult) response.getData();
            
            String message = String.format(
                "Attendance Summary\n\n" +
                "Total Sessions: %d\n" +
                "Present: %d\n" +
                "Absent: %d\n" +
                "Excused: %d\n" +
                "Absence Rate: %.2f%%\n" +
                "Risk Level: %s",
                result.totalSessions, result.present, result.absent, 
                result.excused, result.absenceRate, result.riskLevel
            );
            
            TextArea area = new TextArea(message);
            area.setEditable(false);
            
            content.getChildren().clear();
            Button loadBtn = new Button("Load Attendance Summary");
            loadBtn.setOnAction(e -> loadAttendance(content));
            content.getChildren().addAll(loadBtn, area);
        } else {
            showAlert("Error", "Failed to load attendance: " + response.getMessage());
        }
    }
    
    // =========================================================================
    // TAB: Student - Session Eligibility
    // =========================================================================
    private Tab createStudentEligibilityTab() {
        Tab tab = new Tab("Session Eligibility");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button checkBtn = new Button("Check Eligibility");
        checkBtn.setOnAction(e -> checkEligibility(content));
        
        content.getChildren().addAll(checkBtn, new Label("Check if you are eligible for exam session"));
        tab.setContent(content);
        
        return tab;
    }
    
    private void checkEligibility(VBox content) {
        Request request = new Request("CHECK_SESSION_ELIGIBILITY", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);
        
        if (response.isSuccess()) {
            var result = (server.service.EducationService.SessionEligibilityResult) response.getData();
            
            String status = result.eligible ? "✓ ELIGIBLE" : "✗ NOT ELIGIBLE";
            String message = String.format(
                "Session Eligibility: %s\n\n" +
                "Average Grade: %.2f\n" +
                "Absence Count: %d\n" +
                "Total Records: %d\n",
                status, result.averageGrade, result.absenceCount, result.totalRecords
            );
            
            if (!result.issues.isEmpty()) {
                message += "\nIssues:\n" + String.join("\n", result.issues);
            }
            
            TextArea area = new TextArea(message);
            area.setEditable(false);
            
            content.getChildren().clear();
            Button checkBtn = new Button("Check Eligibility");
            checkBtn.setOnAction(e -> checkEligibility(content));
            content.getChildren().addAll(checkBtn, area);
        } else {
            showAlert("Error", "Failed to check eligibility: " + response.getMessage());
        }
    }
    
    // =========================================================================
    // TAB: Teacher - Grade Management
    // =========================================================================
    private Tab createTeacherGradesTab() {
        Tab tab = new Tab("Grade Management");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("Use this tab to manage student grades and generate reports");
        content.getChildren().add(instruction);
        
        tab.setContent(content);
        return tab;
    }
    
    // =========================================================================
    // TAB: Teacher - Analytics
    // =========================================================================
    private Tab createTeacherAnalyticsTab() {
        Tab tab = new Tab("Group Analytics");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("View performance analytics for student groups");
        content.getChildren().add(instruction);
        
        tab.setContent(content);
        return tab;
    }
    
    // =========================================================================
    // TAB: Teacher - Alerts
    // =========================================================================
    private Tab createTeacherAlertsTab() {
        Tab tab = new Tab("Low Grade Alerts");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button loadAlerts = new Button("Load Alerts");
        loadAlerts.setOnAction(e -> loadAlerts(content));
        
        content.getChildren().addAll(loadAlerts, new Label("Click to view students with failing grades"));
        tab.setContent(content);
        
        return tab;
    }
    
    private void loadAlerts(VBox content) {
        Request request = new Request("GET_LOW_GRADE_ALERTS", new Object[]{3.0});
        Response response = connection.sendRequest(request);
        
        if (response.isSuccess()) {
            var alerts = (java.util.List<server.service.EducationService.AlertResult>) response.getData();
            
            StringBuilder sb = new StringBuilder("Low Grade Alerts\n\n");
            if (alerts.isEmpty()) {
                sb.append("No students with failing grades. Great job!");
            } else {
                for (var alert : alerts) {
                    sb.append(alert.message).append("\n\n");
                }
            }
            
            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);
            
            content.getChildren().clear();
            Button loadAlerts = new Button("Load Alerts");
            loadAlerts.setOnAction(e -> loadAlerts(content));
            content.getChildren().addAll(loadAlerts, area);
        } else {
            showAlert("Error", "Failed to load alerts: " + response.getMessage());
        }
    }
    
    // =========================================================================
    // TAB: Admin - Analytics
    // =========================================================================
    private Tab createAdminAnalyticsTab() {
        Tab tab = new Tab("Institutional Analytics");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button exportBtn = new Button("Export Analytics Report");
        exportBtn.setOnAction(e -> exportAnalytics(content));
        
        content.getChildren().addAll(exportBtn, new Label("Generate comprehensive institutional report"));
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
            Button exportBtn = new Button("Export Analytics Report");
            exportBtn.setOnAction(e -> exportAnalytics(content));
            content.getChildren().addAll(exportBtn, area);
        } else {
            showAlert("Error", "Failed to export analytics: " + response.getMessage());
        }
    }
    
    // =========================================================================
    // TAB: Admin - Schedule
    // =========================================================================
    private Tab createAdminScheduleTab() {
        Tab tab = new Tab("Schedule Management");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("Validate schedules for conflicts");
        content.getChildren().add(instruction);
        
        tab.setContent(content);
        return tab;
    }
    
    // =========================================================================
    // TAB: Admin - Archive
    // =========================================================================
    private Tab createAdminArchiveTab() {
        Tab tab = new Tab("Archive Records");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("Archive completed semester records");
        TextField semesterField = new TextField();
        semesterField.setPromptText("Enter semester (e.g., 2025-2026-fall)");
        
        Button archiveBtn = new Button("Archive Records");
        archiveBtn.setOnAction(e -> {
            String semester = semesterField.getText().trim();
            if (semester.isEmpty()) {
                showAlert("Error", "Please enter a semester");
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
            showAlert("Archive Result", result.summary);
        } else {
            showAlert("Error", "Failed to archive records: " + response.getMessage());
        }
    }
    
    // =========================================================================
    // TAB: Admin - Reports
    // =========================================================================
    private Tab createAdminReportsTab() {
        Tab tab = new Tab("Reports");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("Generate various reports and statistics");
        content.getChildren().add(instruction);
        
        tab.setContent(content);
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
            System.err.println("Error during logout: " + e.getMessage());
        }
        
        connection.disconnect();
        
        try {
            stage.close();
            LoginWindow loginWindow = new LoginWindow();
            Stage loginStage = new Stage();
            loginWindow.start(loginStage);
        } catch (Exception e) {
            System.err.println("Error reopening login window: " + e.getMessage());
            System.exit(0);
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setPrefSize(500, 400);
        alert.showAndWait();
    }
}
