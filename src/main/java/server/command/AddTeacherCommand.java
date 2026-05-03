package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class AddTeacherCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String login = (String) data[0];
            String fullName = (String) data[1];
            String position = (String) data[2];
            String department = (String) data[3];
            String specialization = (String) data[4];
            int teachingHours = (Integer) data[5];

            boolean success = EducationService.getInstance().addTeacher(login, fullName, position, department, specialization, teachingHours);
            return new Response(success, success ? "Преподаватель добавлен" : "Ошибка добавления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}