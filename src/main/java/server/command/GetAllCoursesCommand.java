package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetAllCoursesCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            List<String> courses = EducationService.getInstance().getAllCoursesAsString();
            return new Response(true, "Курсы загружены", courses);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}