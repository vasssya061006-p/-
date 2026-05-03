package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetTeacherCoursesCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            int teacherId;
            Object data = request.getData();

            if (data instanceof Integer) {
                teacherId = (Integer) data;
            } else if (data instanceof Object[]) {
                Object[] arr = (Object[]) data;
                teacherId = (Integer) arr[0];
            } else {
                return new Response(false, "Неверный формат данных", null);
            }

            List<String> courses = EducationService.getInstance().getTeacherCourses(teacherId);
            return new Response(true, "Курсы загружены", courses);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}