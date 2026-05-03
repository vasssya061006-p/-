package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetAllSchedulesForListCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            List<String> schedules = EducationService.getInstance().getAllSchedulesForList();
            return new Response(true, "Расписание загружено", schedules);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}