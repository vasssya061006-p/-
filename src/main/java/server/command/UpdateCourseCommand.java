package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class UpdateCourseCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int id = (Integer) data[0];
            String name = (String) data[1];
            String description = (String) data[2];
            int credits = (Integer) data[3];
            Integer teacherId = (Integer) data[4];
            String department = (String) data[5];

            boolean success = EducationService.getInstance().updateCourse(id, name, description, credits, teacherId, department);
            return new Response(success, success ? "Курс обновлён" : "Ошибка обновления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}