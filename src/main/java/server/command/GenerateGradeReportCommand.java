package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GenerateGradeReportCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int studentId = (Integer) data[0];
            EducationService.GradeReportResult result = EducationService.getInstance().generateGradeReport(studentId);
            return new Response(true, "Report generated", result);
        } catch (Exception e) {
            return new Response(false, "Error generating report: " + e.getMessage(), null);
        }
    }
}
