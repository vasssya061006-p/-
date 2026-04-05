package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetGroupAnalyticsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int groupId = (Integer) data[0];
            EducationService.GroupAnalyticsResult result = EducationService.getInstance().getGroupPerformanceAnalytics(groupId);
            return new Response(true, "Analytics retrieved", result);
        } catch (Exception e) {
            return new Response(false, "Error getting analytics: " + e.getMessage(), null);
        }
    }
}
