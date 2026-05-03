package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class UpdateTeacherCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int id = (Integer) data[0];
            String login = (String) data[1];
            String fullName = (String) data[2];
            String position = (String) data[3];
            String department = (String) data[4];
            String specialization = (String) data[5];
            int teachingHours = (Integer) data[6];
            boolean isActive = (Boolean) data[7];

            boolean success = EducationService.getInstance().updateTeacher(id, login, fullName, position, department, specialization, teachingHours, isActive);
            return new Response(success, success ? "Преподаватель обновлён" : "Ошибка обновления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}