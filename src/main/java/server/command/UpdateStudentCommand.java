package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class UpdateStudentCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int id = (Integer) data[0];
            String login = (String) data[1];
            String fullName = (String) data[2];
            String groupName = (String) data[3];
            int yearOfStudy = (Integer) data[4];
            String studentIdNumber = (String) data[5];
            boolean isActive = (Boolean) data[6];

            boolean success = EducationService.getInstance().updateStudent(id, login, fullName, groupName, yearOfStudy, studentIdNumber, isActive);
            return new Response(success, success ? "Студент обновлён" : "Ошибка обновления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}