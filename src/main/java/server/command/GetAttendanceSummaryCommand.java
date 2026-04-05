package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetAttendanceSummaryCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int studentId = (Integer) data[0];
            EducationService.AttendanceSummaryResult result = EducationService.getInstance().getAttendanceSummary(studentId);
            return new Response(true, "Attendance retrieved", result);
        } catch (Exception e) {
            return new Response(false, "Error getting attendance: " + e.getMessage(), null);
        }
    }
}
