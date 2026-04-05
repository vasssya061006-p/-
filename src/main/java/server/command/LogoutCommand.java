package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class LogoutCommand implements Command {
    private EducationService educationService = EducationService.getInstance();

    @Override
    public Response execute(Request request) {
        try {
            Integer userId = request.getUserId();
            educationService.logout(userId);
            return new Response(true, "Logout successful", null);
        } catch (Exception e) {
            return new Response(false, "Error during logout: " + e.getMessage(), null);
        }
    }
}
