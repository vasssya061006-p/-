package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;
import java.util.List;

public class GetStudentsByGroupAndCourseCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String groupName = (String) data[0];
            String courseName = (String) data[1];
            int teacherId = (Integer) data[2];
            List<Object[]> students = EducationService.getInstance().getStudentsByGroupAndCourse(groupName, courseName, teacherId);
            return new Response(true, "Students loaded", students);
        } catch (Exception e) {
            return new Response(false, "Error: " + e.getMessage(), null);
        }
    }
}