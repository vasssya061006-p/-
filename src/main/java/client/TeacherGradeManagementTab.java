package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import server.model.User;
import server.network.Request;
import server.network.Response;

import java.time.LocalDate;
import java.util.List;

public class TeacherGradeManagementTab {

    private ServerConnection connection;
    private User currentUser;
    private VBox content;

    // UI Components
    private ComboBox<String> groupCombo;
    private ComboBox<String> courseCombo;
    private ListView<String> studentList;
    private TextArea gradeArea;
    private ComboBox<String> attendanceCombo; // добавлено

    public TeacherGradeManagementTab(ServerConnection connection, User currentUser) {
        this.connection = connection;
        this.currentUser = currentUser;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Управление оценками");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Selection panel
        GridPane selectionPanel = new GridPane();
        selectionPanel.setHgap(10);
        selectionPanel.setVgap(10);

        Label groupLabel = new Label("Группа:");
        groupCombo = new ComboBox<>();
        groupCombo.setPromptText("Выберите группу");
        groupCombo.setPrefWidth(200);

        Label courseLabel = new Label("Курс:");
        courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Выберите курс");
        courseCombo.setPrefWidth(200);

        Button loadBtn = new Button("Загрузить студентов");
        loadBtn.setOnAction(e -> loadStudents());

        selectionPanel.add(groupLabel, 0, 0);
        selectionPanel.add(groupCombo, 1, 0);
        selectionPanel.add(courseLabel, 0, 1);
        selectionPanel.add(courseCombo, 1, 1);
        selectionPanel.add(loadBtn, 2, 1);

        // Student list
        Label studentsLabel = new Label("Список студентов:");
        studentsLabel.setStyle("-fx-font-weight: bold;");
        studentList = new ListView<>();
        studentList.setPrefHeight(200);

        // Grade input panel
        GridPane gradePanel = new GridPane();
        gradePanel.setHgap(10);
        gradePanel.setVgap(10);

        Label studentLabel = new Label("Выбранный студент:");
        TextField selectedStudentField = new TextField();
        selectedStudentField.setEditable(false);
        selectedStudentField.setPrefWidth(200);
        selectedStudentField.setPromptText("Не выбран");

        Label gradeLabel = new Label("Оценка (0-10):");
        ComboBox<Integer> gradeCombo = new ComboBox<>();
        for (int i = 0; i <= 10; i++) {
            gradeCombo.getItems().add(i);
        }
        gradeCombo.setPromptText("Выберите оценку");

        Label typeLabel = new Label("Тип оценки:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("экзамен", "тест", "курсовая", "лабораторная", "проект", "практическая");
        typeCombo.setPromptText("Выберите тип");

        // НОВЫЙ БЛОК: статус посещаемости
        Label attendanceLabel = new Label("Статус посещаемости:");
        attendanceCombo = new ComboBox<>();
        attendanceCombo.getItems().addAll("присутствовал", "отсутствовал", "по уважительной причине");
        attendanceCombo.setPromptText("Выберите статус");
        attendanceCombo.setValue("присутствовал");

        Button submitBtn = new Button("Выставить оценку");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        gradePanel.add(studentLabel, 0, 0);
        gradePanel.add(selectedStudentField, 1, 0);
        gradePanel.add(gradeLabel, 0, 1);
        gradePanel.add(gradeCombo, 1, 1);
        gradePanel.add(typeLabel, 0, 2);
        gradePanel.add(typeCombo, 1, 2);
        gradePanel.add(attendanceLabel, 0, 3);      // добавлено
        gradePanel.add(attendanceCombo, 1, 3);     // добавлено
        gradePanel.add(submitBtn, 1, 4);           // изменена строка

        // Grade display area
        Label currentGradesLabel = new Label("Текущие оценки:");
        currentGradesLabel.setStyle("-fx-font-weight: bold;");
        gradeArea = new TextArea();
        gradeArea.setEditable(false);
        gradeArea.setPrefHeight(150);
        gradeArea.setStyle("-fx-font-family: monospace;");
        gradeArea.setPromptText("Выберите студента для просмотра оценок");

        // Connect button actions
        submitBtn.setOnAction(e -> {
            String selected = studentList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Ошибка", "Пожалуйста, выберите студента");
                return;
            }
            if (gradeCombo.getValue() == null) {
                showAlert("Ошибка", "Пожалуйста, выберите оценку");
                return;
            }
            if (typeCombo.getValue() == null) {
                showAlert("Ошибка", "Пожалуйста, выберите тип оценки");
                return;
            }
            if (attendanceCombo.getValue() == null) {
                showAlert("Ошибка", "Пожалуйста, выберите статус посещаемости");
                return;
            }

            // Parse student ID from selected string (format: "ID: Name")
            int studentId = Integer.parseInt(selected.split(":")[0].trim());
            String studentName = selected.split(":")[1].trim();
            String courseName = courseCombo.getValue();
            int gradeValue = gradeCombo.getValue();
            String gradeType = typeCombo.getValue();
            String attendanceStatus = attendanceCombo.getValue(); // добавлено
            String date = LocalDate.now().toString();

            submitGrade(studentId, studentName, courseName, gradeValue, gradeType, attendanceStatus, date); // изменён вызов
        });

        studentList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        String studentName = selected.split(":")[1].trim();
                        selectedStudentField.setText(studentName);
                        loadStudentGrades(selected);
                    }
                });

        content.getChildren().addAll(
                title, selectionPanel, new Separator(),
                studentsLabel, studentList,
                new Separator(), gradePanel,
                currentGradesLabel, gradeArea
        );

        // Load initial data
        loadGroups();
        loadCourses();

        return content;
    }

    private void loadGroups() {
        Request request = new Request("GET_TEACHER_GROUPS", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> groups = (List<String>) response.getData();
            groupCombo.getItems().clear();
            groupCombo.getItems().addAll(groups);
        } else {
            showAlert("Ошибка", "Не удалось загрузить группы: " + response.getMessage());
        }
    }

    private void loadCourses() {
        Request request = new Request("GET_TEACHER_COURSES", new Object[]{currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> courses = (List<String>) response.getData();
            courseCombo.getItems().clear();
            courseCombo.getItems().addAll(courses);
        } else {
            showAlert("Ошибка", "Не удалось загрузить курсы: " + response.getMessage());
        }
    }

    private void loadStudents() {
        String group = groupCombo.getValue();
        String course = courseCombo.getValue();

        if (group == null || course == null) {
            showAlert("Ошибка", "Пожалуйста, выберите группу и курс");
            return;
        }

        Request request = new Request("GET_STUDENTS_BY_GROUP_AND_COURSE",
                new Object[]{group, course, currentUser.getId()});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<Object[]> students = (List<Object[]>) response.getData();
            studentList.getItems().clear();
            for (Object[] s : students) {
                studentList.getItems().add(s[0] + ": " + s[1]);
            }
            if (students.isEmpty()) {
                gradeArea.setText("В этой группе нет студентов.");
            } else {
                gradeArea.setText("Выберите студента из списка.");
            }
        } else {
            showAlert("Ошибка", "Не удалось загрузить студентов: " + response.getMessage());
        }
    }

    private void loadStudentGrades(String studentInfo) {
        if (studentInfo == null) return;

        int studentId = Integer.parseInt(studentInfo.split(":")[0].trim());

        Request request = new Request("GET_STUDENT_GRADES", new Object[]{studentId});
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            String grades = (String) response.getData();
            if (grades.isEmpty() || grades.equals("No grades available for this student.")) {
                gradeArea.setText("У этого студента пока нет оценок.");
            } else {
                gradeArea.setText(grades);
            }
        } else {
            gradeArea.setText("Не удалось загрузить оценки: " + response.getMessage());
        }
    }

    private void submitGrade(int studentId, String studentName, String courseName,
                             int gradeValue, String gradeType, String attendanceStatus, String date) {
        Request request = new Request("ADD_GRADE", new Object[]{
                studentId, studentName, courseName, gradeValue, gradeType, date, attendanceStatus
        });
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            showAlert("Успех", "Оценка успешно выставлена!");
            // Refresh grades display
            String selected = studentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadStudentGrades(selected);
            }
        } else {
            showAlert("Ошибка", "Не удалось выставить оценку: " + response.getMessage());
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