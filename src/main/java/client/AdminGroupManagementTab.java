package client;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.network.Request;
import server.network.Response;
import java.util.List;

public class AdminGroupManagementTab {

    private ServerConnection connection;
    private VBox content;

    // UI Components
    private TextField nameField;
    private TextField yearField;
    private TextField curatorIdField;
    private ListView<String> groupList;
    private TextArea resultArea;
    private TextField editIdField;
    private TextField editNameField;
    private TextField editYearField;
    private TextField editCuratorField;

    public AdminGroupManagementTab(ServerConnection connection) {
        this.connection = connection;
    }

    public VBox createContent() {
        content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Управление группами");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ========== ФОРМА ДОБАВЛЕНИЯ ==========
        TitledPane addPane = new TitledPane("➕ Добавить группу", createAddForm());
        addPane.setCollapsible(true);

        // ========== ФОРМА РЕДАКТИРОВАНИЯ ==========
        TitledPane editPane = new TitledPane("✏️ Редактировать группу", createEditForm());
        editPane.setCollapsible(true);

        // ========== СПИСОК ГРУПП ==========
        Label listLabel = new Label("Существующие группы:");
        listLabel.setStyle("-fx-font-weight: bold;");
        groupList = new ListView<>();
        groupList.setPrefHeight(200);

        HBox listButtons = new HBox(10);
        Button refreshBtn = new Button("🔄 Обновить список");
        refreshBtn.setOnAction(e -> loadGroups());

        Button deleteBtn = new Button("🗑️ Удалить выбранную группу");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteSelectedGroup());

        listButtons.getChildren().addAll(refreshBtn, deleteBtn);

        // Область для результата
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(80);
        resultArea.setStyle("-fx-font-family: monospace;");

        content.getChildren().addAll(
                title, addPane, editPane, new Separator(),
                listLabel, groupList, listButtons,
                new Separator(), resultArea
        );

        // Загрузить список групп
        loadGroups();

        // При выборе группы заполняем форму редактирования
        groupList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
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

        Label nameLabel = new Label("Название группы:");
        nameField = new TextField();
        nameField.setPromptText("Например: CS-203");

        Label yearLabel = new Label("Год обучения:");
        yearField = new TextField();
        yearField.setPromptText("1, 2, 3, 4");

        Label curatorLabel = new Label("ID куратора:");
        curatorIdField = new TextField();
        curatorIdField.setPromptText("ID преподавателя (опционально)");

        Button addBtn = new Button("Добавить");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addGroup());

        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(yearLabel, 0, 1);
        form.add(yearField, 1, 1);
        form.add(curatorLabel, 0, 2);
        form.add(curatorIdField, 1, 2);
        form.add(addBtn, 1, 3);

        return form;
    }

    private GridPane createEditForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label idLabel = new Label("ID группы:");
        editIdField = new TextField();
        editIdField.setEditable(false);
        editIdField.setPromptText("Выберите группу из списка");

        Label nameLabel = new Label("Название:");
        editNameField = new TextField();

        Label yearLabel = new Label("Год обучения:");
        editYearField = new TextField();

        Label curatorLabel = new Label("ID куратора:");
        editCuratorField = new TextField();

        Button updateBtn = new Button("Сохранить изменения");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> updateGroup());

        form.add(idLabel, 0, 0);
        form.add(editIdField, 1, 0);
        form.add(nameLabel, 0, 1);
        form.add(editNameField, 1, 1);
        form.add(yearLabel, 0, 2);
        form.add(editYearField, 1, 2);
        form.add(curatorLabel, 0, 3);
        form.add(editCuratorField, 1, 3);
        form.add(updateBtn, 1, 4);

        return form;
    }

    private void addGroup() {
        String name = nameField.getText().trim();
        String yearText = yearField.getText().trim();

        if (name.isEmpty() || yearText.isEmpty()) {
            resultArea.setText("❌ Ошибка: Заполните название группы и год обучения");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            resultArea.setText("❌ Ошибка: Год обучения должен быть числом");
            return;
        }

        Integer curatorId = null;
        String curatorText = curatorIdField.getText().trim();
        if (!curatorText.isEmpty()) {
            try {
                curatorId = Integer.parseInt(curatorText);
            } catch (NumberFormatException e) {
                resultArea.setText("❌ Ошибка: ID куратора должен быть числом");
                return;
            }
        }

        Request request = new Request("ADD_GROUP", new Object[]{name, year, curatorId});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Группа \"" + name + "\" успешно добавлена!");
            nameField.clear();
            yearField.clear();
            curatorIdField.clear();
            loadGroups();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void fillEditForm(String selected) {
        // Формат: "ID: Название (X курс)"
        try {
            String[] parts = selected.split(":");
            int id = Integer.parseInt(parts[0].trim());
            editIdField.setText(String.valueOf(id));

            // Извлекаем название
            String namePart = parts[1].trim();
            String name = namePart.split(" \\(")[0];
            editNameField.setText(name);

            // Извлекаем курс
            String yearPart = namePart.split("\\(")[1].replace(" курс)", "");
            editYearField.setText(yearPart);

            editCuratorField.clear();
        } catch (Exception e) {
            resultArea.setText("❌ Ошибка при загрузке данных группы");
        }
    }

    private void updateGroup() {
        String idText = editIdField.getText().trim();
        String name = editNameField.getText().trim();
        String yearText = editYearField.getText().trim();

        if (idText.isEmpty() || name.isEmpty() || yearText.isEmpty()) {
            resultArea.setText("❌ Ошибка: Заполните все поля");
            return;
        }

        int id = Integer.parseInt(idText);
        int year = Integer.parseInt(yearText);

        Integer curatorId = null;
        String curatorText = editCuratorField.getText().trim();
        if (!curatorText.isEmpty()) {
            curatorId = Integer.parseInt(curatorText);
        }

        Request request = new Request("UPDATE_GROUP", new Object[]{id, name, year, curatorId});
        Response response = connection.sendRequest(request);

        if (response.isSuccess()) {
            resultArea.setText("✅ Группа успешно обновлена!");
            loadGroups();
        } else {
            resultArea.setText("❌ Ошибка: " + response.getMessage());
        }
    }

    private void deleteSelectedGroup() {
        String selected = groupList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resultArea.setText("❌ Пожалуйста, выберите группу для удаления");
            return;
        }

        int id = Integer.parseInt(selected.split(":")[0].trim());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить эту группу?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Request request = new Request("DELETE_GROUP", new Object[]{id});
            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                resultArea.setText("✅ Группа успешно удалена!");
                loadGroups();
                editIdField.clear();
                editNameField.clear();
                editYearField.clear();
                editCuratorField.clear();
            } else {
                resultArea.setText("❌ Ошибка: " + response.getMessage());
            }
        }
    }

    private void loadGroups() {
        Request request = new Request("GET_ALL_GROUPS", null);
        Response response = connection.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<String> groups = (List<String>) response.getData();
            groupList.getItems().clear();
            groupList.getItems().addAll(groups);
        } else {
            groupList.getItems().clear();
            groupList.getItems().add("Не удалось загрузить группы");
        }
    }
}