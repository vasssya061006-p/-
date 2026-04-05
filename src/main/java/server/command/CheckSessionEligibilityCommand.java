package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class CheckSessionEligibilityCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int studentId = (Integer) data[0];
            EducationService.SessionEligibilityResult result = EducationService.getInstance().checkSessionEligibility(studentId);
            return new Response(true, "Eligibility checked", result);
        } catch (Exception e) {
            return new Response(false, "Error checking eligibility: " + e.getMessage(), null);
        }
    }
}
