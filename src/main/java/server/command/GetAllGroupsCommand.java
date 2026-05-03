package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetAllGroupsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            List<String> groups = EducationService.getInstance().getAllGroupsAsString();
            return new Response(true, "Группы загружены", groups);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}