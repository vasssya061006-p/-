package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class ExportAnalyticsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            EducationService.AnalyticsExportResult result = EducationService.getInstance().exportAcademicAnalytics();
            return new Response(true, "Analytics exported", result);
        } catch (Exception e) {
            return new Response(false, "Error exporting analytics: " + e.getMessage(), null);
        }
    }
}
