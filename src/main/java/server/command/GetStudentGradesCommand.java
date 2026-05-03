package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetStudentGradesCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int studentId = (Integer) data[0];
            String grades = EducationService.getInstance().getStudentGradesAsString(studentId);
            return new Response(true, "Grades loaded", grades);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }
}