package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetAllTeachersCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            List<String> teachers = EducationService.getInstance().getAllTeachersAsString();
            return new Response(true, "Преподаватели загружены", teachers);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}