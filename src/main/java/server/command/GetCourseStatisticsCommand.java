package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetCourseStatisticsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int courseId = (Integer) data[0];
            EducationService.CourseStatisticsResult result = EducationService.getInstance().getCourseStatistics(courseId);
            return new Response(true, "Statistics retrieved", result);
        } catch (Exception e) {
            return new Response(false, "Error getting statistics: " + e.getMessage(), null);
        }
    }
}
