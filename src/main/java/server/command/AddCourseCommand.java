package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class AddCourseCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String name = (String) data[0];
            String description = (String) data[1];
            int credits = (Integer) data[2];
            Integer teacherId = (Integer) data[3];
            String department = (String) data[4];

            boolean success = EducationService.getInstance().addCourse(name, description, credits, teacherId, department);
            return new Response(success, success ? "Курс добавлен" : "Ошибка добавления курса", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}