package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetAllStudentsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            List<String> students = EducationService.getInstance().getAllStudentsAsString();
            return new Response(true, "Студенты загружены", students);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}