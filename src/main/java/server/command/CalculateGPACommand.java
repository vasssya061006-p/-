package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class CalculateGPACommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int studentId = (Integer) data[0];
            EducationService.GPACalculatorResult result = EducationService.getInstance().calculateStudentGPA(studentId);
            return new Response(true, "GPA calculated successfully", result);
        } catch (Exception e) {
            return new Response(false, "Error calculating GPA: " + e.getMessage(), null);
        }
    }
}
