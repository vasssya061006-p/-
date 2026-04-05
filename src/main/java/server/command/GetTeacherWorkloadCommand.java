package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetTeacherWorkloadCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int teacherId = (Integer) data[0];
            EducationService.TeacherWorkloadResult result = EducationService.getInstance().getTeacherWorkloadReport(teacherId);
            return new Response(true, "Workload retrieved", result);
        } catch (Exception e) {
            return new Response(false, "Error getting workload: " + e.getMessage(), null);
        }
    }
}
