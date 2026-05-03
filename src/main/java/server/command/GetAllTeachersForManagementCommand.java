package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetAllTeachersForManagementCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            List<String> teachers = EducationService.getInstance().getAllTeachersForManagement();
            return new Response(true, "Преподаватели загружены", teachers);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}