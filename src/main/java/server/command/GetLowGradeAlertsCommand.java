package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

import java.util.List;

public class GetLowGradeAlertsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            double threshold = 3.0;
            Object[] data = (Object[]) request.getData();
            if (data != null && data.length > 0) {
                threshold = (Double) data[0];
            }
            List<EducationService.AlertResult> alerts = EducationService.getInstance().getLowGradeAlerts(threshold);
            return new Response(true, "Alerts generated", alerts);
        } catch (Exception e) {
            return new Response(false, "Error generating alerts: " + e.getMessage(), null);
        }
    }
}
