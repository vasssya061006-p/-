package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class ValidateScheduleCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int scheduleId = (Integer) data[0];
            EducationService.ScheduleValidationResult result = EducationService.getInstance().validateSchedule(scheduleId);
            return new Response(true, "Schedule validated", result);
        } catch (Exception e) {
            return new Response(false, "Error validating schedule: " + e.getMessage(), null);
        }
    }
}
