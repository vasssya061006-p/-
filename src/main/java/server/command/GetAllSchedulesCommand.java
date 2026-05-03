package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetAllSchedulesCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            String schedules = EducationService.getInstance().getAllSchedulesAsString();
            return new Response(true, "Schedules loaded", schedules);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }
}